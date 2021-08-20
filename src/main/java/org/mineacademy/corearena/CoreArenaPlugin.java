package org.mineacademy.corearena;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.MinecraftVersion.V;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.debug.Debugger;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.model.HookManager;
import org.mineacademy.fo.model.Variables;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.remain.CompMonsterEgg;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.fo.settings.YamlStaticConfig;
import org.mineacademy.game.data.GeneralDataSection;
import org.mineacademy.game.hook.ArenaPAPIPlaceholder;
import org.mineacademy.game.hook.CoreHookManager;
import org.mineacademy.game.hook.JobsListener;
import org.mineacademy.game.hook.McMMOListener;
import org.mineacademy.game.hook.StackMobListener;
import org.mineacademy.game.impl.ArenaPlayer;
import org.mineacademy.game.impl.SimpleGameCommandGroup;
import org.mineacademy.game.impl.arena.SimpleArena;
import org.mineacademy.game.listener.AutoRepairListener;
import org.mineacademy.game.listener.InArenaListener;
import org.mineacademy.game.listener.PlayerListener;
import org.mineacademy.game.listener.SignsListener;
import org.mineacademy.game.manager.ClassManager;
import org.mineacademy.game.manager.RewardsManager;
import org.mineacademy.game.manager.SetupManager;
import org.mineacademy.game.manager.SimpleArenaManager;
import org.mineacademy.game.manager.UpgradesManager;
import org.mineacademy.game.menu.MenuGameTools;
import org.mineacademy.game.menu.MenuItems;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.model.ArenaManager;
import org.mineacademy.game.model.ArenaPlugin;
import org.mineacademy.game.settings.Localization;
import org.mineacademy.game.settings.Settings;
import org.mineacademy.game.type.StopCause;
import org.mineacademy.game.util.FallingLimitter;
import org.mineacademy.game.util.InventoryStorageUtil;
import org.mineacademy.game.visualize.VisualizerListener;

import lombok.Getter;
import lombok.NonNull;

public final class CoreArenaPlugin extends SimplePlugin implements ArenaPlugin {

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
	private static Map<String, ArenaPlayer> playerCache = new HashMap<>();

	private SimpleArenaManager arenaManager;

	@Getter
	private final SimpleGameCommandGroup mainCommand = new SimpleGameCommandGroup();
	private final SetupManager setupManager = new SetupManager();
	private final ClassManager classManager = new ClassManager();
	private final UpgradesManager upgradesManager = new UpgradesManager();

	private RewardsManager rewardsManager;
	private BukkitTask fallingLimitter;

	@Override
	public V getMinimumVersion() {
		return V.v1_8;
	}

	@Override
	protected String[] getStartupLogo() {
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
	protected void onPluginPreStart() {
		if (Common.doesPluginExistSilently("GameAPI")) {
			Common.logFramed(true,
					"**PLEASE REMOVE GAMEAPI**",
					"",
					"Dear user! " + getName() + " no longer requires GameAPI.",
					"Please remove it from your disk and restart the server.");

			isEnabled = false;
		}

		if (isEnabled)
			arenaManager = new SimpleArenaManager();
	}

	@Override
	protected void onPluginStart() {
		DEBUG_EDITING_MODE = Debugger.isDebugModeEnabled();

		// Setup listeners
		registerEvents(new InArenaListener());
		registerEvents(new SignsListener());
		registerEvents(new PlayerListener());
		registerEvents(new VisualizerListener());

		registerEventsIf(new AutoRepairListener(), Settings.Arena.AUTO_REPAIR_ITEMS);
		registerEventsIf(new McMMOListener(), Common.doesPluginExistSilently("mcMMO") && Settings.Arena.Integration.MCMMO_BLOCK_EXP);
		registerEventsIf(new JobsListener(), Common.doesPluginExistSilently("Jobs") && Settings.Arena.Integration.JOBS_BLOCK_LEVELUP);
		registerEventsIf(new StackMobListener(), Common.doesPluginExistSilently("StackMob"));

		// Load tools
		MenuItems.getInstance();
		MenuGameTools.getInstance();

		Settings.MySQL.dummyCall();

		CoreHookManager.loadCoreDependencies();

		if (!HookManager.isWorldEditLoaded() && MinecraftVersion.atLeast(V.v1_13))
			Common.warning("No WorldEdit detected. It is required for procedural damage and arena snapshots.");

		if (CoreHookManager.isBossLoaded() || CoreHookManager.isMythicMobsLoaded())
			CompMonsterEgg.acceptUnsafeEggs = true; // Our scientific research detected users being lazy to set this value in their config manually

		rewardsManager = new RewardsManager();

		// Register variables
		{
			Variables.addVariable("nuggets", player -> {
				final ArenaPlayer arenaPlayer = getDataFor(player);

				return arenaPlayer.getNuggets() + "";
			});

			Variables.addVariable("is_playing", player -> {
				final ArenaPlayer arenaPlayer = getDataFor(player);

				return arenaPlayer.hasArenaCache() ? "true" : "false";
			});

			Variables.addVariable("class", player -> {
				final ArenaPlayer.ClassCache arenaClass = getDataFor(player).getClassCache();

				return arenaClass.assignedClass != null ? arenaClass.assignedClass.getName() : "";
			});
		}

		// Start the rest
		startReloadable();
	}

	@Override
	protected void onPluginPreReload() {
		clearData();

		playerCache.clear();
	}

	@Override
	public void onPluginReload() {
		startReloadable();
	}

	public void startReloadable() {
		GeneralDataSection.getInstance(); // Load up

		getArenaManager().loadSavedArenas();

		classManager.loadClasses();
		upgradesManager.loadUpgrades();
		rewardsManager.loadRewards();

		if (fallingLimitter != null)
			fallingLimitter.cancel();

		fallingLimitter = new FallingLimitter().runTaskTimer(this, 10, 10);

		getArenaManager().onPostLoad();

		if (Common.doesPluginExistSilently("PlaceholderAPI"))
			new ArenaPAPIPlaceholder().register();

		Common.ADD_TELL_PREFIX = true;
	}

	@Override
	protected void onPluginStop() {
		try {
			clearData();

		} catch (final Throwable t) {
			Common.error(t, "Plugin shutdown malfunction: " + t.getClass().getSimpleName());
		}
	}

	private void clearData() {
		SimpleArena.clearRegisteredArenas();

		if (getArenaManager() != null)
			getArenaManager().stopArenas(StopCause.INTERRUPTED_RELOAD);

		for (final Arena arena : setupManager.getEditedArenas())
			setupManager.removeEditedArena(arena);

		final InventoryStorageUtil i = InventoryStorageUtil.$();

		for (final Player pl : Remain.getOnlinePlayers()) {
			final ArenaPlayer data = getDataFor(pl);

			if (Settings.Arena.STORE_INVENTORIES && i.hasStored(pl))
				i.restore(pl);

			data.removeCurrentArena();
			data.removeCurrentSetup();

			if (Menu.getMenu(pl) != null)
				pl.closeInventory();
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
	public List<Class<? extends YamlStaticConfig>> getSettings() {
		return Arrays.asList(Settings.class, Localization.class);
	}

	/*@Override
	public SpigotUpdater getUpdateCheck() {
		return new SpigotUpdater(42404);
	}*/

	@Override
	public int getFoundedYear() {
		return 2017;
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
		synchronized (playerCache) {
			if (uuid == null || name == null)
				return null;

			ArenaPlayer cache = playerCache.get(name);

			if (cache == null)
				playerCache.put(name, cache = new ArenaPlayer(uuid, name));

			return cache;
		}
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
		return (CoreArenaPlugin) SimplePlugin.getInstance();
	}
}
