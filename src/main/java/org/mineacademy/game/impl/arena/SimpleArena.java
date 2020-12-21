package org.mineacademy.game.impl.arena;

import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.EntityUtil;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.MinecraftVersion.V;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.ReflectionUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.StrictSet;
import org.mineacademy.fo.debug.Debugger;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.model.HookManager;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.remain.CompMetadata;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.game.countdown.EndCountdown;
import org.mineacademy.game.countdown.LaunchCountdown;
import org.mineacademy.game.event.ArenaJoinEvent;
import org.mineacademy.game.event.ArenaLeaveEvent;
import org.mineacademy.game.event.ArenaLobbyTeleportEvent;
import org.mineacademy.game.event.ArenaPostStartEvent;
import org.mineacademy.game.event.ArenaPostStopEvent;
import org.mineacademy.game.event.ArenaPreJoinEvent;
import org.mineacademy.game.event.ArenaPreLeaveEvent;
import org.mineacademy.game.event.LobbyStartEvent;
import org.mineacademy.game.event.SpawnTeleportEvent;
import org.mineacademy.game.exception.CancelledException;
import org.mineacademy.game.exception.EventHandledException;
import org.mineacademy.game.impl.ArenaPlayer;
import org.mineacademy.game.impl.ArenaPlayer.ClassCache;
import org.mineacademy.game.impl.ArenaPlayer.InArenaCache;
import org.mineacademy.game.impl.SimpleMessenger;
import org.mineacademy.game.impl.SimplePhaseIncremental;
import org.mineacademy.game.impl.SimpleSignPower;
import org.mineacademy.game.impl.SimpleSnapshotDummy;
import org.mineacademy.game.impl.SimpleSnapshotWorldEdit;
import org.mineacademy.game.manager.ClassManager;
import org.mineacademy.game.manager.UpgradesManager;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.model.ArenaData;
import org.mineacademy.game.model.ArenaPhase;
import org.mineacademy.game.model.ArenaPlugin;
import org.mineacademy.game.model.ArenaRegistry;
import org.mineacademy.game.model.ArenaSettings;
import org.mineacademy.game.model.ArenaSign;
import org.mineacademy.game.model.ArenaSign.SignType;
import org.mineacademy.game.model.ArenaSnapshot;
import org.mineacademy.game.model.ArenaSnapshotProcedural.DamagedStage;
import org.mineacademy.game.model.ArenaSnapshotStage;
import org.mineacademy.game.physics.BlockPhysicsCheckTask;
import org.mineacademy.game.settings.Localization;
import org.mineacademy.game.settings.Settings;
import org.mineacademy.game.type.ArenaState;
import org.mineacademy.game.type.DeathCause;
import org.mineacademy.game.type.JoinCause;
import org.mineacademy.game.type.LeaveCause;
import org.mineacademy.game.type.StopCause;
import org.mineacademy.game.util.InventoryStorageUtil;
import org.mineacademy.game.util.Permissions;

public abstract class SimpleArena implements Arena {

	private final StrictSet<Player> players = new StrictSet<>();

	private final SimpleMessenger messenger;
	private final ArenaData data;
	private final ArenaSettings settings;
	private final ArenaSnapshot snapshot;
	private final ArenaPhase phase;

	private ArenaState state = ArenaState.STOPPED;

	private EndCountdown endCountdown;
	private LaunchCountdown launchCountdown;

	private boolean stopping = false;
	private boolean restoreSnapshots = true;

	public static final void clearRegisteredArenas() {
		ArenaRegistry.unregisterAll(SimplePlugin.getNamed());
	}

	protected SimpleArena(ArenaSettings settings) {
		this.settings = settings;
		data = settings.getDataSection();

		messenger = new SimpleMessenger(this);
		snapshot = Settings.ProceduralDamage.ENABLED && settings.hasProceduralDamage() && HookManager.isWorldEditLoaded() && MinecraftVersion.atLeast(V.v1_13) ? new SimpleSnapshotWorldEdit(this) : new SimpleSnapshotDummy(this);
		phase = new SimplePhaseIncremental(this);

		ArenaRegistry.register(this);
	}

	@Override
	public final void onPostLoad() {
		getData().onPostLoad();
	}

	// --------------------------------------------------------------------
	// Player joins. This starts the lobby -> counter -> arena
	// --------------------------------------------------------------------

	@Override
	public final boolean joinPlayer(Player pl, JoinCause cause) {
		try {
			if (!Common.callEvent(new ArenaPreJoinEvent(this, cause, pl)))
				return false;

			if (!stopping && isArenaAcceptingJoining(pl) && canPlayerJoin(pl, cause)) {
				handlePlayerJoining(pl, cause);

				return true;
			}

			return false;

		} catch (final Throwable t) {
			Common.error(t, "Error while joining " + pl.getName() + " to " + getName(), "Arena has been stopped for safety.", "%error");

			stopArena(StopCause.INTERRUPTED_ERROR);
			return false;
		}
	}

	private final boolean isArenaAcceptingJoining(Player pl) {
		if (!isEnabled()) {
			getMessenger().tell(pl, Localization.Arena.Error.NOT_ENABLED);
			return false;
		}

		if (CoreArenaPlugin.getSetupManager().isArenaEdited(this)) {
			getMessenger().tell(pl, Localization.Arena.Error.EDITED);
			return false;
		}

		if (!getSetup().isReady()) {
			getMessenger().tell(pl, Localization.Arena.Error.NOT_CONFIGURED);
			return false;
		}

		if (getState() == ArenaState.RUNNING) {
			getMessenger().tell(pl, Localization.Arena.Error.ALREADY_RUNNING);
			return false;
		}

		if (getPlayers().size() >= getSettings().getMaximumPlayers()) {
			getMessenger().tell(pl, Localization.Arena.Error.FULL);
			return false;
		}

		return true;
	}

	private final boolean canPlayerJoin(Player player, JoinCause cause) {

		final String joinPermission = Permissions.Commands.JOIN.replace("{arena}", this.getName().toLowerCase());

		if (!PlayerUtil.hasPerm(player, joinPermission)) {
			getMessenger().tell(player, Localization.Arena.Error.NO_PERMISSION.replace("{perm}", joinPermission));

			return false;
		}

		final Arena existing = ArenaRegistry.getCommonManager().findArena(player);

		if (existing != null) {
			getMessenger().tell(player, Localization.Arena.Error.ALREADY_PLAYING.replace("{arena}", existing.getName()));

			return false;
		}

		final Arena editedArena = CoreArenaPlugin.getSetupManager().getEditedArena(player);

		if (editedArena != null) {
			getMessenger().tell(player, Localization.Arena.CANNOT_DO_WHILE_EDITING.replace("{arena}", editedArena.getName()));
			return false;
		}

		if (!getSettings().allowOwnEquipment())
			if (!Settings.Arena.STORE_INVENTORIES && !PlayerUtil.hasEmptyInventory(player)) {
				getMessenger().tell(player, Localization.Arena.Error.INVENTORY_NOT_EMPTY);
				return false;
			}

		return true;
	}

	private final void handlePlayerJoining(Player player, JoinCause cause) {
		Valid.checkBoolean(!getPlayers().contains(player), "Player " + player.getName() + " already playing " + getName());

		players.add(player);

		final ArenaPlayer cache = getCache(player);
		cache.setCurrentArena(player, this);
		cache.getArenaCache().pendingJoining = true;

		final ArenaJoinEvent joinEvent = new ArenaJoinEvent(this, cause, player);
		Common.callEvent(joinEvent);

		if (state == ArenaState.STOPPED)
			startLobby();

		if (getSetup().areJoinSignsSet())
			getData().getSigns().updateSigns(SignType.JOIN, this);

		{ // Inventory
			final InventoryStorageUtil inv = InventoryStorageUtil.$();

			if (settings.allowOwnEquipment())
				inv.saveExperience(player);

			else if (Settings.Arena.STORE_INVENTORIES)
				inv.saveInventory(player);

			// Clean inventory
			PlayerUtil.normalize(player, !settings.allowOwnEquipment() && cache.getClassCache().assignedClass == null);
		}

		// Move to lobby
		{
			final ArenaLobbyTeleportEvent tpEvent = new ArenaLobbyTeleportEvent(player, this, data.getLobby().getLocation().clone().add(0, 1, 0));

			if (Common.callEvent(tpEvent))
				player.teleport(tpEvent.getLocation());
		}

		handlePlayerPostJoin(player);

		if (!joinEvent.isSilent()) {
			getMessenger().broadcastExcept(player, player, Localization.Arena.Lobby.JOIN_OTHERS);
			getMessenger().tell(player, Localization.Arena.Lobby.JOIN_PLAYER.replace("{time}", Localization.Cases.SECOND.formatWithCount(launchCountdown.getTimeLeft())));

			getMessenger().playSound(player, settings.getPlayerJoinSound());
		}

		cache.getArenaCache().pendingJoining = false;
	}

	protected abstract void handlePlayerPostJoin(Player pl);

	// --------------------------------------------------------------------
	// Handle basic arena start and end events
	// --------------------------------------------------------------------

	@Override
	public final void startLobby() {
		Valid.checkBoolean(getState() == ArenaState.STOPPED, "Cannot start lobby when arena is started or already has lobby");

		stopping = false;
		setState(ArenaState.LOBBY);

		final LobbyStartEvent event = new LobbyStartEvent(this);
		Common.callEvent(event);

		try {
			removeEntities();
		} catch (final Throwable t) {
			t.printStackTrace();
		}

		try {
			settings.getLobbyStartCommands().run(this, Settings.Arena.CONSOLE_CMD_FOREACH);
		} catch (final Throwable t) {
			t.printStackTrace();
		}

		if (restoreSnapshots)
			try {
				snapshot.restore(DamagedStage.INITIAL);

			} catch (final Throwable t) {
				Common.error(t, "Failed to set the initial snapshot of " + getName(), "%error");
			}

		launchCountdown = new LaunchCountdown(this);

		if (event.getCountdown() > 0)
			launchCountdown.launch();

		for (final ArenaSign sign : getData().getSigns().getSigns(SignType.POWER)) {
			final SimpleSignPower power = (SimpleSignPower) sign;

			power.onLobbyStart();
		}
	}

	// --------------------------------------------------------------------
	// Check. This starts the lobby -> counter -> arena
	// --------------------------------------------------------------------

	@Override
	public final boolean startArena(boolean force) {
		boolean started = false;
		boolean error = false;

		try {
			started = doStart(force);

		} catch (final Throwable t) {
			Common.error(t,
					"Error starting arena " + getName() + "!",
					"%error",
					"The arena has been stopped for safety.");

			error = true;
		}

		if (error && !started) // Do not start not running arenas
			stopArena(StopCause.INTERRUPTED_ERROR);

		return started && !error;
	}

	private final boolean doStart(boolean force) {
		Valid.checkBoolean(getState() != ArenaState.RUNNING, "Cannot start already running arena " + getName());

		if (getPlayers().size() < getSettings().getMinimumPlayers()) {
			stopArena(StopCause.CANCELLED_NOT_ENOUGH_PLAYERS);

			return false;
		}

		try {
			setState(ArenaState.RUNNING);
			getMessenger().broadcastAndLogFramed(Localization.Arena.Game.START);
		} catch (final Throwable t) {
			t.printStackTrace();
		}

		try { // Safely end
			if (launchCountdown != null && launchCountdown.isRunning())
				launchCountdown.cancel();
		} catch (final Throwable t) {
			t.printStackTrace();
		}

		try {
			endCountdown = new EndCountdown(this);
			endCountdown.launch();
		} catch (final Throwable t) {
			t.printStackTrace();
		}

		try {
			for (final Player player : getPlayers())
				player.closeInventory();
		} catch (final Throwable t) {
			t.printStackTrace();
		}

		handleArenaPostStart();

		if (phase instanceof SimplePhaseIncremental)
			((SimplePhaseIncremental) phase).redetectChests();

		try {
			if (force)
				getPhase().stopAndReset();

			getPhase().startTimer();
		} catch (final Throwable t) {
			t.printStackTrace();
		}

		try {
			settings.getStartCommands().run(this, Settings.Arena.CONSOLE_CMD_FOREACH);
		} catch (final Throwable t) {
			t.printStackTrace();
		}

		try {
			for (final ArenaSign sign : getData().getSigns().getSigns(SignType.POWER)) {
				final SimpleSignPower power = (SimpleSignPower) sign;

				power.onArenaStart();
			}
		} catch (final Throwable t) {
			t.printStackTrace();
		}

		getMessenger().playSound(settings.getArenaStartSound());

		Common.callEvent(new ArenaPostStartEvent(this));
		return true;
	}

	protected abstract void handleArenaPostStart();

	@Override
	public final void stopArena(StopCause cause) {
		try {
			doStop(cause);

		} catch (final Throwable t) {
			Common.error(t,
					"Error stopping arena " + getName() + "!",
					"%error",
					"All players have been kicked for safety.");

			for (final Player pl : getPlayers())
				kickPlayer(pl, LeaveCause.ERROR);
		}
	}

	private final void doStop(StopCause cause) {
		synchronized (players) {
			if (stopping)
				return;

			if (cause != StopCause.INTERRUPTED_ERROR)
				Valid.checkBoolean(getState() != ArenaState.STOPPED, "Cannot stop not running arena " + getName());

			stopping = true;

			try {
				getMessenger().broadcastAndLogFramed(cause == StopCause.CANCELLED_NOT_ENOUGH_PLAYERS ? Localization.Arena.Lobby.FAIL_START_NOT_ENOUGH_PLAYERS : Localization.Arena.Game.END_GENERIC);

				if (endCountdown != null && endCountdown.isRunning())
					endCountdown.cancel();

				if (launchCountdown != null && launchCountdown.isRunning())
					launchCountdown.cancel();

				getPhase().stopAndReset();

				BlockPhysicsCheckTask.cancelRunning();

				removeEntities();

				if (restoreSnapshots)
					snapshot.restore(DamagedStage.INITIAL);

				for (final ArenaSign sign : getData().getSigns().getSigns(SignType.POWER)) {
					final SimpleSignPower power = (SimpleSignPower) sign;

					power.onArenaEnd();
				}

				handleArenaPostStop(cause);

				if (phase instanceof SimplePhaseIncremental)
					((SimplePhaseIncremental) phase).restoreChests();

				if (cause.toString().startsWith("NATURAL"))
					settings.getFinishCommands().run(this, Settings.Arena.CONSOLE_CMD_FOREACH);

				settings.getEndCommands().run(this, Settings.Arena.CONSOLE_CMD_FOREACH);

				try {
					removeAllPlayers();
				} catch (final Throwable t) {
					Common.error(t, "Failed to remove all players from " + getName(), "%error");
				}

				setState(ArenaState.STOPPED);
				Common.callEvent(new ArenaPostStopEvent(this, cause));

				stopping = false;

			} catch (final Throwable t) {
				Common.error(t, "Failed to stop " + getName(), "Kicking players for safety..", "%error");

				try {
					removeAllPlayers();
				} catch (final Throwable tt) {
					Common.error(tt, "Unrecoverably failed to remove all players from " + getName(), "%error");
				}
			}
		}
	}

	private final void removeEntities() {
		for (final Entity en : getData().getRegion().getEntities())
			if (en != null && (EntityUtil.isAggressive(en) || EntityUtil.canBeCleaned(en) || CompMetadata.hasMetadata(en, "CoreTempEntity")))
				en.remove();
	}

	protected abstract void handleArenaPostStop(StopCause cause);

	// --------------------------------------------------------------------
	// Handle player-related events
	// --------------------------------------------------------------------

	@Override
	public boolean kickPlayer(Player pl, LeaveCause cause) {
		checkRunning();
		Valid.checkBoolean(getPlayers().contains(pl), "Player " + pl.getName() + " not playing in " + getName());

		final boolean kicked = handlePlayerLeave(pl, cause, null);

		return kicked;
	}

	private final void removeAllPlayers() {
		final Iterator<Player> it = players.iterator();

		while (it.hasNext()) {
			Player pl = null;

			try {
				pl = it.next();
			} catch (final ConcurrentModificationException ex) { // Handle below
			}

			Valid.checkBoolean(pl != null && pl.isOnline(), "Report / Unhandled player leave in " + getName() + "! Current players: {" + Common.join(players) + "}");
			handlePlayerLeave(pl, LeaveCause.ARENA_END, it);
		}
	}

	private final boolean handlePlayerLeave(Player pl, LeaveCause cause, Iterator<Player> it) {
		final InArenaCache cache = getCache(pl).getArenaCache();

		Valid.checkBoolean(!cache.pendingRemoval, "Report / Player " + pl.getName() + " is already pending removal!");

		final ArenaPreLeaveEvent event = new ArenaPreLeaveEvent(this, cause, pl);
		if (!Common.callEvent(event))
			return false;

		cache.pendingRemoval = true;

		try {
			handlePrePlayerLeave(pl, cause);
			handlePlayerLeave0(pl, cause, event.isSilent());

		} catch (final Throwable t) {
			Common.error(t, "Error while removing player " + pl.getName() + " from " + getName(), "Moving him to spawn.", "%error");
			teleportPlayerBack(pl);

		} finally {
			if (it != null)
				it.remove();
			else
				players.remove(pl);

			getCache(pl).removeCurrentArena();

			Common.callEvent(new ArenaLeaveEvent(this, cause, pl));
		}

		stopIfPlayersBelowLimit();

		return true;
	}

	private final void stopIfPlayersBelowLimit() {
		final int autoStop = getSettings().getAutoStopPlayersLimit() != -1 ? getSettings().getAutoStopPlayersLimit() : 0;

		if (getPlayers().size() <= autoStop)
			stopArena(StopCause.INTERRUPTED_LAST_PLAYER_LEFT);
	}

	private final void handlePlayerLeave0(Player player, LeaveCause cause, boolean silent) {
		final ArenaPlayer data = getCache(player);

		{ // De-set class' settings
			final ClassCache cache = data.getClassCache();

			if (cache.assignedClass != null)
				cache.assignedClass.getTier(cache.classTier).onArenaLeave(player);
		}

		try {
			PlayerUtil.normalize(player, !settings.allowOwnEquipment());

			Common.runLater(2, () -> {
				final InventoryStorageUtil i = InventoryStorageUtil.$();

				if (i.hasStored(player)) {
					if (settings.allowOwnEquipment())
						i.restoreExperience(player); // Only restore experience since we reserve the bar for Nuggets
					else if (Settings.Arena.STORE_INVENTORIES)
						i.restore(player);

				} else
					Common.log("&cSkipping restoring " + player.getName() + "'s exp or inventory, hasn't any but was in the arena " + getName());
			});

		} catch (final Throwable t) {
			t.printStackTrace();
		}

		if (getPhase() instanceof SimplePhaseIncremental)
			((SimplePhaseIncremental) getPhase()).onPlayerLeave(player);

		final Location prev = player.getLocation();

		if (true) { // Always run
			settings.getPlayerLeaveCommands().runAsConsole(this, player);
			settings.getPlayerLeaveCommands().runAsPlayer(this, player);
		}

		if (getSetup().areJoinSignsSet())
			getData().getSigns().updateSigns(SignType.JOIN, this);

		if (player.isOnline() && player.getLocation().distance(prev) < 3 && cause != LeaveCause.ESCAPED)
			try {
				teleportPlayerBack(player);
			} catch (final Throwable t) {
				t.printStackTrace();
			}

		// Convert Levels to Nuggets
		if (isEligibleForNuggets(cause)) {
			final double nuggetsFromPlayingD = data.getArenaCache().getLevel() * Settings.Experience.LEVEL_TO_NUGET_CONVERSION_RATIO;
			final int nuggetsFromPlaying = (int) Math.round(nuggetsFromPlayingD);

			final int totalNuggets = data.getNuggets() + nuggetsFromPlaying;

			Debugger.debug("rewards", "Setting Nuggets for " + player.getName() + " leaving arena due to " + cause + ". Before: " + data.getNuggets() + ", after: " + totalNuggets);

			data.setNuggets(totalNuggets);

			if (nuggetsFromPlaying != 0)
				getMessenger().tell(player, Localization.Currency.RECEIVED.replace("{amount}", Localization.Currency.format(nuggetsFromPlaying)).replace("{balance}", Localization.Currency.format(totalNuggets)));
		}

		// Decorative messages.
		// Delay so it's played after respawn
		if (!silent)
			Common.runLater(3, new BukkitRunnable() {

				@Override
				public void run() {
					getMessenger().playSound(player, settings.getPlayerLeaveSound());

					if (cause == LeaveCause.ARENA_END || cause == LeaveCause.KILLED || cause == LeaveCause.COMMAND || cause == LeaveCause.ERROR)
						Localization.Title.KICK.playLong(player, message -> getMessenger().replaceVariables(message));
					else if (cause == LeaveCause.ESCAPED)
						Localization.Title.ESCAPE.playLong(player, message -> getMessenger().replaceVariables(message));
				}
			});
	}

	private final boolean isEligibleForNuggets(LeaveCause cause) {
		if (!Settings.Experience.REWARD_ESCAPE && (cause == LeaveCause.COMMAND || cause == LeaveCause.DISCONNECT || cause == LeaveCause.ESCAPED))
			return false;

		return state == ArenaState.RUNNING && cause != LeaveCause.NO_ENOUGH_CLASS && cause != LeaveCause.NOT_READY && cause != LeaveCause.ERROR;
	}

	protected abstract void handlePrePlayerLeave(Player pl, LeaveCause cause);

	@Override
	public void onPlayerDeath(Player pl, Player killer) throws EventHandledException {
		handlePlayerDeath(pl);
	}

	@Override
	public void onPlayerDeath(Player pl, DeathCause cause) throws EventHandledException {
		handlePlayerDeath(pl);
	}

	private final void handlePlayerDeath(Player pl) throws EventHandledException {
		checkRunning();

		if (getState() == ArenaState.LOBBY) {
			Remain.respawn(pl, 0);

			returnHandled();
		}
	}

	@Override
	public void onPlayerRespawn(PlayerRespawnEvent e) throws EventHandledException {
		checkRunning();

		final InArenaCache cache = getCache(e.getPlayer()).getArenaCache();

		if (cache.pendingRemoval) {
			cache.pendingRemoval = false;

			e.setRespawnLocation(getBackLocation(e.getPlayer()));

			final boolean kicked = kickPlayer(e.getPlayer(), LeaveCause.KILLED);

			if (kicked)
				returnHandled();
		}

		if (getState() == ArenaState.LOBBY) {
			final Location lobby = data.getLobby().getLocation().clone().add(0, 1, 0);

			e.setRespawnLocation(lobby);
			returnHandled();
		}
	}

	@Override
	public final void teleportPlayerBack(Player pl) {
		//checkRunning();

		final SpawnTeleportEvent event = new SpawnTeleportEvent(pl, getBackLocation(pl));

		if (Common.callEvent(event)) {
			final Location loc = event.getLocation();
			final ArenaPlayer data = getCache(pl);

			if (data.hasArenaCache() && data.getArenaCache().pendingRemoval)
				Common.runLater(2, () -> teleportToSpawn0(loc, pl));

			else
				teleportToSpawn0(loc, pl);
		}
	}

	private final void teleportToSpawn0(Location loc, Player player) {
		player.teleport(loc);

		if (player.getGameMode() == GameMode.CREATIVE && loc.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR)
			player.setFlying(true);

		HookManager.setBackLocation(player, loc);
	}

	private final Location getBackLocation(Player player) {
		final ArenaPlayer data = getCache(player);

		if (data.hasArenaCache()) {
			final Location loc = data.getArenaCache().prevLocation;

			Valid.checkNotNull(loc, "Player's previous world cannot be null!");
			return loc;
		}

		return getData().getRegion().getWorld().getSpawnLocation();
	}

	// --------------------------------------------------------------------
	// Handle entity-related events
	// --------------------------------------------------------------------

	@Override
	public final void onEntitySpawn(EntitySpawnEvent e) {
		final Entity en = e.getEntity();

		if (EntityUtil.isCreature(en))
			CompMetadata.setMetadata(en, "CoreTempEntity");
	}

	@Override
	public void onEntityDeath(EntityDeathEvent e) {
		final boolean allMonstersKilled = getAliveMonsters() == 0;
		final int endClear = settings.getEndPhaseNoMonsters();

		if (allMonstersKilled && endClear != -1 && phase.getCurrent() >= endClear) {
			stopArena(StopCause.NATURAL_NO_MONSTERS);

			returnHandled();
		}
	}

	@Override
	public final void onEntityTarget(EntityTargetEvent e) throws EventHandledException {
		checkRunning();

		final Entity en = e.getEntity();
		final Entity target = e.getTarget();

		if (en.getType() == EntityType.WOLF && isTamed(en))
			return;

		if (target != null && target.getType() == EntityType.WOLF && isTamed(target))
			return;

		final TargetReason r = e.getReason();

		if (r == TargetReason.TARGET_ATTACKED_ENTITY)
			e.setCancelled(true);

		else if (r == TargetReason.FORGOT_TARGET || r.toString().equals("CLOSEST_ENTITY"))
			e.setTarget(getNearestPlayer(e.getEntity().getLocation()));
	}

	private final boolean isTamed(Entity wolf) {
		return ((Wolf) wolf).isTamed();
	}

	@Override
	public final void onSnapshotUpdate(ArenaSnapshotStage newState) {
	}

	// ----------------------------------------------------------------------
	// Utility
	// ----------------------------------------------------------------------

	public final Player getNearestPlayer(Location loc) {
		final TreeSet<Player> sorted = new TreeSet<>((first, second) -> {
			final Double firstDistance = first.getLocation().distance(loc);
			final Double secondDistance = second.getLocation().distance(loc);

			return firstDistance.compareTo(secondDistance);
		});

		for (final Player pl : getPlayers())
			if (getData().getRegion().isWithin(pl.getLocation()))
				sorted.add(pl);

		return !sorted.isEmpty() ? sorted.first() : null;
	}

	private final void setState(ArenaState newState) {
		state = newState;

		if (getSetup().areJoinSignsSet())
			getData().getSigns().updateSigns(SignType.JOIN, this);
	}

	protected final ArenaPlayer getCache(Player player) {
		return CoreArenaPlugin.getDataFor(player);
	}

	protected final EventHandledException returnHandled() throws EventHandledException {
		throw new EventHandledException();
	}

	protected final CancelledException returnCancelled() throws CancelledException {
		throw new CancelledException();
	}

	// ----------------------------------------------------------------------
	// Technical Getters (reason here: they are made final)
	// ----------------------------------------------------------------------

	@Override
	public final String getName() {
		return settings.getName();
	}

	@Override
	public final SimpleMessenger getMessenger() {
		return messenger;
	}

	@Override
	public final Collection<Player> getPlayers() {
		if (!isStopping() && getState() == ArenaState.STOPPED && !players.isEmpty())
			throw new FoException("Found players in a stopped arena " + getName() + ": " + StringUtils.join(players.getSource(), ", "));

		return Collections.unmodifiableCollection(players.getSource());
	}

	@Override
	public boolean isJoined(Player player) {
		checkPlayers();

		return players.contains(player);
	}

	@Override
	public boolean isJoined(String playerName) {
		checkPlayers();

		for (final Player player : players)
			if (player.getName().equalsIgnoreCase(playerName))
				return true;

		return false;
	}

	private final void checkPlayers() {
		if (!isStopping() && getState() == ArenaState.STOPPED && !players.isEmpty())
			throw new FoException("Found players in a stopped arena " + getName() + ": " + StringUtils.join(players.getSource(), ", "));
	}

	@Override
	public final ArenaData getData() {
		return data;
	}

	@Override
	public final ArenaSettings getSettings() {
		return settings;
	}

	@Override
	public final ArenaState getState() {
		return state;
	}

	@Override
	public final ArenaSnapshot getSnapshot() {
		return snapshot;
	}

	@Override
	public final ArenaPhase getPhase() {
		return phase;
	}

	@Override
	public final boolean isStopping() {
		return stopping;
	}

	@Override
	public final int getRemainingSeconds() {
		return endCountdown != null ? endCountdown.getTimeLeft() : getSettings().getArenaDurationSeconds();
	}

	@Override
	public int getAliveMonsters() {
		int alive = 0;

		for (final Entity en : data.getRegion().getEntities())
			if (EntityUtil.isAggressive(en))
				alive++;

		return alive;
	}

	@Override
	public final void setRestoreSnapshots(boolean restoreSnapshot) {
		this.restoreSnapshots = restoreSnapshot;
	}

	@Override
	public final ArenaPlugin getPlugin() {
		return CoreArenaPlugin.getInstance();
	}

	@Override
	public final boolean isEnabled() {
		return data.isEnabled();
	}

	@Override
	public final void setEnabled(boolean enabled) {
		data.setEnabled(enabled);
	}

	protected final void checkRunning() {
		if (getState() == ArenaState.STOPPED)
			throw new FoException("Cannot call " + ReflectionUtil.getCallerMethods(1, 5) + "() in a non running arena " + getName());
	}

	public final ClassManager getClassManager() {
		return CoreArenaPlugin.getClassManager();
	}

	public final UpgradesManager getUpgradesManager() {
		return CoreArenaPlugin.getUpgradesManager();
	}

	@Override
	public final boolean equals(Object obj) {
		return obj instanceof SimpleArena && ((SimpleArena) obj).getName().equals(getName());
	}

	@Override
	public final String toString() {
		return "Arena{plugin=" + getPlugin() + ", name=" + getName() + "}";
	}
}
