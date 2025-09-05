package org.mineacademy.corearena.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.data.AllData;
import org.mineacademy.corearena.exp.ExpFormula;
import org.mineacademy.corearena.hook.CoreHookManager;
import org.mineacademy.corearena.model.ArenaCommands;
import org.mineacademy.corearena.model.ArenaData;
import org.mineacademy.corearena.model.ArenaMaterialAllower;
import org.mineacademy.corearena.model.ArenaMaterialAllower.AllowMode;
import org.mineacademy.corearena.model.ArenaSettings;
import org.mineacademy.corearena.model.ArenaTrigger;
import org.mineacademy.corearena.settings.Settings;
import org.mineacademy.corearena.type.NextPhaseMode;
import org.mineacademy.corearena.util.Constants;
import org.mineacademy.fo.CommonCore;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.MinecraftVersion.V;
import org.mineacademy.fo.TimeUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.model.HookManager;
import org.mineacademy.fo.model.SimpleSound;
import org.mineacademy.fo.model.SimpleTime;
import org.mineacademy.fo.model.Triple;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.settings.YamlConfig;

import lombok.Getter;

@Getter
public final class SimpleSettings extends YamlConfig implements ArenaSettings {

	private final ArenaData dataSection;

	// Generic
	private int lifes;
	private int minimumTier;

	private int killHeight;

	private boolean allowOwnEquipment;
	private boolean allowNaturalDrops;
	private boolean respawningRandom;
	private boolean mobBurn;
	private boolean openClassMenu;
	private boolean explosiveArrowPlayerDamage;
	private boolean proceduralDamage;
	private boolean disableHealthRegen;

	// Items
	private boolean igniteTnts;
	private boolean spawnWolves;
	private boolean launchFireballs;

	// Limits
	private int minimumPlayers;
	private int maximumPlayers;
	private int mobLimit;
	private int mobSpread;

	// Duration
	private int lobbyDurationSeconds;
	private int arenaDurationSeconds;
	private int phaseDurationSeconds;

	// Allowers
	private ArenaMaterialAllower breakingList;
	private ArenaMaterialAllower placeList;
	private ArenaMaterialAllower repairBlacklist;

	// Per phase
	private NextPhaseMode nextPhaseMode;
	private int nextPhaseWaitSeconds;

	private ArenaTrigger chestRefill;
	private int pvpPhase;
	private int lastPhase;
	private int autoStopPlayersLimit;
	private int endPhaseNoMonsters;
	private int maxPhase;
	private int spawnerActivationRadius;

	// Commands
	private ArenaCommands startCommands, lobbyStartCommands, phaseStartCommands, lastPhaseCommands, endCommands, finishCommands, playerLeaveCommands;

	// Experience overrides
	private ExpFormula expPhase = null, expMob = null;
	private final Map<String, ExpFormula> expMobs = new HashMap<>();

	// Rewards
	private Map<Integer /*wave*/, List<Object> /**/> rewardsEveryWave, rewardsOnWave;

	// Fart noises
	private SimpleSound playerJoinSound;
	private SimpleSound playerLeaveSound;
	private SimpleSound arenaStartSound;

	private SimpleTime endCommandsDelay;

	public SimpleSettings(File file) {
		this(file.getName().replace(".yml", ""));
	}

	public SimpleSettings(String arenaName) {
		this.dataSection = AllData.getInstance().loadArenaData(arenaName);

		this.setHeader(Constants.Header.ARENA_FILE);
		this.loadAndExtract("prototype/arena.yml", "arenas/" + arenaName + ".yml");
	}

	@Override
	protected void onLoad() {
		this.lifes = this.getInteger("Lifes");
		this.disableHealthRegen = this.getBoolean("Disable_Health_Regen", false);
		this.minimumTier = this.getInteger("Required_Class_Tier");
		this.killHeight = this.isSet("Kill_Height") ? this.getInteger("Kill_Height") : -1;
		this.allowOwnEquipment = this.getBoolean("Allow_Own_Equipment");
		this.allowNaturalDrops = this.getBoolean("Natural_Drops");
		this.respawningRandom = this.getBoolean("Random_Respawn_Location");
		this.mobBurn = this.getBoolean("Mob_Burn_On_Sunlight");
		this.mobSpread = this.getInteger("Mob_Spread");
		this.openClassMenu = this.getBoolean("Open_Class_Menu");
		this.explosiveArrowPlayerDamage = this.getBoolean("Explosive_Arrows_Damage_Players");
		this.proceduralDamage = this.getBoolean("Procedural_Damage");
		this.nextPhaseMode = this.get("Next_Phase_Mode", NextPhaseMode.class);
		this.nextPhaseWaitSeconds = (int) (TimeUtil.toTicks(this.getString("Next_Phase_Wait")) / 20);
		this.rewardsEveryWave = this.loadWaves("Rewards.Every");
		this.rewardsOnWave = this.loadWaves("Rewards.At");
		this.playerJoinSound = this.get("Sound.Player_Join", SimpleSound.class);
		this.playerLeaveSound = this.get("Sound.Player_Leave", SimpleSound.class);
		this.arenaStartSound = this.get("Sound.Arena_Start", SimpleSound.class);
		this.spawnerActivationRadius = this.getInteger("Spawner_Activation_Radius");
		this.endCommandsDelay = this.getTime("End_Commands_Delay", SimpleTime.fromSeconds(0));

		this.loadLimits();
		this.loadDurations();
		this.loadInteraction();
		this.loadPhases();
		this.loadCommands();
		this.loadExp();
	}

	@SuppressWarnings("rawtypes")
	private Map<Integer, List<Object>> loadWaves(String path) {
		final Map<Integer, List<Object>> map = new HashMap<>();

		if (this.isSet(path))
			for (final Map.Entry<Integer, List> entry : this.getMap(path, Integer.class, List.class).entrySet()) {
				final int wave = entry.getKey();
				final List<Object> parsed = new ArrayList<>();

				for (final Object rawObject : entry.getValue()) {
					final String raw = rawObject.toString();

					// Detected command
					if (raw.startsWith("/"))
						parsed.add(raw);
					else {
						final String[] parts = raw.split(":");

						if (!(parts.length == 1 || parts.length == 2 || parts.length == 3))
							throw new FoException("For reward items, please use formatting 'material_name' or 'material_name:amount' or 'material_name:amount:class'. Given: " + raw, false);

						final CompMaterial material = CompMaterial.fromString(parts[0]);
						if (material == null)
							throw new FoException("Unable to find a reward from material: " + parts[0] + " - is this a valid Bukkit Material?", false);

						final int amount = parts.length == 2 || parts.length == 2 ? Integer.parseInt(parts[1]) : 1;
						final String requiredClass = parts.length == 3 ? parts[2] : "";

						parsed.add(new Triple<>(material, amount, requiredClass));
					}
				}

				map.put(wave, parsed);
			}

		return Collections.unmodifiableMap(map);
	}

	private void loadLimits() {
		this.setPathPrefix("Player_Limit");

		this.minimumPlayers = MathUtil.range(this.getInteger("Minimum"), 1, 100);
		this.maximumPlayers = MathUtil.range(this.getInteger("Maximum"), 1, 100);

		this.setPathPrefix(null);
		this.mobLimit = MathUtil.range(this.getInteger("Mob_Limit"), 0, 800);
	}

	private void loadDurations() {
		this.setPathPrefix("Duration");

		this.lobbyDurationSeconds = (int) MathUtil.range(TimeUtil.toTicks(this.getString("Lobby")) / 20, CoreArenaPlugin.DEBUG_EDITING_MODE ? 1 : 10, 1800);
		this.arenaDurationSeconds = (int) MathUtil.range(TimeUtil.toTicks(this.getString("Arena")) / 20, CoreArenaPlugin.DEBUG_EDITING_MODE ? 1 : 10, 7200);
		this.phaseDurationSeconds = (int) MathUtil.range(TimeUtil.toTicks(this.getString("Phase")) / 20, CoreArenaPlugin.DEBUG_EDITING_MODE ? 1 : 10, 7200);
	}

	private void loadInteraction() {
		this.setPathPrefix("Interaction");

		this.breakingList = this.getAllower("Allow_Breaking");
		this.placeList = this.getAllower("Allow_Placement");
		this.repairBlacklist = this.getAllower("Disallow_Auto_Repair");
		this.igniteTnts = this.getBoolean("Ignite_Tnts");
		this.spawnWolves = this.getBoolean("Spawn_Wolves");
		this.launchFireballs = this.getBoolean("Launch_Fireballs");
	}

	private void loadPhases() {
		this.setPathPrefix("Phase");

		this.pvpPhase = this.getInteger("PvP");
		this.chestRefill = this.getTrigger("Chest_Refill");
		this.lastPhase = this.getInteger("Arena_End");
		this.autoStopPlayersLimit = this.getInteger("Players_End");
		this.endPhaseNoMonsters = this.getInteger("Arena_End_No_Monsters");
		this.maxPhase = this.getInteger("Max_Phase");

		if (this.lastPhase != -1 && this.maxPhase != -1) {
			this.maxPhase = -1;

			CommonCore.logFramed("If Arena_End is set, Max_Phase must be -1 in " + this.getName() + ".yml");
		}
	}

	private void loadCommands() {
		this.startCommands = this.prepareCommands("Start");
		this.lobbyStartCommands = this.prepareCommands("Lobby_Start");
		this.phaseStartCommands = this.prepareCommands("Phase");
		this.lastPhaseCommands = this.prepareCommands("Last_Phase");
		this.finishCommands = this.prepareCommands("Finish");
		this.endCommands = this.prepareCommands("End");
		this.playerLeaveCommands = this.prepareCommands("Player_Leave");
	}

	private void loadExp() {
		this.setPathPrefix("Experience");

		this.expMobs.clear();
		this.expPhase = this.isSet("Next_Phase") ? new ExpFormula(this.getString("Next_Phase")) : null;

		if (this.isSet("Kill"))
			for (final Entry<String, Object> e : this.getMap("Kill").asMap().entrySet()) {
				final ExpFormula formula = new ExpFormula(e.getValue().toString());

				if ("global".equalsIgnoreCase(e.getKey()))
					this.expMob = formula;
				else
					this.expMobs.put(e.getKey().toLowerCase(), formula);
			}
	}

	private ArenaCommands prepareCommands(String path) {
		this.setPathPrefix("Commands." + path);

		final List<String> playerCmds = this.getStringList("Player");
		final List<String> playerConsoleCmds = this.getStringList("Player_Console");
		final List<String> consoleCmds = this.getStringList("Console");

		return new ArenaCommands(playerCmds, playerConsoleCmds, consoleCmds);
	}

	private ArenaMaterialAllower getAllower(String path) {
		final Object obj = this.getObject(path);

		if (obj == null)
			return new ArenaMaterialAllower(AllowMode.NONE);

		if (obj instanceof String && ((String) obj).equals("*"))
			return new ArenaMaterialAllower(AllowMode.ALL);

		if (obj instanceof List)
			return new ArenaMaterialAllower(this.getList(path, Material.class));

		throw new FoException("Please set either '*' or a list of materials at " + path + " in your " + this.getName() + ".yml file.", false);
	}

	private ArenaTrigger getTrigger(String path) {
		final Object obj = this.getObject(path);

		if (obj == null || obj instanceof Integer && ((Integer) obj).equals(-1))
			return null;

		if (obj instanceof Integer)
			return new ArenaTrigger((int) obj);

		if (obj instanceof List) {
			final Set<Integer> copy = new HashSet<>();

			for (final Object key : (List<?>) obj) {
				Valid.checkBoolean(key instanceof Integer, "Please only insert numbers at " + path + " in " + this.getName() + ", not '" + key + "' (" + key.getClass() + ")");

				copy.add((int) key);
			}

			return new ArenaTrigger(copy);
		}

		throw new FoException("Error parsing " + path + " in " + this.getName() + ".yml, got " + obj + " of class " + obj.getClass());
	}

	@Override
	public boolean allowOwnEquipment() {
		return this.allowOwnEquipment;
	}

	@Override
	public boolean allowNaturalDrops() {
		return this.allowNaturalDrops;
	}

	@Override
	public boolean allowMonstersBurn() {
		return this.mobBurn;
	}

	@Override
	public boolean igniteTnts() {
		return this.igniteTnts;
	}

	@Override
	public boolean spawnWolves() {
		return this.spawnWolves;
	}

	@Override
	public boolean launchFireballs() {
		return this.launchFireballs;
	}

	@Override
	public boolean openClassMenu() {
		return this.openClassMenu;
	}

	@Override
	public boolean explosiveArrowPlayerDamage() {
		return this.explosiveArrowPlayerDamage;
	}

	@Override
	public boolean hasProceduralDamage() {
		return this.proceduralDamage && Settings.ProceduralDamage.ENABLED && HookManager.isWorldEditLoaded() && MinecraftVersion.atLeast(V.v1_13);
	}

	@Override
	public boolean disableHealthRegen() {
		return this.disableHealthRegen;
	}

	@Override
	public int getPhaseExp(int currentPhase) {
		return (this.expPhase != null ? this.expPhase : Settings.Experience.Amount.NEXT_PHASE).calculate(currentPhase);
	}

	@Override
	public void deleteFile() {
		this.getFile().delete();
	}

	@Override
	public int getExpFor(Entity entity, int currentPhase) {
		final String bossName = CoreHookManager.getBossName(entity);
		final String ename = (bossName != null ? bossName : entity.getType().toString()).toLowerCase();

		return (this.expMobs.containsKey(ename) ? this.expMobs.get(ename) : this.expMob != null ? this.expMob : Settings.Experience.Amount.getExpFor(entity)).calculate(currentPhase);
	}

	@Override
	public String getName() {
		return this.getFileName();
	}
}
