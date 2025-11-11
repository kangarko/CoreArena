package org.mineacademy.corearena.settings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.entity.Entity;
import org.mineacademy.corearena.exp.ExpFormula;
import org.mineacademy.corearena.hook.CoreHookManager;
import org.mineacademy.corearena.impl.MySQLDatabase;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.model.SimpleTime;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.settings.SimpleSettings;

@SuppressWarnings("unused")
public final class Settings extends SimpleSettings {

	@Override
	protected List<String> getUncommentedSections() {
		return Arrays.asList("Experience.Amount.Kill");
	}

	public static final class Arena {

		public static Boolean STORE_INVENTORIES = false;
		public static Boolean RESTORE_SAVED_MAX_HEALTH;
		public static Boolean GIVE_RANDOM_CLASS_IF_NOT_SELECTED;
		public static Boolean KEEP_OWN_EQUIPMENT;
		public static Boolean MOVE_FORGOTTEN_PLAYERS;
		public static Boolean AUTO_REPAIR_ITEMS;
		public static Boolean SHOW_PHASE_BAR;
		public static Boolean SHOW_HEALTH_BAR;
		public static Boolean HIDE_DEATH_MESSAGES;
		public static Boolean CONSOLE_CMD_FOREACH;
		public static Set<String> ALLOWED_COMMANDS;

		private static void init() {
			setPathPrefix("Arena");

			STORE_INVENTORIES = getBoolean("Store_Inventories");
			RESTORE_SAVED_MAX_HEALTH = getBoolean("Restore_Saved_Max_Health");
			GIVE_RANDOM_CLASS_IF_NOT_SELECTED = getBoolean("Give_Random_Class_If_Not_Selected");
			KEEP_OWN_EQUIPMENT = getBoolean("Keep_Own_Equipment_On_Death");
			MOVE_FORGOTTEN_PLAYERS = getBoolean("Move_Forgotten_Players");
			AUTO_REPAIR_ITEMS = getBoolean("Auto_Repair_Items");
			SHOW_PHASE_BAR = getBoolean("Display_Phase_Bar");
			SHOW_HEALTH_BAR = getBoolean("Display_Mob_Health_Bar");
			HIDE_DEATH_MESSAGES = getBoolean("Hide_Death_Messages");
			CONSOLE_CMD_FOREACH = getBoolean("Console_Commands_For_Each");
			ALLOWED_COMMANDS = new HashSet<>(getStringList("Allowed_Commands"));
		}

		public static final class Chat {

			public static Boolean ENABLED;
			public static Boolean RANGED;
			public static String FORMAT;
			public static String GLOBAL_FORMAT;

			private static void init() {
				setPathPrefix("Arena.Chat");

				ENABLED = getBoolean("Enabled");
				RANGED = getBoolean("Ranged");
				FORMAT = getString("Format");
				GLOBAL_FORMAT = getString("Global_Format");

			}
		}

		public static final class Integration {

			public static Boolean MCMMO_BLOCK_EXP, JOBS_BLOCK_LEVELUP;

			private static void init() {
				setPathPrefix("Arena.Integration");

				MCMMO_BLOCK_EXP = getBoolean("Block_McMMO_Experience");
				JOBS_BLOCK_LEVELUP = getBoolean("Block_Jobs_Level_Up");
			}
		}
	}

	public static final class Experience {

		public static Boolean GIVE_TO_KILLER;
		public static Double EXP_PER_LEVEL;
		public static Double LEVEL_TO_NUGET_CONVERSION_RATIO;

		public static Boolean REWARD_ESCAPE;

		public static CompMaterial ITEM;
		public static String ITEM_LABEL;

		private static void init() {
			setPathPrefix("Experience");

			GIVE_TO_KILLER = getBoolean("Give_To_Killer");
			EXP_PER_LEVEL = getDouble("Exp_Per_Level");
			LEVEL_TO_NUGET_CONVERSION_RATIO = getDouble("Level_To_Nugget_Conversion_Ratio");
			REWARD_ESCAPE = getBoolean("Reward_On_Escape");

			final String itemRaw = getString("Item");
			ITEM = !itemRaw.isEmpty() && !"none".equals(itemRaw) ? CompMaterial.fromString(itemRaw) : CompMaterial.AIR;

			if (ITEM == null)
				ITEM = CompMaterial.BLUE_DYE;

			ITEM_LABEL = getString("Item_Label");
		}

		public static final class Gold {

			public static Integer CONVERSION_RATIO;
			public static String CURRENCY_NAME;

			private static void init() {
				setPathPrefix("Experience.Gold");

				CONVERSION_RATIO = getInteger("Conversion_Ratio");
				CURRENCY_NAME = getString("Currency_Backup_Name");
			}
		}

		public static final class Amount {

			public static ExpFormula NEXT_PHASE;
			private static ExpFormula MOB_KILL_GLOBAL;
			private static Map<String, ExpFormula> MOB_KILL;

			private static void init() {
				setPathPrefix("Experience.Amount");

				NEXT_PHASE = new ExpFormula(getString("Next_Phase"));
				MOB_KILL = new HashMap<>();

				for (final Entry<String, Object> e : getMap("Kill", String.class, Object.class).entrySet()) {

					final String entityRaw = e.getKey();
					final ExpFormula formula = new ExpFormula(e.getValue().toString());

					if ("global".equals(entityRaw.toLowerCase())) {
						MOB_KILL_GLOBAL = formula;

						continue;
					}

					MOB_KILL.put(e.getKey().toLowerCase(), formula);
				}
			}

			public static ExpFormula getExpFor(Entity entity) {
				final String bossName = CoreHookManager.getBossName(entity);
				final String name = (bossName != null ? bossName : entity.getType().toString()).toLowerCase();

				return MOB_KILL.containsKey(name) ? MOB_KILL.get(name) : MOB_KILL_GLOBAL;
			}
		}
	}

	public static final class ProceduralDamage {

		public static Boolean ENABLED;

		private static void init() {
			setPathPrefix("Procedural_Damage");

			ENABLED = getBoolean("Enabled");
		}

		public static final class Explosions {

			public static Float POWER_CREEPER, POWER_TNT;
			public static Double DAMAGE_RADIUS, DAMAGE_DAMAGE;
			public static Integer GRAVITATION_RADIUS_CHECK;

			private static void init() {
				setPathPrefix("Procedural_Damage.Explosions");
				GRAVITATION_RADIUS_CHECK = getInteger("Gravitation_Range");

				setPathPrefix("Procedural_Damage.Explosions.Power");
				POWER_CREEPER = getDouble("Creeper").floatValue();
				POWER_TNT = getDouble("TnT").floatValue();

				setPathPrefix("Procedural_Damage.Explosions.Damage");
				DAMAGE_RADIUS = Double.parseDouble(getObject("Radius").toString());
				DAMAGE_DAMAGE = Double.parseDouble(getObject("Damage").toString());
			}
		}
	}

	public static final class Items {

		public static Boolean DAMAGE_PLAYERS;

		private static void init() {
			setPathPrefix("Items");

			DAMAGE_PLAYERS = getBoolean("Damage_Players");
		}

		public static final class ExplosiveBow {

			public static Float DAMAGE = 0.0F;

			private static void init() {
				setPathPrefix("Items.Explosive_Bow");

				DAMAGE = getDouble("Damage").floatValue();
			}
		}
	}

	public static final class Rewards {
		public static Boolean ALLOW_TIER_SKIP, ENABLE_MATERIAL_REWARDS;
		public static CompMaterial ITEMS, BLOCKS, PACKS;

		private static void init() {
			setPathPrefix("Rewards");

			ALLOW_TIER_SKIP = getBoolean("Allow_Skipping_Tier");
			ENABLE_MATERIAL_REWARDS = getBoolean("Enable_Material_Rewards");

			setPathPrefix("Rewards.Menu_Items");

			ITEMS = get("Items", CompMaterial.class);
			BLOCKS = get("Blocks", CompMaterial.class);
			PACKS = get("Packs", CompMaterial.class);
		}
	}

	public static final class Signs {

		public static Boolean ALLOW_CLASSES_SIGN;
		public static List<String> JOIN_SIGN_FORMAT;
		public static List<String> LEAVE_SIGN_FORMAT;
		public static List<String> CLASS_SIGN_FORMAT;
		public static List<String> UPGRADES_SIGN_FORMAT;

		private static void init() {
			setPathPrefix("Signs");

			ALLOW_CLASSES_SIGN = getBoolean("Allow_Classes_Sign");
			JOIN_SIGN_FORMAT = getStringList("Join_Sign_Format");
			LEAVE_SIGN_FORMAT = getStringList("Leave_Sign_Format");
			CLASS_SIGN_FORMAT = getStringList("Class_Sign_Format");
			UPGRADES_SIGN_FORMAT = getStringList("Upgrades_Sign_Format");
		}

		public static final class Label {
			public static String UPGRADES, CLASS, POWER, LEAVE, CLASSES, REWARDS;

			private static void init() {
				setPathPrefix("Signs.Label");

				UPGRADES = getString("Upgrade");
				CLASS = getString("Class");
				POWER = getString("Power");
				LEAVE = getString("Leave");
				CLASSES = getString("Classes");
				REWARDS = getString("Rewards");
			}
		}
	}

	public static final class WorldEdit {

		public static Integer BLOCK_BULK_RESTORE_AMOUNT;
		public static SimpleTime WAIT_PERIOD;
		public static Boolean CLOSE_STREAM;

		private static void init() {
			setPathPrefix("WorldEdit");

			BLOCK_BULK_RESTORE_AMOUNT = getInteger("Block_Bulk_Restore_Amount");
			WAIT_PERIOD = getTime("Wait_Period");
			CLOSE_STREAM = getBoolean("Close_Stream");

			if (isSet("Alternative_Restore"))
				set("Alternative_Restore", null);

			if (isSet("Execution_Limit"))
				set("Execution_Limit", null);
		}
	}

	public static class MySQL {
		// For security reasons, no sensitive information are stored here.

		public static Boolean ENABLED;
		public static Integer DELAY_TICKS;

		// Prevent obfuscator removing "unused" method
		public static final void dummyCall() {
		}

		private static void init() {
			setPathPrefix("MySQL");

			ENABLED = getBoolean("Enabled");
			DELAY_TICKS = getInteger("Delay_Ticks");

			final String host = getString("Host");
			final String database = getString("Database");
			final String port = getString("Port");

			final String user = getString("User");
			final String password = getString("Password");

			final String line = getString("Connector_Advanced");

			if (ENABLED) {
				Common.log("Connecting to MySQL database...");

				MySQLDatabase.setInstance(line.replace("{host}", host).replace("{database}", database).replace("{port}", port), user, password);
			}
		}
	}

	public static class Backup {

		public static SimpleTime FREQUENCY;

		private static void init() {
			setPathPrefix("Backup");

			FREQUENCY = getTime("Frequency");
		}
	}

	private static void init() {
		setPathPrefix(null);
	}
}
