package org.mineacademy.corearena;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.mineacademy.corearena.data.AllData;
import org.mineacademy.corearena.data.AllData.ArenaPlayer;
import org.mineacademy.corearena.hook.CoreHookManager;
import org.mineacademy.corearena.hook.JobsListener;
import org.mineacademy.corearena.hook.McMMOListener;
import org.mineacademy.corearena.hook.ProtectListener;
import org.mineacademy.corearena.hook.StackMobListener;
import org.mineacademy.corearena.impl.arena.SimpleArena;
import org.mineacademy.corearena.listener.AutoRepairListener;
import org.mineacademy.corearena.listener.InArenaListener;
import org.mineacademy.corearena.listener.PlayerListener;
import org.mineacademy.corearena.listener.SignsListener;
import org.mineacademy.corearena.manager.ClassManager;
import org.mineacademy.corearena.manager.RewardsManager;
import org.mineacademy.corearena.manager.SetupManager;
import org.mineacademy.corearena.manager.SimpleArenaManager;
import org.mineacademy.corearena.manager.UpgradesManager;
import org.mineacademy.corearena.menu.MenuGameTools;
import org.mineacademy.corearena.menu.MenuItems;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.model.ArenaManager;
import org.mineacademy.corearena.model.ArenaPlugin;
import org.mineacademy.corearena.settings.Settings;
import org.mineacademy.corearena.task.ArenaEscapeTask;
import org.mineacademy.corearena.type.StopCause;
import org.mineacademy.corearena.util.FallingLimitter;
import org.mineacademy.corearena.util.InventoryStorageUtil;
import org.mineacademy.corearena.visualize.VisualizerListener;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.FileUtil;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.MinecraftVersion.V;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.ButtonRemove;
import org.mineacademy.fo.model.HookManager;
import org.mineacademy.fo.model.Task;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.platform.BukkitPlugin;
import org.mineacademy.fo.remain.Remain;

import lombok.NonNull;

public final class CoreArenaPlugin extends BukkitPlugin implements ArenaPlugin {

	/**
	 * Unlocks:
	 * <ul>
	 *   <li>No longer 10 seconds limit on lobby/arena waiting</li>
	 *   <li>Disable player auto teleport to spawn on join</li>
	 *   <li>Allow toggle flight when in arena</li>
	 * </ul>
	 */
	public static boolean DEBUG_EDITING_MODE;

	// Player Name, Player Cache
	private static final Map<String, ArenaPlayer> playerCache = new HashMap<>();

	private SimpleArenaManager arenaManager;

	private final SetupManager setupManager = new SetupManager();
	private final ClassManager classManager = new ClassManager();
	private final UpgradesManager upgradesManager = new UpgradesManager();

	private RewardsManager rewardsManager;
	private Task fallingLimitter;

	@Override
	public String[] getStartupLogo() {
		return new String[] {
				"&e    ___                 _                        ",
				"&e   / __\\___  _ __ ___  /_\\  _ __ ___ _ __   __ _ ",
				"&e  / /  / _ \\| '__/ _ \\//_\\\\| '__/ _ \\ '_ \\ / _` |",
				"&6 / /__| (_) | | |  __/  _  \\ | |  __/ | | | (_| |",
				"&6 \\____/\\___/|_|  \\___\\_/ \\_/_|  \\___|_| |_|\\__,_|",
				"&8                                            " + getVersion()
		};
	}

	@Override
	protected void onPluginLoad() {
		final String platform = Platform.getPlatformName();

		if (platform.contains("Luminol") || platform.contains("Folia"))
			throw new IllegalStateException("You cannot run CoreArena plugin on " + platform + ". Please use Paper, Spigot or Purpur.");
	}

	@Override
	protected void onPluginStart() {
		this.arenaManager = new SimpleArenaManager();

		ButtonRemove.setTitle("&4&lRemove {name}");

		this.detectDebugMode();

		// Setup listeners
		this.registerEvents(new InArenaListener());
		this.registerEvents(new SignsListener());
		this.registerEvents(new PlayerListener());
		this.registerEvents(new VisualizerListener());

		if (Settings.Arena.AUTO_REPAIR_ITEMS)
			this.registerEvents(new AutoRepairListener());

		if (Platform.isPluginInstalled("mcMMO") && Settings.Arena.Integration.MCMMO_BLOCK_EXP) {
			Common.log("Hooked into: mcMMO");

			this.registerEvents(new McMMOListener());
		}

		if (Platform.isPluginInstalled("Jobs") && Settings.Arena.Integration.JOBS_BLOCK_LEVELUP) {
			Common.log("Hooked into: Jobs");

			this.registerEvents(new JobsListener());
		}

		if (Platform.isPluginInstalled("StackMob")) {
			Common.log("Hooked into: StackMob");

			this.registerEvents(new StackMobListener());
		}

		if (Platform.isPluginInstalled("Protect")) {
			Common.log("Hooked into: Protect");

			this.registerEvents(new ProtectListener());
		}

		// Load tools
		MenuItems.getInstance();
		MenuGameTools.getInstance();

		Settings.MySQL.dummyCall();

		// Run automated backups
		Platform.runTaskTimerAsync(Settings.Backup.FREQUENCY.getTimeTicks(), Settings.Backup.FREQUENCY.getTimeTicks(), () -> Platform.runTask(() -> backupData(true)));
		Platform.runTaskTimer(20 * 10, new ArenaEscapeTask());

		CoreHookManager.loadCoreDependencies();

		if (MinecraftVersion.atLeast(V.v1_13)) {
			if (!HookManager.isWorldEditLoaded())
				Common.warning("No WorldEdit detected. It is required for procedural damage and arena snapshots.");

			if (HookManager.isWorldEditLoaded() && HookManager.isWorldGuardLoaded())
				Common.warning("WorldGuard detected, make sure to set block-break flag to true in __global__ or your arena region for procedural damage to work..");

		} else if (HookManager.isWorldEditLoaded())
			Common.log("Legacy WorldEdit will be ignored. Procedural damage requires 1.13.");

		this.rewardsManager = new RewardsManager();

		// Start the rest
		this.startReloadable();
	}

	/**
	 * Loads debug mode, called automatically in {@link BukkitPlugin}
	 */
	private void detectDebugMode() {
		if (new File(this.getDataFolder(), "debug.lock").exists()) {
			DEBUG_EDITING_MODE = true;

			Bukkit.getLogger().info("Detected debug.lock file, debug features enabled!");

		} else
			DEBUG_EDITING_MODE = false;
	}

	@Override
	protected void onPluginPreReload() {
		AllData.getInstance().save();
	}

	@Override
	public void onPluginReload() {
		this.clearData();
		playerCache.clear();

		this.startReloadable();
	}

	public void startReloadable() {
		AllData.getInstance(); // Load up

		getArenaManager().loadSavedArenas();

		this.classManager.loadClasses();
		this.upgradesManager.loadUpgrades();
		this.rewardsManager.loadRewards();

		if (this.fallingLimitter != null)
			this.fallingLimitter.cancel();

		this.fallingLimitter = Platform.runTaskTimer(10, 10, new FallingLimitter());

		getArenaManager().onPostLoad();
	}

	@Override
	protected void onPluginStop() {

		AllData.getInstance().save();

		try {
			this.clearData();

		} catch (final Throwable t) {
			Common.error(t, "Plugin shutdown malfunction: " + t.getClass().getSimpleName());
		}
	}

	private void clearData() {
		SimpleArena.clearRegisteredArenas();

		if (getArenaManager() != null)
			getArenaManager().stopArenas(StopCause.INTERRUPTED_RELOAD);

		for (final Arena arena : this.setupManager.getEditedArenas())
			this.setupManager.removeEditedArena(arena);

		for (final Player online : Remain.getOnlinePlayers()) {
			final ArenaPlayer data = getDataFor(online);

			if (Settings.Arena.STORE_INVENTORIES)
				InventoryStorageUtil.getInstance().restoreIfStored(online);

			data.removeCurrentArena();
			data.removeCurrentSetup();

			if (Menu.getMenu(online) != null)
				online.closeInventory();
		}
	}

	@Override
	public ArenaManager getArenas() {
		return getArenaManager();
	}

	@Override
	public Plugin getPlugin() {
		return this;
	}

	@Override
	public int getFoundedYear() {
		return 2017;
	}

	@Override
	public String getSentryDsn() {
		return "https://c0abe3e98045cf4b85ab3bf062316a8a@o4508048573661184.ingest.us.sentry.io/4508052472659968";
	}

	@Override
	public int getBuiltByBitId() {
		return 21643;
	}

	@Override
	public String getBuiltByBitSharedToken() {
		return "UYl2VzxD9xaAiDVCnLTxTCDjD4vDuPr7";
	}

	public static ArenaPlayer getDataFor(OfflinePlayer offlinePlayer) {
		return getDataFor(offlinePlayer.getUniqueId(), offlinePlayer.getName());
	}

	public static ArenaPlayer getDataFor(@NonNull CommandSender sender) {
		return sender instanceof Player ? getDataFor((Player) sender) : getDataFor(null, sender.getName());
	}

	public static ArenaPlayer getDataFor(@NonNull Player player) {
		return getDataFor(player.getUniqueId(), player.getName());
	}

	private static ArenaPlayer getDataFor(UUID uuid, String name) {
		if (uuid == null || name == null)
			return null;

		ArenaPlayer cache = playerCache.get(name);

		if (cache == null)
			playerCache.put(name, cache = AllData.getInstance().loadPlayerData(uuid, name));

		return cache;
	}

	public static void trashDataFor(Player player) {
		Valid.checkBoolean(playerCache.containsKey(player.getName()), "Cannot trash non existing cached player " + player.getName());

		playerCache.remove(player.getName());
	}

	public static SimpleArenaManager getArenaManager() {
		return getInstance().arenaManager;
	}

	public static ClassManager getClassManager() {
		return getInstance().classManager;
	}

	public static SetupManager getSetupManager() {
		return getInstance().setupManager;
	}

	public static UpgradesManager getUpgradesManager() {
		return getInstance().upgradesManager;
	}

	public static RewardsManager getRewadsManager() {
		return getInstance().rewardsManager;
	}

	public static CoreArenaPlugin getInstance() {
		return (CoreArenaPlugin) BukkitPlugin.getInstance();
	}

	public static void backupData(boolean ifTrue) {
		final File dataFile = FileUtil.getFile("data.yml");

		if (dataFile.exists() && ifTrue) {
			final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
			final File backupFile = FileUtil.createIfNotExists("backup/" + dateFormat.format(new Date()) + "_" + dataFile.getName());
			final AllData data = AllData.getInstance();

			FileUtil.write(backupFile, data.getDataYamlConfig().saveToString());

			Common.log("Backed up data.yml to " + backupFile + ", copied sections: " + data.getDataYamlConfig().getMap("").keySet());
		}
	}
}
