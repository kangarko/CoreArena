package org.mineacademy.corearena.impl.arena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.countdown.EndCountdown;
import org.mineacademy.corearena.countdown.LaunchCountdown;
import org.mineacademy.corearena.data.AllData.ArenaPlayer;
import org.mineacademy.corearena.data.AllData.ArenaPlayer.ClassCache;
import org.mineacademy.corearena.data.AllData.ArenaPlayer.InArenaCache;
import org.mineacademy.corearena.event.ArenaJoinEvent;
import org.mineacademy.corearena.event.ArenaLeaveEvent;
import org.mineacademy.corearena.event.ArenaLobbyTeleportEvent;
import org.mineacademy.corearena.event.ArenaPostStartEvent;
import org.mineacademy.corearena.event.ArenaPostStopEvent;
import org.mineacademy.corearena.event.ArenaPreJoinEvent;
import org.mineacademy.corearena.event.ArenaPreLeaveEvent;
import org.mineacademy.corearena.event.LobbyStartEvent;
import org.mineacademy.corearena.event.SpawnTeleportEvent;
import org.mineacademy.corearena.exception.CancelledException;
import org.mineacademy.corearena.exception.EventHandledException;
import org.mineacademy.corearena.impl.SimpleMessenger;
import org.mineacademy.corearena.impl.SimplePhaseIncremental;
import org.mineacademy.corearena.impl.SimpleSignPower;
import org.mineacademy.corearena.impl.SimpleSnapshotDummy;
import org.mineacademy.corearena.impl.SimpleSnapshotWorldEdit;
import org.mineacademy.corearena.manager.ClassManager;
import org.mineacademy.corearena.manager.UpgradesManager;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.model.ArenaData;
import org.mineacademy.corearena.model.ArenaPhase;
import org.mineacademy.corearena.model.ArenaPlugin;
import org.mineacademy.corearena.model.ArenaRegistry;
import org.mineacademy.corearena.model.ArenaSettings;
import org.mineacademy.corearena.model.ArenaSign;
import org.mineacademy.corearena.model.ArenaSign.SignType;
import org.mineacademy.corearena.model.ArenaSnapshot;
import org.mineacademy.corearena.model.ArenaSnapshotProcedural.DamagedStage;
import org.mineacademy.corearena.model.ArenaSnapshotStage;
import org.mineacademy.corearena.physics.PhysicalEngine;
import org.mineacademy.corearena.settings.Settings;
import org.mineacademy.corearena.type.ArenaState;
import org.mineacademy.corearena.type.DeathCause;
import org.mineacademy.corearena.type.JoinCause;
import org.mineacademy.corearena.type.LeaveCause;
import org.mineacademy.corearena.type.NextPhaseMode;
import org.mineacademy.corearena.type.StopCause;
import org.mineacademy.corearena.util.InventoryStorageUtil;
import org.mineacademy.corearena.util.Permissions;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.CommonCore;
import org.mineacademy.fo.EntityUtil;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.debug.Debugger;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.model.HookManager;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.model.SimpleRunnable;
import org.mineacademy.fo.platform.BukkitPlugin;
import org.mineacademy.fo.platform.FoundationPlayer;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.remain.CompMetadata;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.fo.settings.Lang;

public abstract class SimpleArena implements Arena {

	private final Set<Player> players = new HashSet<>();

	private final SimpleMessenger messenger;
	private final ArenaData data;
	private final ArenaSettings settings;
	private final ArenaSnapshot snapshot;
	private final ArenaPhase phase;

	private ArenaState state = ArenaState.STOPPED;

	private EndCountdown endCountdown;
	private LaunchCountdown launchCountdown;

	private boolean stopping = false;

	public static final void clearRegisteredArenas() {
		ArenaRegistry.unregisterAll(Platform.getPlugin().getName());
	}

	protected SimpleArena(ArenaSettings settings) {
		this.settings = settings;
		this.data = settings.getDataSection();

		this.messenger = new SimpleMessenger(this);
		this.snapshot = settings.hasProceduralDamage() && HookManager.isWorldEditLoaded() ? new SimpleSnapshotWorldEdit(this) : new SimpleSnapshotDummy(this);
		this.phase = new SimplePhaseIncremental(this);

		ArenaRegistry.register(this);
	}

	@Override
	public final void onPostLoad() {
		this.getData().onPostLoad();
	}

	// --------------------------------------------------------------------
	// Player joins. This starts the lobby -> counter -> arena
	// --------------------------------------------------------------------

	@Override
	public final boolean joinPlayer(Player player, JoinCause cause) {
		try {
			if (!Platform.callEvent(new ArenaPreJoinEvent(this, cause, player)))
				return false;

			if (!this.stopping && this.isArenaAcceptingJoining(player) && this.canPlayerJoin(player, cause)) {
				this.handlePlayerJoining(player, cause);

				return true;
			}

			return false;

		} catch (final Throwable t) {
			Common.error(t,
					"Error while joining " + player.getName() + " to " + this.getName(),
					"Arena has been stopped for safety.",
					"Error: {error}");

			this.stopArena(StopCause.INTERRUPTED_ERROR);
			return false;
		}
	}

	private final boolean isArenaAcceptingJoining(Player player) {
		if (!this.isEnabled()) {
			this.getMessenger().tell(player, Lang.component("arena-error-not-enabled"));
			return false;
		}

		if (CoreArenaPlugin.getSetupManager().isArenaEdited(this)) {
			this.getMessenger().tell(player, Lang.component("arena-error-edited"));
			return false;
		}

		if (!this.getSetup().isReady()) {
			this.getMessenger().tell(player, Lang.component("arena-error-not-configured"));
			return false;
		}

		if (this.getState() == ArenaState.RUNNING) {
			this.getMessenger().tell(player, Lang.component("arena-error-already-running"));
			return false;
		}

		if (this.getPlayers().size() >= this.getSettings().getMaximumPlayers()) {
			this.getMessenger().tell(player, Lang.component("arena-error-full"));
			return false;
		}

		return true;
	}

	private final boolean canPlayerJoin(Player player, JoinCause cause) {

		final String joinPermission = Permissions.Commands.JOIN.replace("{arena}", this.getName().toLowerCase());

		if (!player.hasPermission(joinPermission)) {
			this.getMessenger().tell(player, Lang.component("arena-error-no-permission", "perm", joinPermission));
			return false;
		}

		final Arena existing = ArenaRegistry.getCommonManager().findArena(player);

		if (existing != null) {
			this.getMessenger().tell(player, Lang.component("arena-error-already-playing", "arena", existing.getName()));
			return false;
		}

		final Arena editedArena = CoreArenaPlugin.getSetupManager().getEditedArena(player);

		if (editedArena != null) {
			this.getMessenger().tell(player, Lang.component("arena-cannot-do-while-editing", "arena", editedArena.getName()));
			return false;
		}

		if (!this.getSettings().allowOwnEquipment()) {
			if (!Settings.Arena.STORE_INVENTORIES && !PlayerUtil.hasEmptyInventory(player)) {
				this.getMessenger().tell(player, Lang.component("arena-error-inventory-not-empty"));
				return false;
			}
		}

		if (!this.data.getRegion().isWithin(this.data.getLobby().getLocation().clone().add(0, 2, 0))) {
			this.getMessenger().tell(player, SimpleComponent.fromPlain("Arena " + this.getName() + " has lobby point outside of its region, alert admins to move it 2 blocks below the region top boundary or expand the region itself!"));

			return false;
		}

		return true;
	}

	private final void handlePlayerJoining(Player player, JoinCause cause) {
		Valid.checkBoolean(!this.getPlayers().contains(player), "Player " + player.getName() + " already playing " + this.getName());

		this.players.add(player);

		final ArenaPlayer cache = this.getCache(player);
		cache.setCurrentArena(player, this);
		cache.getArenaCache().pendingJoining = true;

		final ArenaJoinEvent joinEvent = new ArenaJoinEvent(this, cause, player);
		Platform.callEvent(joinEvent);

		if (this.state == ArenaState.STOPPED)
			this.startLobby();

		if (this.getSetup().areJoinSignsSet())
			this.getData().getSigns().updateSigns(SignType.JOIN, this);

		{ // Inventory

			if (this.settings.allowOwnEquipment())
				InventoryStorageUtil.getInstance().saveExperience(player);

			else if (Settings.Arena.STORE_INVENTORIES)
				InventoryStorageUtil.getInstance().saveInventory(player);

			// Clean inventory
			PlayerUtil.normalize(player, !this.settings.allowOwnEquipment() && cache.getClassCache().assignedClass == null);
		}

		// Move to lobby
		{
			final ArenaLobbyTeleportEvent tpEvent = new ArenaLobbyTeleportEvent(player, this, this.data.getLobby().getLocation().clone().add(0, 1, 0));

			if (Platform.callEvent(tpEvent))
				player.teleport(tpEvent.getLocation());
		}

		player.setMetadata("CoreArena_Arena", new FixedMetadataValue(BukkitPlugin.getInstance(), this.getName()));

		this.handlePlayerPostJoin(player);

		if (!joinEvent.isSilent()) {
			this.getMessenger().broadcastExcept(player, player, Lang.component("arena-lobby-join-others"));

			this.getMessenger().tell(player, Lang.component("arena-lobby-join-player", "arena", this.getName(), "time", Lang.numberFormat("case-second", this.launchCountdown.getTimeLeft())));
			this.getMessenger().playSound(player, this.settings.getPlayerJoinSound());
		}

		cache.getArenaCache().pendingJoining = false;
	}

	protected abstract void handlePlayerPostJoin(Player pl);

	// --------------------------------------------------------------------
	// Handle basic arena start and end events
	// --------------------------------------------------------------------

	@Override
	public final void startLobby() {
		Valid.checkBoolean(this.getState() == ArenaState.STOPPED, "Cannot start lobby when arena is started or already has lobby");

		this.stopping = false;
		this.setState(ArenaState.LOBBY);

		final LobbyStartEvent event = new LobbyStartEvent(this);
		Platform.callEvent(event);

		try {
			this.removeEntities();
		} catch (final Throwable t) {
			t.printStackTrace();
		}

		try {
			this.settings.getLobbyStartCommands().run(this, this.getPlayers(), Settings.Arena.CONSOLE_CMD_FOREACH);
		} catch (final Throwable t) {
			t.printStackTrace();
		}

		this.launchCountdown = new LaunchCountdown(this);

		if (event.getCountdown() > 0)
			this.launchCountdown.launch();

		for (final ArenaSign sign : this.getData().getSigns().getSigns(SignType.POWER)) {
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
			started = this.doStart(force);

		} catch (final Throwable t) {
			Common.error(t,
					"Error starting arena " + this.getName() + "!",
					"Error: {error}",
					"The arena has been stopped for safety.");

			error = true;
		}

		if (error && !started) // Do not start not running arenas
			this.stopArena(StopCause.INTERRUPTED_ERROR);

		return started && !error;
	}

	private final boolean doStart(boolean force) {
		Valid.checkBoolean(this.getState() != ArenaState.RUNNING, "Cannot start already running arena " + this.getName());

		if (this.getPlayers().size() < this.getSettings().getMinimumPlayers()) {
			this.stopArena(StopCause.CANCELLED_NOT_ENOUGH_PLAYERS);

			return false;
		}

		try {
			this.setState(ArenaState.RUNNING);
			this.getMessenger().broadcastAndLogFramed(Lang.component("arena-game-start"));
		} catch (final Throwable t) {
			t.printStackTrace();
		}

		try { // Safely end
			if (this.launchCountdown != null && this.launchCountdown.isRunning())
				this.launchCountdown.cancel();
		} catch (final Throwable t) {
			t.printStackTrace();
		}

		try {
			this.endCountdown = new EndCountdown(this);
			this.endCountdown.launch();
		} catch (final Throwable t) {
			t.printStackTrace();
		}

		try {
			for (final Player player : this.getPlayers())
				player.closeInventory();
		} catch (final Throwable t) {
			t.printStackTrace();
		}

		this.handleArenaPostStart();

		if (this.phase instanceof SimplePhaseIncremental)
			((SimplePhaseIncremental) this.phase).redetectChests();

		try {
			if (force)
				this.getPhase().stopAndReset();

			this.getPhase().startTimer();
		} catch (final Throwable t) {
			t.printStackTrace();
		}

		try {
			this.settings.getStartCommands().run(this, this.getPlayers(), Settings.Arena.CONSOLE_CMD_FOREACH);
		} catch (final Throwable t) {
			t.printStackTrace();
		}

		try {
			for (final ArenaSign sign : this.getData().getSigns().getSigns(SignType.POWER)) {
				final SimpleSignPower power = (SimpleSignPower) sign;

				power.onArenaStart();
			}
		} catch (final Throwable t) {
			t.printStackTrace();
		}

		this.getMessenger().playSound(this.settings.getArenaStartSound());

		Platform.callEvent(new ArenaPostStartEvent(this));
		return true;
	}

	protected abstract void handleArenaPostStart();

	@Override
	public final void stopArena(StopCause cause) {
		try {
			this.doStop(cause);

		} catch (final Throwable t) {
			Common.error(t,
					"Error stopping arena " + this.getName() + "!",
					"Error: {error}",
					"All players have been kicked for safety.");

			for (final Player pl : this.getPlayers())
				this.kickPlayer(pl, LeaveCause.ERROR);
		}
	}

	private final void doStop(StopCause cause) {
		if (this.stopping)
			return;

		if (cause != StopCause.INTERRUPTED_ERROR)
			Valid.checkBoolean(this.getState() != ArenaState.STOPPED, "Cannot stop not running arena " + this.getName());

		Debugger.debug("arena", "Stopping arena " + this.getName() + " due to " + cause);

		this.stopping = true;

		try {
			this.getMessenger().broadcastAndLogFramed(cause == StopCause.CANCELLED_NOT_ENOUGH_PLAYERS ? Lang.component("arena-lobby-fail-start-not-enough-players") : Lang.component("arena-game-end-generic"));

			if (this.endCountdown != null && this.endCountdown.isRunning())
				this.endCountdown.cancel();

			if (this.launchCountdown != null && this.launchCountdown.isRunning())
				this.launchCountdown.cancel();

			this.getPhase().stopAndReset();

			PhysicalEngine.cancelRunning();

			this.removeEntities();

			this.snapshot.restore(DamagedStage.INITIAL);

			for (final ArenaSign sign : this.getData().getSigns().getSigns(SignType.POWER)) {
				final SimpleSignPower power = (SimpleSignPower) sign;

				power.onArenaEnd();
			}

			this.handleArenaPostStop(cause);

			if (this.phase instanceof SimplePhaseIncremental)
				((SimplePhaseIncremental) this.phase).restoreChests();

			final int endDelayTicks = this.settings.getEndCommandsDelay().getTimeTicks();
			final Collection<Player> players = endDelayTicks > 0 ? new ArrayList<>(this.getPlayers()) : this.getPlayers();

			CommonCore.runTaskOrNow(endDelayTicks, () -> {
				if (cause.toString().startsWith("NATURAL")) {
					if (this.settings.getNextPhaseMode() == NextPhaseMode.MONSTERS && cause == StopCause.NATURAL_COUNTDOWN) {
						// Ignore cause
					} else
						this.settings.getFinishCommands().run(this, players, Settings.Arena.CONSOLE_CMD_FOREACH);
				}

				this.settings.getEndCommands().run(this, players, Settings.Arena.CONSOLE_CMD_FOREACH);
			});

			try {
				this.removeAllPlayers();
			} catch (final Throwable t) {
				Common.error(t, "Failed to remove all players from " + this.getName(), "Error: {error}");
			}

			this.setState(ArenaState.STOPPED);
			Platform.callEvent(new ArenaPostStopEvent(this, cause));

			this.stopping = false;

		} catch (final Throwable t) {
			Common.error(t, "Failed to stop " + this.getName(), "Kicking players for safety..", "Error: {error}");

			try {
				this.removeAllPlayers();
			} catch (final Throwable tt) {
				Common.error(tt, "Unrecoverably failed to remove all players from " + this.getName(), "Error: {error}");
			}
		}
	}

	private final void removeEntities() {
		for (final Entity en : this.getData().getRegion().getEntities())
			if (en != null && (EntityUtil.isAggressive(en) || EntityUtil.canBeCleaned(en) || CompMetadata.hasMetadata(en, "CoreTempEntity")))
				en.remove();
	}

	protected abstract void handleArenaPostStop(StopCause cause);

	// --------------------------------------------------------------------
	// Handle player-related events
	// --------------------------------------------------------------------

	@Override
	public boolean kickPlayer(Player pl, LeaveCause cause) {
		this.checkRunning();
		Valid.checkBoolean(this.getPlayers().contains(pl), "Player " + pl.getName() + " not playing in " + this.getName());

		final boolean kicked = this.handlePlayerLeave(pl, cause, null);

		return kicked;
	}

	private final void removeAllPlayers() {
		final Iterator<Player> it = this.players.iterator();

		while (it.hasNext()) {
			Player pl = null;

			try {
				pl = it.next();
			} catch (final ConcurrentModificationException ex) { // Handle below
			}

			Valid.checkBoolean(pl != null && pl.isOnline(), "Report / Unhandled player leave in " + this.getName() + "! Current players: {" + Common.join(this.players) + "}");
			this.handlePlayerLeave(pl, LeaveCause.ARENA_END, it);
		}
	}

	private final boolean handlePlayerLeave(Player player, LeaveCause cause, Iterator<Player> it) {
		final InArenaCache cache = this.getCache(player).getArenaCache();

		Valid.checkBoolean(!cache.pendingRemoval, "Report / Player " + player.getName() + " is already pending removal!");

		Debugger.debug("arena", "Kicking player " + player.getName() + " from " + this.getName() + " due to " + cause);

		final ArenaPreLeaveEvent event = new ArenaPreLeaveEvent(this, cause, player);
		if (!Platform.callEvent(event))
			return false;

		cache.pendingRemoval = true;

		try {
			this.handlePrePlayerLeave(player, cause);
			this.handlePlayerLeave0(player, cause, event.isSilent());

		} catch (final Throwable t) {
			Common.error(t, "Error while removing player " + player.getName() + " from " + this.getName(), "Moving him to spawn.", "Error: {error}");
			this.teleportPlayerBack(player);

		} finally {
			if (it != null)
				it.remove();
			else
				this.players.remove(player);

			this.getCache(player).removeCurrentArena();

			Platform.callEvent(new ArenaLeaveEvent(this, cause, player));
		}

		player.removeMetadata("CoreArena_Arena", BukkitPlugin.getInstance());

		this.stopIfPlayersBelowLimit();

		return true;
	}

	private final void stopIfPlayersBelowLimit() {
		final int autoStop = this.getSettings().getAutoStopPlayersLimit() != -1 ? this.getSettings().getAutoStopPlayersLimit() : 0;

		if (this.getPlayers().size() <= autoStop)
			this.stopArena(StopCause.INTERRUPTED_LAST_PLAYER_LEFT);
	}

	private final void handlePlayerLeave0(Player player, LeaveCause cause, boolean silent) {
		final ArenaPlayer data = this.getCache(player);

		{ // De-set class' settings
			final ClassCache cache = data.getClassCache();

			if (cache.assignedClass != null)
				cache.assignedClass.getTier(cache.classTier).onArenaLeave(player);
		}

		try {
			PlayerUtil.normalize(player, !this.settings.allowOwnEquipment());

			Platform.runTask(3, () -> {
				if (player.isOnline())
					InventoryStorageUtil.getInstance().restoreIfStored(player);
			});

		} catch (final Throwable t) {
			t.printStackTrace();
		}

		if (this.getPhase() instanceof SimplePhaseIncremental)
			((SimplePhaseIncremental) this.getPhase()).onPlayerLeave(player);

		final Location prev = player.getLocation();

		this.settings.getPlayerLeaveCommands().runAsConsole(this, player);
		this.settings.getPlayerLeaveCommands().runAsPlayer(this, player);

		if (this.getSetup().areJoinSignsSet())
			this.getData().getSigns().updateSigns(SignType.JOIN, this);

		try {
			if (player.isOnline() && player.getLocation().distance(prev) < 3 && cause != LeaveCause.ESCAPED)
				this.teleportPlayerBack(player);
		} catch (final Throwable t) {
			t.printStackTrace();
		}

		// Convert Levels to Nuggets
		if (this.isEligibleForNuggets(cause)) {
			final double nuggetsFromPlayingD = data.getArenaCache().getLevel() * Settings.Experience.LEVEL_TO_NUGET_CONVERSION_RATIO;
			final int nuggetsFromPlaying = (int) Math.round(nuggetsFromPlayingD);

			final int totalNuggets = data.getNuggets() + nuggetsFromPlaying;

			data.setNuggets(totalNuggets);

			if (nuggetsFromPlaying != 0)
				this.getMessenger().tell(player, Lang.component("currency-received",
						"amount", Lang.numberFormat("currency-name", nuggetsFromPlaying),
						"balance", Lang.numberFormat("currency-name", totalNuggets)));
		}

		// Decorative messages.
		// Delay so it's played after respawn
		if (!silent)
			Platform.runTask(3, new SimpleRunnable() {

				@Override
				public void run() {
					final FoundationPlayer audience = Platform.toPlayer(player);

					SimpleArena.this.getMessenger().playSound(player, SimpleArena.this.settings.getPlayerLeaveSound());

					if (cause == LeaveCause.ARENA_END || cause == LeaveCause.KILLED || cause == LeaveCause.COMMAND || cause == LeaveCause.ERROR)
						audience.showTitle(Lang.component("title-kick-title", "arena", getName()), Lang.component("title-kick-subtitle", "arena", getName()));

					else if (cause == LeaveCause.ESCAPED)
						audience.showTitle(Lang.component("title-escape-title", "arena", getName()), Lang.component("title-escape-subtitle", "arena", getName()));
				}
			});
	}

	private final boolean isEligibleForNuggets(LeaveCause cause) {
		if (!Settings.Experience.REWARD_ESCAPE && (cause == LeaveCause.COMMAND || cause == LeaveCause.DISCONNECT || cause == LeaveCause.ESCAPED))
			return false;

		return this.state == ArenaState.RUNNING && cause != LeaveCause.NO_ENOUGH_CLASS && cause != LeaveCause.NOT_READY && cause != LeaveCause.ERROR;
	}

	protected abstract void handlePrePlayerLeave(Player pl, LeaveCause cause);

	@Override
	public void onPlayerDeath(Player pl, Player killer) throws EventHandledException {
		this.handlePlayerDeath(pl);
	}

	@Override
	public void onPlayerDeath(Player pl, DeathCause cause) throws EventHandledException {
		this.handlePlayerDeath(pl);
	}

	private final void handlePlayerDeath(Player pl) throws EventHandledException {
		this.checkRunning();

		if (this.getState() == ArenaState.LOBBY) {
			Remain.respawn(pl, 0);

			this.returnHandled();
		}
	}

	@Override
	public void onPlayerRespawn(PlayerRespawnEvent e) throws EventHandledException {
		this.checkRunning();

		final InArenaCache cache = this.getCache(e.getPlayer()).getArenaCache();

		if (cache.pendingRemoval) {
			cache.pendingRemoval = false;

			e.setRespawnLocation(this.getBackLocation(e.getPlayer()));

			final boolean kicked = this.kickPlayer(e.getPlayer(), LeaveCause.KILLED);

			if (kicked)
				this.returnHandled();
		}

		if (this.getState() == ArenaState.LOBBY) {
			final Location lobby = this.data.getLobby().getLocation().clone().add(0, 1, 0);

			e.setRespawnLocation(lobby);
			this.returnHandled();
		}
	}

	@Override
	public final void teleportPlayerBack(Player pl) {
		//checkRunning();

		final SpawnTeleportEvent event = new SpawnTeleportEvent(pl, this.getBackLocation(pl));

		if (Platform.callEvent(event)) {
			final Location loc = event.getLocation();
			final ArenaPlayer data = this.getCache(pl);

			if (data.hasArenaCache() && data.getArenaCache().pendingRemoval)
				Platform.runTask(2, () -> this.teleportToSpawn0(loc, pl));

			else
				this.teleportToSpawn0(loc, pl);
		}
	}

	private final void teleportToSpawn0(Location loc, Player player) {
		player.teleport(loc);

		if (player.getGameMode() == GameMode.CREATIVE && loc.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR)
			player.setFlying(true);

		HookManager.setBackLocation(player, loc);
	}

	private final Location getBackLocation(Player player) {
		final ArenaPlayer data = this.getCache(player);

		if (data.hasArenaCache()) {
			final Location loc = data.getArenaCache().prevLocation;

			Valid.checkNotNull(loc, "Player's previous world cannot be null!");
			return loc;
		}

		return this.getData().getRegion().getWorld().getSpawnLocation();
	}

	// --------------------------------------------------------------------
	// Handle entity-related events
	// --------------------------------------------------------------------

	@Override
	public final void onEntitySpawn(EntitySpawnEvent e) {
		final Entity en = e.getEntity();

		if (EntityUtil.isCreature(en))
			CompMetadata.setMetadata(en, "CoreTempEntity", "CoreTempEntity");
	}

	@Override
	public void onEntityDeath(EntityDeathEvent e) {
		final boolean allMonstersKilled = this.getAliveMonsters() == 0;
		final int endClear = this.settings.getEndPhaseNoMonsters();

		if (allMonstersKilled && endClear != -1 && this.phase.getCurrent() >= endClear) {
			this.stopArena(StopCause.NATURAL_NO_MONSTERS);

			this.returnHandled();
		}
	}

	@Override
	public final void onEntityTarget(EntityTargetEvent event) throws EventHandledException {
		this.checkRunning();

		final Entity entity = event.getEntity();
		final Entity target = event.getTarget();

		if (entity.getType() == EntityType.WOLF && this.isTamed(entity))
			return;

		if (target != null && target.getType() == EntityType.WOLF && this.isTamed(target))
			return;

		final TargetReason targetReason = event.getReason();

		if (targetReason == TargetReason.TARGET_ATTACKED_ENTITY)
			event.setCancelled(true);

		else if (targetReason == TargetReason.FORGOT_TARGET || targetReason.toString().equals("CLOSEST_ENTITY"))
			event.setTarget(this.getNearestPlayer(event.getEntity().getLocation()));
	}

	private final boolean isTamed(Entity wolf) {
		return ((Wolf) wolf).isTamed();
	}

	@Override
	public final void onSnapshotUpdate(ArenaSnapshotStage newState) {
	}

	@Override
	public final void onHealthRegen(EntityRegainHealthEvent event, Player player) {
		if (this.settings.disableHealthRegen() && !event.getRegainReason().toString().contains("MAGIC"))
			this.returnCancelled();
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

		for (final Player pl : this.getPlayers())
			if (this.getData().getRegion().isWithin(pl.getLocation()))
				sorted.add(pl);

		return !sorted.isEmpty() ? sorted.first() : null;
	}

	private final void setState(ArenaState newState) {
		this.state = newState;

		if (this.getSetup().areJoinSignsSet())
			this.getData().getSigns().updateSigns(SignType.JOIN, this);
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
		return this.settings.getName();
	}

	@Override
	public final SimpleMessenger getMessenger() {
		return this.messenger;
	}

	@Override
	public final Collection<Player> getPlayers() {
		if (!this.isStopping() && this.getState() == ArenaState.STOPPED && !this.players.isEmpty())
			throw new FoException("Found players in a stopped arena " + this.getName() + ": " + Common.join(this.players, ", "));

		return Collections.unmodifiableCollection(this.players);
	}

	@Override
	public boolean isJoined(Player player) {
		this.checkPlayers();

		return this.players.contains(player);
	}

	@Override
	public boolean isJoined(String playerName) {
		this.checkPlayers();

		for (final Player player : this.players)
			if (player.getName().equalsIgnoreCase(playerName))
				return true;

		return false;
	}

	private final void checkPlayers() {
		if (!this.isStopping() && this.getState() == ArenaState.STOPPED && !this.players.isEmpty())
			throw new FoException("Found players in a stopped arena " + this.getName() + ": " + Common.join(this.players, ", "));
	}

	@Override
	public final ArenaData getData() {
		return this.data;
	}

	@Override
	public final ArenaSettings getSettings() {
		return this.settings;
	}

	@Override
	public final ArenaState getState() {
		return this.state;
	}

	@Override
	public final ArenaSnapshot getSnapshot() {
		return this.snapshot;
	}

	@Override
	public final ArenaPhase getPhase() {
		return this.phase;
	}

	@Override
	public final boolean isStopping() {
		return this.stopping;
	}

	@Override
	public final int getRemainingSeconds() {
		return this.endCountdown != null ? this.endCountdown.getTimeLeft() : this.getSettings().getArenaDurationSeconds();
	}

	@Override
	public final int getRemainingLobbySeconds() {
		return this.launchCountdown != null ? this.launchCountdown.getTimeLeft() : this.getSettings().getLobbyDurationSeconds();
	}

	@Override
	public int getAliveMonsters() {
		int alive = 0;

		for (final Entity entity : this.data.getRegion().getEntities())
			if (EntityUtil.isAggressive(entity) && !entity.hasMetadata("ae-entity")) { // Ignore AdvancedEnchantments-spawned mobs
				Debugger.debug("alive-monsters", "Counting alive monster: " + entity);

				alive++;
			}

		return alive;
	}

	@Override
	public final ArenaPlugin getPlugin() {
		return CoreArenaPlugin.getInstance();
	}

	@Override
	public final boolean isEnabled() {
		return this.data.isEnabled();
	}

	@Override
	public final void setEnabled(boolean enabled) {
		this.data.setEnabled(enabled);
	}

	protected final void checkRunning() {
		if (this.getState() == ArenaState.STOPPED)
			throw new FoException("Cannot call checkRunning() in a non running arena " + this.getName());
	}

	public final ClassManager getClassManager() {
		return CoreArenaPlugin.getClassManager();
	}

	public final UpgradesManager getUpgradesManager() {
		return CoreArenaPlugin.getUpgradesManager();
	}

	@Override
	public final boolean equals(Object obj) {
		return obj instanceof SimpleArena && ((SimpleArena) obj).getName().equals(this.getName());
	}

	@Override
	public final String toString() {
		return "Arena{plugin=" + this.getPlugin() + ", name=" + this.getName() + "}";
	}
}
