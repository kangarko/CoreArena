package org.mineacademy.corearena.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.mineacademy.corearena.countdown.PhaseCountdown;
import org.mineacademy.corearena.data.AllData.ArenaPlayer.ClassCache;
import org.mineacademy.corearena.hook.CoreHookManager;
import org.mineacademy.corearena.impl.arena.SimpleArena;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.model.ArenaData;
import org.mineacademy.corearena.model.ArenaPhase;
import org.mineacademy.corearena.model.ArenaSettings;
import org.mineacademy.corearena.model.ArenaSign;
import org.mineacademy.corearena.model.ArenaSign.SignType;
import org.mineacademy.corearena.model.ArenaTrigger;
import org.mineacademy.corearena.model.BossBarIndicator;
import org.mineacademy.corearena.model.SpawnPoint;
import org.mineacademy.corearena.settings.Settings;
import org.mineacademy.corearena.type.ArenaState;
import org.mineacademy.corearena.type.NextPhaseMode;
import org.mineacademy.corearena.type.SpawnPointType;
import org.mineacademy.corearena.type.StopCause;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.MinecraftVersion.V;
import org.mineacademy.fo.RandomUtil;
import org.mineacademy.fo.ReflectionUtil;
import org.mineacademy.fo.SerializeUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.debug.Debugger;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.model.CompChatColor;
import org.mineacademy.fo.model.HookManager;
import org.mineacademy.fo.model.SimpleSound;
import org.mineacademy.fo.model.Triple;
import org.mineacademy.fo.model.Variables;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.remain.CompEntityType;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompMetadata;
import org.mineacademy.fo.remain.CompMonsterEgg;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.fo.settings.Lang;

import lombok.Getter;
import net.kyori.adventure.bossbar.BossBar;

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
		this.countdown = new PhaseCountdown(arena, this.monstersMode ? 1 : arena.getSettings().getPhaseDurationSeconds());

		this.bossBar = Settings.Arena.SHOW_PHASE_BAR
				? (MinecraftVersion.atLeast(V.v1_9)
						? new SimpleBossBarNative(Lang.plain("bossbar-color-start"), this.getColorFromBar(Lang.plain("bossbar-color-start")) + Lang.legacy("bossbar-title", "phase", this.phase + ""))
						: new SimpleBossBarLegacy(Lang.plain("bossbar-color-start"), this.getColorFromBar(Lang.plain("bossbar-color-start")) + Lang.legacy("bossbar-title", "phase", this.phase + "")))
				: new SimpleBossBarDummy();

		this.chestRefiller = arena.getSettings().getChestRefill() != null ? new ChestRefiller(arena.getSettings().getChestRefill()) : null;
	}

	public void redetectChests() {
		if (this.chestRefiller != null)
			this.chestRefiller.detectChests();
	}

	public void restoreChests() {
		if (this.chestRefiller != null)
			this.chestRefiller.restore();
	}

	@Override
	public int getCurrent() {
		return this.phase;
	}

	@Override
	public void onTimerTick() {
		if (this.monstersMode)
			return;

		final double left = ((double) this.countdown.getTimeLeft() - 1) / this.countdown.getDurationSeconds();
		final BossBar.Color color = this.convertStringToColor(this.getColorForProgress(left));

		this.bossBar.updateProgress((float) left);
		this.bossBar.updateColor(color);
		this.bossBar.updateTitle(this.getColorFromBar(color) + Lang.legacy("bossbar-title", "phase", this.phase + ""));
	}

	private String getColorForProgress(double left) {
		return left > 0.37 ? Lang.plain("bossbar-color-start") : left > 0.17 ? Lang.plain("bossbar-color-mid") : Lang.plain("bossbar-color-end");
	}

	private CompChatColor getColorFromBar(String colorRaw) {
		return this.getColorFromBar(this.convertStringToColor(colorRaw));
	}

	private BossBar.Color convertStringToColor(String colorName) {
		return ReflectionUtil.lookupEnum(BossBar.Color.class, colorName);
	}

	private CompChatColor getColorFromBar(BossBar.Color color) {
		switch (color) {
			case BLUE:
				return CompChatColor.BLUE;
			case GREEN:
				return CompChatColor.GREEN;
			case PINK:
				return CompChatColor.LIGHT_PURPLE;
			case PURPLE:
				return CompChatColor.DARK_PURPLE;
			case RED:
				return CompChatColor.RED;
			case WHITE:
				return CompChatColor.WHITE;
			case YELLOW:
				return CompChatColor.YELLOW;
		}

		throw new FoException("dead end");
	}

	private boolean waitBetweenNextPhase() {
		if (this.waitedBetweenNextPhase == 0)
			this.arena.getMessenger().broadcast(Lang.component("phase-next-wait", "delay", this.arena.getSettings().getNextPhaseWaitSeconds() + ""));

		if (this.waitedBetweenNextPhase++ < this.arena.getSettings().getNextPhaseWaitSeconds())
			return false;

		this.startedToCountNextPhase = false;
		this.waitedBetweenNextPhase = 0;

		return true;
	}

	@Override
	public void onNextPhase() {

		// Stop arena if somehow it has been stopped
		if (this.arena.getState() != ArenaState.RUNNING) {
			if (this.countdown.isRunning())
				this.countdown.cancel();

			return;
		}

		if (this.monstersMode) {
			if (!this.startedToCountNextPhase) {
				final int aliveMonsters = this.arena.getAliveMonsters();

				if (aliveMonsters == 0) {
					Debugger.debug("next-phase", "No aggressive monsters in arena, continuing to next phase.");
					// continue to the next phase

				} else {
					Debugger.debug("next-phase", "Waiting for aggressive monsters to be killed. Remaining: " + aliveMonsters);

					return;
				}

			} else if (!this.waitBetweenNextPhase()) {
				this.startedToCountNextPhase = true;

				return;
			}
		}

		this.phase++;

		// Check if not last
		if (this.arena.getSettings().getLastPhase() != -1)
			if (this.phase > this.arena.getSettings().getLastPhase()) {
				this.arena.getSettings().getLastPhaseCommands().run(this.arena, this.arena.getPlayers(), Settings.Arena.CONSOLE_CMD_FOREACH);
				this.arena.stopArena(StopCause.NATURAL_LAST_PHASE);

				return;
			}

		{ // Features
			this.giveExp();

			if (this.chestRefiller != null)
				this.chestRefiller.refill();

			this.checkPvP();
			this.updatePowerSigns();
			this.resetBossbar(this.convertStringToColor(Lang.plain("bossbar-color-start")));
			this.spawnMobs();

			this.arena.getSettings().getPhaseStartCommands().run(this.arena, this.arena.getPlayers(), Settings.Arena.CONSOLE_CMD_FOREACH);
		}

		{ // Rewards
			this.giveRewards(this.arena.getSettings().getRewardsOnWave(), true);
			this.giveRewards(this.arena.getSettings().getRewardsEveryWave(), false);
		}

		// Decoration
		this.arena.getMessenger().broadcastBar(Lang.legacy("bossbar-next-phase", "phase", this.phase + ""));
		this.arena.getMessenger().playSound(CompSound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR, 0.1F);

		this.checkMaxPhase();
	}

	private void giveRewards(Map<Integer, List<Object>> rewards, boolean phaseEquals) {
		for (final Entry<Integer, List<Object>> entry : rewards.entrySet()) {
			final int phaseTrigger = entry.getKey();

			if (phaseEquals && this.phase != phaseTrigger || !phaseEquals && this.phase % phaseTrigger != 0)
				continue;

			for (final Object raw : entry.getValue())
				for (final Player online : this.arena.getPlayers())
					if (raw instanceof String) {
						String command = this.arena.getMessenger().replaceVariables(raw.toString().substring(1));
						command = Variables.builder().audience(online).replaceLegacy(command);

						Platform.dispatchConsoleCommand(Platform.toPlayer(online), command);

					} else if (raw instanceof Triple) {
						final Triple<CompMaterial, Integer, String> triple = (Triple<CompMaterial, Integer, String>) raw;

						Valid.checkNotNull(triple.getFirst(), "Material cannot be null for " + raw + " in " + entry);
						Valid.checkNotNull(triple.getSecond(), "Quantity cannot be null for " + raw + " in " + entry);

						final String requiredClass = triple.getThird();

						if (!requiredClass.isEmpty()) {
							final ClassCache classCache = CoreArenaPlugin.getDataFor(online).getClassCache();

							if (classCache.assignedClass == null || !classCache.assignedClass.getName().equalsIgnoreCase(requiredClass))
								continue;
						}

						online.getInventory().addItem(triple.getFirst().toItem(triple.getSecond()));
					}
		}
	}

	private void checkMaxPhase() {
		final int max = this.arena.getSettings().getMaxPhase();

		if (max != -1)
			if (this.phase == max) {
				this.arena.getMessenger().broadcast(Lang.component("phase-max-text"));
				SimpleSound.fromString(Lang.plain("phase-max-sound")).play(this.arena.getPlayers());

				if (this.countdown.isRunning())
					this.countdown.cancel();

				this.resetBossbar(this.convertStringToColor(Lang.plain("bossbar-color-end")));
			}
	}

	private void giveExp() {
		for (final Player player : this.arena.getPlayers()) {

			final int points = this.arena.getSettings().getPhaseExp(this.phase);
			CoreArenaPlugin.getDataFor(player).getArenaCache().giveAndShowExp(player, points);

			this.arena.getMessenger().tell(player, Lang.component("experience-next-phase", "amount", points + "", "phase", this.phase + ""));
		}
	}

	private void checkPvP() {
		final ArenaSettings settings = this.arena.getSettings();

		if (settings.getPvpPhase() != 1 && this.phase == settings.getPvpPhase())
			this.arena.getMessenger().broadcast(Lang.component("arena-game-friendly-fire-activated"));
	}

	private void updatePowerSigns() {
		for (final ArenaSign sign : this.arena.getData().getSigns().getSigns(SignType.POWER)) {
			final SimpleSignPower power = (SimpleSignPower) sign;

			power.onNextPhase();
		}
	}

	private void resetBossbar(BossBar.Color color) {
		this.bossBar.updateTitle(this.getColorFromBar(color) + Lang.legacy("bossbar-title", "phase", this.phase + ""));
		this.bossBar.updateProgress(1F);
		this.bossBar.updateColor(color);
	}

	final class ChestRefiller {

		private final ArenaTrigger trigger;
		private final Map<Location, ItemStack[]> chestLocations = new HashMap<>();

		private ChestRefiller(ArenaTrigger trigger) {
			this.trigger = trigger;
		}

		private void detectChests() {
			this.chestLocations.clear();

			if (SimplePhaseIncremental.this.arena.getData().getRegion().isComplete())
				for (final Block block : SimplePhaseIncremental.this.arena.getData().getRegion().getBlocks())
					if (block.getState() instanceof Chest) {
						final Chest ch = (Chest) block.getState();

						final List<ItemStack> copy = new ArrayList<>();

						for (final ItemStack content : ch.getBlockInventory().getContents())
							copy.add(content == null ? null : content.clone());

						this.chestLocations.put(block.getLocation(), copy.toArray(new ItemStack[copy.size()]));
					}
		}

		private void refill() {
			if (!this.trigger.trigger(SimplePhaseIncremental.this.phase))
				return;

			for (final Entry<Location, ItemStack[]> entry : this.chestLocations.entrySet()) {
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
			for (final Entry<Location, ItemStack[]> entry : this.chestLocations.entrySet()) {
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
		final ArenaData data = this.arena.getData();
		final World world = this.arena.getData().getRegion().getWorld();

		final int entityLimit = this.arena.getSettings().getMobLimit();
		int totalEntities = 0;

		Debugger.debug("spawning", "#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#");
		Debugger.debug("spawning", "#-#-#-#-#-# Launching spawners for phase " + this.getCurrent() + " #-#-#-#-#-#");

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

			Debugger.debug("spawning", this.getCurrent() + " ::: Activating " + spawner);

			if (!spawner.getActivePeriod().mayExecute(this.getCurrent())) {
				Debugger.debug("spawning", "\tFAIL - spawner's active period (" + spawner.getActivePeriod() + ") does not match current phase: " + this.getCurrent());
				continue;
			}

			if (this.arena.getPlayers().size() < spawner.getMinimumPlayers()) {
				Debugger.debug("spawning", "\tFAIL - too few players. Need " + spawner.getMinimumPlayers() + " but have " + this.arena.getPlayers().size());
				continue;
			}

			if (!RandomUtil.chance(spawner.getChance())) {
				Debugger.debug("spawning", "\tFAIL - chance not hit. Chance: " + spawner.getChance() + "%");
				continue;
			}

			final Location loc = spawner.getLocation().clone().add(0, 1, 0);
			boolean inRange = false;
			final int activationRadius = this.arena.getSettings().getSpawnerActivationRadius();

			for (final Player arenaPlayer : this.arena.getPlayers())
				if (arenaPlayer.getLocation().distance(loc) <= activationRadius) {
					inRange = true;
					Debugger.debug("spawning", "\tFound " + arenaPlayer.getName() + " in range of " + Math.round(arenaPlayer.getLocation().distance(loc)) + " blocks from spawner");

					break;
				}

			if (!inRange) {
				Debugger.debug("spawning", "\tFAIL - no players in range of " + activationRadius + " blocks");

				continue;
			}

			final List<SpawnedEntity> types = Common.removeNullAndEmpty(Arrays.asList(spawner.getSpawnedTypes()));

			if (types == null || types.size() == 0) {
				Common.log("[Arena " + this.arena.getName() + "] Spawnpoint at " + SerializeUtil.serializeLocation(loc) + " has no monsters configured!");

				continue;
			}

			final List<SpawnedEntity> entitiesToSpawn = spawner.isSpawnAllSlots() ? types : Arrays.asList(RandomUtil.nextItem(types));

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

					final Location randomLoc = this.randomizeLocation(loc);
					Entity spawned = null;

					Debugger.debug("\t", "Custom ? " + spawnedEntity.isCustom());

					if (spawnedEntity.isCustom()) {
						if (HookManager.isMythicMobsLoaded() && CoreHookManager.tryMythicalSpawn(spawnedEntity.getCustomItem(), randomLoc)) {
							Debugger.debug("spawning", "\t\tSUCCESS - handled in MythicMobs");

							continue;
						}

						if (HookManager.isBossLoaded())
							spawned = CoreHookManager.tryBOSSSpawn(spawnedEntity.getCustomItem(), randomLoc);

						if (spawned == null) {
							final String customType = CompMetadata.getMetadata(spawnedEntity.getCustomItem(), "CoreArenaMob");
							final EntityType eggMob = customType != null ? CompEntityType.fromName(customType) : CompMonsterEgg.lookupEntity(spawnedEntity.getCustomItem());

							Debugger.debug("spawning", "\tTrying from custom egg (corearena tag = " + customType + ", detected mob = " + eggMob + ")");

							if (eggMob != null) {
								spawned = randomLoc.getWorld().spawnEntity(randomLoc, eggMob);

								Debugger.debug("spawning", "\t\tSUCCESS - spawning from custom egg - " + eggMob);
							} else
								Debugger.debug("spawning", "\t\tFAIL - unknown custom egg = " + spawnedEntity);
						}
					}

					if (spawned == null) {
						final String debugMessage = "spawning " + spawnedEntity.getType() + " at " + SerializeUtil.serializeLocation(randomLoc);

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

					Valid.checkNotNull(spawned, "Failed to spawn " + spawnedEntity + " in " + this.arena.getName());

					if (spawned instanceof Creature) {
						final Player target = ((SimpleArena) this.arena).getNearestPlayer(spawned.getLocation());

						if (target != null)
							((Creature) spawned).setTarget(target);
					}
				}
			}
		}
	}

	private boolean validLocation(Location loc) {
		final Arena locArena = CoreArenaPlugin.getArenaManager().findArena(loc);
		if (locArena == null || !locArena.equals(this.arena))
			return false;

		final Block center = loc.getBlock();

		for (int x = -1; x <= 1; x++) {
			for (int y = 0; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					Block check = center.getRelative(x, y, z);
					if (check.getType() != Material.AIR)
						return false;
				}
			}
		}

		return true;
	}

	private Location randomizeLocation(Location loc) {
		final int spread = this.arena.getSettings().getMobSpread();

		if (spread <= 0) {
			Debugger.debug("spawning", "Not spreading spawn location for monsters in " + this.arena.getName() + " since Mob_Spread is set to 0.");
			return loc;
		}

		Debugger.debug("spawning", "Spreading spawn location for monsters in " + this.arena.getName() + ". Mob_Spread = " + spread);

		final int tries = 20;

		Location newLoc = loc;

		for (int i = 0; i < tries; i++) {
			newLoc = newLoc.clone().add(this.getPositiveOrNegRandom() * Math.random() * (1 + RandomUtil.nextInt(spread)), 0, this.getPositiveOrNegRandom() * Math.random() * (1 + RandomUtil.nextInt(spread)));

			if (validLocation(newLoc))
				return newLoc;
		}

		return newLoc.add(0.5, 0, 0.5);
	}

	private int getPositiveOrNegRandom() {
		return RandomUtil.nextBoolean() ? 1 : -1;
	}

	public void onPlayerLeave(Player player) {
		this.bossBar.hideFrom(player);
	}

	@Override
	public void startTimer() {
		this.countdown.launch();

		try {
			this.spawnMobs();

		} catch (final Throwable t) {
			Common.error(t,
					"Error while spawning mobs in " + this.arena.getName(),
					"Error: {error}",
					"Stopping arena for safety.");

			this.arena.stopArena(StopCause.INTERRUPTED_ERROR);

			return;
		}

		for (final Player player : this.arena.getPlayers())
			this.bossBar.showTo(player);

		{ // Rewards: Support 1st wave
			this.giveRewards(this.arena.getSettings().getRewardsOnWave(), true);
			this.giveRewards(this.arena.getSettings().getRewardsEveryWave(), false);
		}
	}

	@Override
	public void stopAndReset() {
		if (this.countdown.isRunning())
			this.countdown.cancel();

		this.phase = 1;
		this.waitedBetweenNextPhase = 0;

		this.resetBossbar(this.convertStringToColor(Lang.plain("bossbar-color-start")));
		this.bossBar.hide();
	}
}