package org.mineacademy.game.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.MinecraftVersion.V;
import org.mineacademy.fo.RandomUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.StrictMap;
import org.mineacademy.fo.debug.Debugger;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.model.Tuple;
import org.mineacademy.fo.model.Variables;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompMonsterEgg;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.game.countdown.PhaseCountdown;
import org.mineacademy.game.hook.CoreHookManager;
import org.mineacademy.game.impl.arena.SimpleArena;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.model.ArenaBarColor;
import org.mineacademy.game.model.ArenaData;
import org.mineacademy.game.model.ArenaPhase;
import org.mineacademy.game.model.ArenaSettings;
import org.mineacademy.game.model.ArenaSign;
import org.mineacademy.game.model.ArenaSign.SignType;
import org.mineacademy.game.model.ArenaTrigger;
import org.mineacademy.game.model.BossBarIndicator;
import org.mineacademy.game.model.SpawnPoint;
import org.mineacademy.game.settings.Localization;
import org.mineacademy.game.settings.Settings;
import org.mineacademy.game.type.ArenaState;
import org.mineacademy.game.type.NextPhaseMode;
import org.mineacademy.game.type.SpawnPointType;
import org.mineacademy.game.type.StopCause;

import lombok.Getter;

public final class SimplePhaseIncremental implements ArenaPhase {

	private final Arena arena;
	private final PhaseCountdown countdown;
	private final BossBarIndicator bossBar;
	private final ChestRefiller chestRefiller;

	private final boolean monstersMode;

	private int phase = 1;

	private boolean startedToCountNextPhase = false;

	@Getter
	private int waitedBetweenNextPhase = 0;

	public SimplePhaseIncremental(Arena arena) {
		this.arena = arena;
		this.monstersMode = arena.getSettings().getNextPhaseMode() == NextPhaseMode.MONSTERS;
		this.countdown = new PhaseCountdown(arena, monstersMode ? 1 : arena.getSettings().getPhaseDurationSeconds());
		this.bossBar = Settings.Arena.SHOW_PHASE_BAR && MinecraftVersion.newerThan(V.v1_8) ? new SimpleBossBarNative(Localization.Bossbar.COLOR_START, getColorFromBar(Localization.Bossbar.COLOR_START) + Localization.Bossbar.TITLE.replace("{phase}", phase + "")) : new SimpleBossBarDummy();
		this.chestRefiller = arena.getSettings().getChestRefill() != null ? new ChestRefiller(arena.getSettings().getChestRefill()) : null;
	}

	public void redetectChests() {
		if (chestRefiller != null)
			chestRefiller.detectChests();
	}

	public void restoreChests() {
		if (chestRefiller != null)
			chestRefiller.restore();
	}

	@Override
	public int getCurrent() {
		return phase;
	}

	@Override
	public void onTimerTick() {
		if (monstersMode)
			return;

		final double left = ((double) countdown.getTimeLeft() - 1) / countdown.getDurationSeconds();
		final ArenaBarColor color = getColorForProgress(left);

		bossBar.updateProgress(left);
		bossBar.updateColor(color);
		bossBar.updateTitle(getColorFromBar(color) + Localization.Bossbar.TITLE.replace("{phase}", phase + ""));
	}

	private ArenaBarColor getColorForProgress(double left) {
		return left > 0.37 ? Localization.Bossbar.COLOR_START : left > 0.17 ? Localization.Bossbar.COLOR_MID : Localization.Bossbar.COLOR_END;
	}

	private ChatColor getColorFromBar(ArenaBarColor color) {
		switch (color) {
			case BLUE:
				return ChatColor.BLUE;
			case GREEN:
				return ChatColor.GREEN;
			case PINK:
				return ChatColor.LIGHT_PURPLE;
			case PURPLE:
				return ChatColor.DARK_PURPLE;
			case RED:
				return ChatColor.RED;
			case WHITE:
				return ChatColor.WHITE;
			case YELLOW:
				return ChatColor.YELLOW;
		}

		throw new FoException("dead end");
	}

	private boolean waitBetweenNextPhase() {
		if (waitedBetweenNextPhase == 0)
			arena.getMessenger().broadcast(Localization.Phase.NEXT_WAIT.replace("{delay}", arena.getSettings().getNextPhaseWaitSeconds() + ""));

		if (waitedBetweenNextPhase++ < arena.getSettings().getNextPhaseWaitSeconds()) {
			Debugger.debug("phase", "Increased to " + waitedBetweenNextPhase + ", limit " + arena.getSettings().getNextPhaseWaitSeconds());

			return false;
		}

		Debugger.debug("phase", "Reset");

		startedToCountNextPhase = false;
		waitedBetweenNextPhase = 0;

		return true;
	}

	@Override
	public void onNextPhase() {

		// Stop arena if somehow it has been stopped
		if (arena.getState() != ArenaState.RUNNING) {
			if (countdown.isRunning())
				countdown.cancel();

			return;
		}

		Debugger.put("next-phase", "onNextPhase()");

		if (monstersMode) {
			if (!startedToCountNextPhase) {
				Debugger.push(" monsters alive = " + arena.getAliveMonsters());

				if (arena.getAliveMonsters() == 0) {
					// continue to the next phase
				} else
					return;
			} else
				Debugger.push(" skip");

			if (!waitBetweenNextPhase()) {
				startedToCountNextPhase = true;

				return;
			}
		}

		phase++;

		// Check if not last
		if (arena.getSettings().getLastPhase() != -1)
			if (phase > arena.getSettings().getLastPhase()) {
				arena.stopArena(StopCause.NATURAL_LAST_PHASE);

				return;
			}

		{ // Features
			//if (phase % 2 == 1)
			giveExp();

			if (chestRefiller != null)
				chestRefiller.refill();

			checkPvP();
			updatePowerSigns();
			resetBossbar(Localization.Bossbar.COLOR_START);
			spawnMobs();

			arena.getSettings().getPhaseCommands().run(arena, Settings.Arena.CONSOLE_CMD_FOREACH);
		}

		{ // Rewards
			giveRewards(arena.getSettings().getRewardsOnWave(), true);
			giveRewards(arena.getSettings().getRewardsEveryWave(), false);
		}

		// Decoration
		arena.getMessenger().broadcastBar(Localization.Bossbar.NEXT_PHASE.replace("{phase}", phase + ""));
		arena.getMessenger().playSound(CompSound.FIREWORK_LARGE_BLAST2, 0.1F);

		checkMaxPhase();
	}

	private void giveRewards(Map<Integer, List<Object>> rewards, boolean phaseEquals) {
		for (final Entry<Integer, List<Object>> entry : rewards.entrySet()) {
			final int phaseTrigger = entry.getKey();

			if ((phaseEquals && this.phase != phaseTrigger) || (!phaseEquals && this.phase % phaseTrigger != 0))
				continue;

			for (final Object raw : entry.getValue())
				for (final Player online : arena.getPlayers()) {
					if (raw instanceof String) {
						String command = arena.getMessenger().replaceVariables(raw.toString().substring(1));
						command = Variables.replace(command, online);

						Common.dispatchCommand(online, command);

					} else if (raw instanceof Tuple) {
						final Tuple<CompMaterial, Integer> tuple = (Tuple<CompMaterial, Integer>) raw;

						Valid.checkNotNull(tuple.getKey(), "Material cannot be null for " + raw + " in " + entry);
						Valid.checkNotNull(tuple.getValue(), "Quantity cannot be null for " + raw + " in " + entry);

						online.getInventory().addItem(tuple.getKey().toItem(tuple.getValue()));
					}
				}
		}
	}

	private void checkMaxPhase() {
		final int max = arena.getSettings().getMaxPhase();

		if (max != -1)
			if (phase == max) {
				arena.getMessenger().broadcast(Localization.Phase.Max.TEXT);
				Localization.Phase.Max.SOUND.play(arena.getPlayers());

				if (countdown.isRunning())
					countdown.cancel();

				resetBossbar(Localization.Bossbar.COLOR_END);
			}
	}

	private void giveExp() {
		for (final Player player : arena.getPlayers()) {

			final int points = arena.getSettings().getPhaseExp(phase);
			CoreArenaPlugin.getDataFor(player).getArenaCache().giveAndShowExp(player, points);

			arena.getMessenger().tell(player, Localization.Experience.NEXT_PHASE.replace("{amount}", points + "").replace("{phase}", phase + ""));
		}
	}

	private void checkPvP() {
		final ArenaSettings settings = arena.getSettings();

		if (settings.getPvpPhase() != 1 && phase == settings.getPvpPhase())
			arena.getMessenger().broadcast(Localization.Arena.Game.FRIENDLY_FIRE_ACTIVATED);
	}

	private void updatePowerSigns() {
		for (final ArenaSign sign : arena.getData().getSigns().getSigns(SignType.POWER)) {
			final SimpleSignPower power = (SimpleSignPower) sign;

			power.onNextPhase();
		}
	}

	private void resetBossbar(ArenaBarColor color) {
		bossBar.updateTitle(getColorFromBar(color) + Localization.Bossbar.TITLE.replace("{phase}", phase + ""));
		bossBar.updateProgress(1D);
		bossBar.updateColor(color);
	}

	final class ChestRefiller {

		private final ArenaTrigger trigger;
		private final StrictMap<Location, ItemStack[]> chestLocations = new StrictMap<>();

		private ChestRefiller(ArenaTrigger trigger) {
			this.trigger = trigger;
		}

		private void detectChests() {
			chestLocations.clear();

			if (arena.getData().getRegion().isComplete())
				for (final Block block : arena.getData().getRegion().getBlocks())
					if (block.getState() instanceof Chest) {
						final Chest ch = (Chest) block.getState();

						final List<ItemStack> copy = new ArrayList<>();

						for (final ItemStack content : ch.getBlockInventory().getContents())
							copy.add(content == null ? null : content.clone());

						chestLocations.put(block.getLocation(), copy.toArray(new ItemStack[copy.size()]));
					}
		}

		private void refill() {
			if (!trigger.trigger(phase))
				return;

			for (final Entry<Location, ItemStack[]> entry : chestLocations.entrySet()) {
				final Location location = entry.getKey();
				final BlockState state = location.getBlock().getState();

				if (state instanceof Chest) {
					final ItemStack[] items = entry.getValue();
					final Chest chest = (Chest) state;
					final Inventory chestInventory = chest.getBlockInventory();

					for (final ItemStack item : items)
						if (item != null && item.getAmount() > 0) {
							if (chestInventory.containsAtLeast(item, item.getAmount()))
								continue;

							chestInventory.addItem(item);
						}

					state.update();
				}
			}
		}

		private void restore() {
			for (final Entry<Location, ItemStack[]> entry : chestLocations.entrySet()) {
				final Location location = entry.getKey();
				final ItemStack[] items = entry.getValue();

				final BlockState state = location.getBlock().getState();

				if (state instanceof Chest) {
					final Inventory chestInventory = ((Chest) state).getBlockInventory();

					chestInventory.setContents(items);
					state.update();
				}
			}
		}
	}

	private void spawnMobs() {
		final ArenaData data = arena.getData();
		final World world = arena.getData().getRegion().getWorld();

		final int entityLimit = arena.getSettings().getMobLimit();
		int totalEntities = 0;

		Debugger.debug("spawning", "#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#");
		Debugger.debug("spawning", "#-#-#-#-#-# Launching spawners for phase " + getCurrent() + " #-#-#-#-#-#");

		{ // Limit entites

			for (final LivingEntity entity : world.getLivingEntities())
				if (entity.getType() != EntityType.PLAYER && data.getRegion().isWithin(entity.getLocation())) {
					if (entity instanceof Tameable && ((Tameable) entity).isTamed())
						continue;

					totalEntities++;
				}

			if (totalEntities >= entityLimit) {
				Debugger.debug("spawning", "Monster limit reached. There's " + totalEntities + " already, the max is " + entityLimit + ". Not spawning.");

				return;
			}
		}

		points:
		for (final SpawnPoint rawPoint : data.getSpawnPoints(SpawnPointType.MONSTER)) {
			final SimpleSpawnPointMonster spawner = (SimpleSpawnPointMonster) rawPoint;

			Debugger.debug("spawning", getCurrent() + " ::: Activating " + spawner);

			if (!spawner.getActivePeriod().mayExecute(getCurrent())) {
				Debugger.debug("spawning", "\tFAIL - invalid phase");
				continue;
			}

			if (arena.getPlayers().size() < spawner.getMinimumPlayers()) {
				Debugger.debug("spawning", "\tFAIL - too few players");
				continue;
			}

			if (!RandomUtil.chance(spawner.getChance())) {
				Debugger.debug("spawning", "\tFAIL - chance not hit");
				continue;
			}

			final Location loc = spawner.getLocation().clone().add(0, 1, 0);
			boolean inRange = false;
			final int activationRadius = arena.getSettings().getSpawnerActivationRadius();

			for (final Player arenaPlayer : arena.getPlayers())
				if (arenaPlayer.getLocation().distance(loc) <= activationRadius) {
					inRange = true;
					Debugger.debug("spawning", "\tFound " + arenaPlayer.getName() + " in range of " + Math.round(arenaPlayer.getLocation().distance(loc)) + " blocks from spawner");

					break;
				}

			if (!inRange) {
				Debugger.debug("spawning", "\tFAIL - no players in range of " + activationRadius + " blocks");

				continue;
			}

			final SpawnedEntity[] types = spawner.getSpawnedTypes();

			if (types == null || types.length == 0) {
				Common.log("[Arena " + arena.getName() + "] Spawnpoint at " + Common.shortLocation(loc) + " has no monsters configured!");

				continue;
			}

			final SpawnedEntity[] entitiesToSpawn = spawner.isSpawnAllSlots() ? types : new SpawnedEntity[] { RandomUtil.nextItem(types) };

			for (final SpawnedEntity spawnedEntity : entitiesToSpawn) {
				if (spawnedEntity == null)
					continue;

				Debugger.debug("spawning", "\t" + (spawner.isSpawnAllSlots() ? "Iterating through" : "Selected spawner") + " slot: " + spawnedEntity.format());

				for (int i = 0; i < spawnedEntity.getCount(); i++) {
					if (totalEntities >= entityLimit) {
						Debugger.debug("spawning", "\t\tSTOP - max entity limit reached: " + totalEntities + " total vs " + entityLimit + " max");

						break points;
					}

					totalEntities++;

					final Location randomLoc = randomizeLocation(loc);
					Entity spawned = null;

					if (spawnedEntity.isCustom()) {
						if (CoreHookManager.isMythicMobsLoaded() && CoreHookManager.tryMythicalSpawn(spawnedEntity.getCustomItem(), randomLoc)) {
							Debugger.debug("spawning", "\t\tSUCCESS - handled in MythicMobs");

							continue;
						}

						if (CoreHookManager.isBossLoaded()) {
							spawned = CoreHookManager.tryBOSSSpawn(spawnedEntity.getCustomItem(), randomLoc);

							if (spawned != null)
								Debugger.debug("spawning", "\t\tSUCCESS - handled in Boss");
						} else {
							final EntityType eggMob = CompMonsterEgg.getEntity(spawnedEntity.getCustomItem());

							if (eggMob != null) {
								spawned = randomLoc.getWorld().spawnEntity(randomLoc, eggMob);

								Debugger.debug("spawning", "\t\tSUCCESS - spawning from custom egg - " + eggMob);
							} else
								Debugger.debug("spawning", "\t\tFAIL - unknown custom egg = " + spawnedEntity);
						}
					}

					if (spawned == null) {
						final String debugMessage = "spawning " + spawnedEntity.getType() + " at " + Common.shortLocation(randomLoc);

						try {
							spawned = world.spawnEntity(randomLoc, spawnedEntity.getType());

							if (spawned.isValid())
								Debugger.debug("spawning", "\t\tSUCCESS " + debugMessage);
							else
								Debugger.debug("spawning", "\t\tFAIL " + debugMessage);

						} catch (final IllegalArgumentException ex) {
							if (spawnedEntity.isCustom()) {
								Debugger.debug("spawning", "\t\tFAIL " + debugMessage + " but custom, forgiving! Error: " + ex);

								continue;
							}

							Debugger.debug("spawning", "\t\tFAIL " + debugMessage + ", got " + ex);
							throw ex;
						}
					}

					Valid.checkNotNull(spawned, "Failed to spawn " + spawnedEntity + " in " + arena.getName());

					if (spawned instanceof Creature) {
						final Player target = ((SimpleArena) arena).getNearestPlayer(spawned.getLocation());

						if (target != null)
							((Creature) spawned).setTarget(target);
					}
				}
			}
		}
	}

	private Location randomizeLocation(Location loc) {
		final int tries = 20;
		final int spread = arena.getSettings().getMobSpread();

		Location newLoc = loc;

		for (int i = 0; i < tries; i++) {
			newLoc = newLoc.clone().add(getPositiveOrNegRandom() * Math.random() * (1 + RandomUtil.nextInt(spread)), 0, getPositiveOrNegRandom() * Math.random() * (1 + RandomUtil.nextInt(spread)));

			final Block b = newLoc.getBlock();

			if (b.getType() == Material.AIR && b.getRelative(BlockFace.UP).getType() == Material.AIR)
				return newLoc;
		}

		return newLoc.add(0.5, 0, 0.5);
	}

	private int getPositiveOrNegRandom() {
		return RandomUtil.nextBoolean() ? 1 : -1;
	}

	public void onPlayerLeave(Player pl) {
		if (bossBar.hasBar(pl))
			bossBar.hideFrom(pl);
	}

	@Override
	public void startTimer() {
		countdown.launch();

		try {
			spawnMobs();

		} catch (final Throwable t) {
			Common.error(t, "Error while spawning mobs in " + arena.getName(), "%error", "Stopping arena for safety.");
			arena.stopArena(StopCause.INTERRUPTED_ERROR);

			return;
		}

		for (final Player pl : arena.getPlayers())
			bossBar.showTo(pl);
	}

	@Override
	public void stopAndReset() {
		if (countdown.isRunning())
			countdown.cancel();

		phase = 1;
		waitedBetweenNextPhase = 0;

		resetBossbar(Localization.Bossbar.COLOR_START);
		bossBar.hide();
	}
}