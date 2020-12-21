package org.mineacademy.game.settings;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.mineacademy.fo.ReflectionUtil;
import org.mineacademy.fo.model.Replacer;
import org.mineacademy.fo.model.SimpleSound;
import org.mineacademy.fo.settings.SimpleLocalization;
import org.mineacademy.fo.settings.YamlConfig.CasusHelper;
import org.mineacademy.fo.settings.YamlConfig.TitleHelper;
import org.mineacademy.game.model.ArenaBarColor;

@SuppressWarnings("unused")
public final class Localization extends SimpleLocalization {

	@Override
	protected int getConfigVersion() {
		return 1;
	}

	// --------------------------------------------------------------------------------------------------------
	// The actual implementation
	// --------------------------------------------------------------------------------------------------------

	private static void init() {
	}

	public static final class Edit {
		public static String SAVED_CHANGES;

		private static void init() {
			pathPrefix("Edit");
			SAVED_CHANGES = getOrSetDefault("Saved_Changes", "&2Your changes have been saved.");
		}

		public static final class Menu {

			public static String EDITING_PIECES;
			public static String EDITING_ITEMS;

			public static Replacer START_EDITING;

			private static void init() {
				pathPrefix("Edit.Menu");
				START_EDITING = Replacer.of(getOrSetDefault("Edited", "&eYou are editing {mode}"));
				EDITING_PIECES = getOrSetDefault("Switch_Pieces", "Click to switch to editing prices.");
				EDITING_ITEMS = getOrSetDefault("Switch_Items", "Click to switch to editing items.");
			}
		}
	}

	public static final class Arena {

		public static String CANNOT_DO_WHILE_EDITING;

		private static void init() {
			pathPrefix("Arena");

			CANNOT_DO_WHILE_EDITING = getString("Action_Denied_While_Editing");
		}

		public static class Error {
			public static String NOT_FOUND;
			public static String NOT_CONFIGURED;
			public static String NOT_ENABLED;
			public static String NO_PERMISSION;
			public static String EDITED;
			public static String ALREADY_PLAYING;
			public static String ALREADY_RUNNING;
			public static String FULL;
			public static String NO_ARENA_AT_LOCATION;
			public static String INVENTORY_NOT_EMPTY;
			public static String ILLEGAL_COMMAND;

			private static void init() {
				pathPrefix("Arena.Error");

				NOT_ENABLED = getString("Not_Enabled");
				NOT_FOUND = getString("Not_Found");
				NOT_CONFIGURED = getString("Not_Ready");
				NO_PERMISSION = getString("No_Permission");
				EDITED = getString("Edited");
				ALREADY_PLAYING = getString("Already_Playing");
				ALREADY_RUNNING = getString("Already_Running");
				FULL = getString("Full");
				NO_ARENA_AT_LOCATION = getString("No_Arena_At_Location");
				INVENTORY_NOT_EMPTY = getString("Inventory_Not_Empty");
				ILLEGAL_COMMAND = getString("Cannot_Run_Command_In_Arena");
			}
		}

		public static class Setup {
			public static String CANNOT_EDIT;
			public static String CANNOT_CLONE;
			public static String CLONE_MOB_UNKNOWN;

			private static void init() {
				pathPrefix("Arena.Setup");

				CANNOT_EDIT = getString("Cannot_Edit");
				CANNOT_CLONE = getString("Cannot_Clone");
				CLONE_MOB_UNKNOWN = getString("Clone_Mob_Spawnpoint_Unknown");
			}
		}

		public static class Lobby {
			public static String JOIN_PLAYER;
			public static String JOIN_OTHERS;
			public static String START_COUNTDOWN;
			public static String FAIL_START_NOT_ENOUGH_PLAYERS;
			public static String KICK_NO_CLASS;
			public static String TIER_TOO_LOW;

			private static void init() {
				pathPrefix("Arena.Lobby");

				JOIN_PLAYER = getString("Join_Player");
				JOIN_OTHERS = getString("Join_Others");
				START_COUNTDOWN = getString("Start_Countdown");
				FAIL_START_NOT_ENOUGH_PLAYERS = getString("Not_Enough_Players");
				KICK_NO_CLASS = getString("Kick_No_Class");
				TIER_TOO_LOW = getString("Tier_Too_Low");
			}
		}

		public static class Game {
			public static String START;
			public static String END_WARNING;
			public static String END_GENERIC;
			public static String END_LAST_LEFT;
			public static String DEATH_TO_VICTIM;
			public static String DEATH_BROADCAST;
			public static String KICK_DEATH_TO_VICTIM;
			public static String KICK_DEATH_BROADCAST;
			public static String KILL_TO_VICTIM;
			public static String KILL_BROADCAST;
			public static String KICK_KILL_BROADCAST;
			public static String KICK_KILL_TO_VICTIM;
			public static String CLASS_AUTO_ASSIGNED;
			public static String FRIENDLY_FIRE_ACTIVATED;
			public static String EXPLOSIVE_ARROW_NO_AMMO;

			private static void init() {
				pathPrefix("Arena.Game");

				START = getString("Start");
				END_WARNING = getString("End_Countdown");
				END_GENERIC = getString("End");
				END_LAST_LEFT = getString("End_Last_Player");
				DEATH_TO_VICTIM = getString("Death_To_Victim");
				DEATH_BROADCAST = getString("Death_Broadcast");
				KICK_DEATH_TO_VICTIM = getString("Kick_Death_To_Victim");
				KICK_DEATH_BROADCAST = getString("Kick_Death_Broadcast");
				KILL_BROADCAST = getString("Kill_Broadcast");
				KILL_TO_VICTIM = getString("Kill_To_Victim");
				KICK_KILL_BROADCAST = getString("Kick_Kill_Broadcast");
				KICK_KILL_TO_VICTIM = getString("Kick_Kill_To_Victim");
				CLASS_AUTO_ASSIGNED = getString("Class_Auto_Assigned");
				FRIENDLY_FIRE_ACTIVATED = getString("Friendly_Fire_Activated");
				EXPLOSIVE_ARROW_NO_AMMO = getString("Explosive_Arrow_No_Ammo");
			}
		}

		public static class State {
			public static String STOPPED, LOBBY, RUNNING;

			private static void init() {
				pathPrefix("Arena.State");

				STOPPED = getString("Stopped");
				LOBBY = getString("Lobby");
				RUNNING = getString("Running");
			}
		}
	}

	public static class Experience {
		public static String NEXT_PHASE;
		public static String PICKUP;

		private static void init() {
			pathPrefix("Experience");

			NEXT_PHASE = getString("Next_Phase");
			PICKUP = getString("Pickup");
		}
	}

	public static class Upgrades {
		public static String LOCKED;
		public static String LACK_LEVELS;
		public static String LACK_SPACE;
		public static String SUCCESSFUL_PURCHASE;

		private static void init() {
			pathPrefix("Upgrades");

			LOCKED = getString("Locked");
			LACK_LEVELS = getString("Lack_Levels");
			LACK_SPACE = getString("Lack_Space");
			SUCCESSFUL_PURCHASE = getString("Successful_Purchase");
		}
	}

	public static class Bossbar {
		public static String TITLE;
		public static String NEXT_PHASE;

		public static ArenaBarColor COLOR_START, COLOR_MID, COLOR_END;

		private static void init() {
			pathPrefix("Bossbar");

			TITLE = getString("Title");
			NEXT_PHASE = getString("Next_Phase");

			pathPrefix("Bossbar.Color");

			COLOR_START = ReflectionUtil.lookupEnum(ArenaBarColor.class, getString("Start"));
			COLOR_MID = ReflectionUtil.lookupEnum(ArenaBarColor.class, getString("Mid"));
			COLOR_END = ReflectionUtil.lookupEnum(ArenaBarColor.class, getString("Near_End"));
		}
	}

	public static class Phase {

		public static String NEXT_WAIT;

		private static void init() {
			pathPrefix("Phase");

			NEXT_WAIT = getString("Next_Phase_Wait");
		}

		public static class Max {
			public static String TEXT;
			public static SimpleSound SOUND;

			private static void init() {
				pathPrefix("Phase.Max_Phase");

				TEXT = getString("Text");
				SOUND = getSound("Sound");
			}
		}
	}

	public static class Title {
		public static TitleHelper KICK;
		public static TitleHelper ESCAPE;

		private static void init() {
			pathPrefix("Title");

			KICK = getTitle("Kick");
			ESCAPE = getTitle("Escape");
		}
	}

	public static class Currency {
		private static CasusHelper NAME;
		private static ChatColor COLOR;
		public static String RECEIVED;

		private static void init() {
			pathPrefix("Currency");

			NAME = getCasus("Name");
			COLOR = ReflectionUtil.lookupEnum(ChatColor.class, getString("Color"));

			RECEIVED = getString("Received");
		}

		public static String rawPluralName() {
			return NAME.getPlural();
		}

		public static String format(final int count) {
			return COLOR.toString() + NAME.formatWithCount(count);
		}
	}

	public static class Menu {
		public static String CANNOT_OPEN_IN_ARENA;
		public static String CANNOT_OPEN_OUTSIDE_ARENA;
		public static String CANNOT_OPEN_OUTSIDE_LOBBY;
		public static String USE_CLASS_MENU;

		private static void init() {
			pathPrefix("Menu");

			CANNOT_OPEN_IN_ARENA = getString("Cannot_Open_In_Arena");
			CANNOT_OPEN_OUTSIDE_ARENA = getString("Cannot_Open_Outside_Arena");
			CANNOT_OPEN_OUTSIDE_LOBBY = getString("Cannot_Open_Outside_Lobby");
			USE_CLASS_MENU = getOrSetDefault("Use_Class_Menu", "&cPlease use the class menu to edit class upgrades");
		}

		public static class Rewards {
			public static String CLASS_NOT_CONFIGURED;

			public static String SELECT_CLASS = "Select a class";
			public static String LABEL;
			public static String ITEMS, BLOCKS, PACKS, CLASS;
			public static String CHOOSE_A_REWARD;

			public static String TIER_ALREADY_BOUGHT;
			public static String TIER_TOO_HIGH;
			//
			public static String TIER_MAXIMUM;
			public static String TIER_TOP;
			public static String TIER_NEXT;
			public static String NOT_SET_HELMET;
			public static String NOT_SET_CHESTPLATE;
			public static String NOT_SET_LEGGINGS;
			public static String NOT_SET_BOOTS;
			public static String TIER_ADDED;
			public static String TIER_IS_LOCKED;

			public static String[] INFO_EDIT_PIECES;

			public static String[] INFO_EDIT_ITEMS;

			public static Replacer INFO_PURCHASE;
			public static Replacer MORE_NUGGETS_NEEDED;

			public static Replacer PURCHASED_TIER_MESSAGE;

			public static Replacer PURCHASED_TIER_TITLE;

			public static Replacer BOUGHT;

			public static List<String> ITEMS_DESCRIPTION, BLOCKS_DESCRIPTION, PACKS_DESCRIPTION, CLASS_DESCRIPTION;
			public static List<String> INFO_PLAYER, INFO_ADMIN;

			private static void init() {
				pathPrefix("Menu.Rewards");

				// Data is stored as List internally. Would throw an class-cast exception otherwise
				INFO_EDIT_ITEMS = getOrSetDefault("Info.Edit.Items", Arrays.asList("&7To edit the rewards, simply", "&emove &7items from your inventory here.")).toArray(new String[0]);
				INFO_EDIT_PIECES = getOrSetDefault("Info.Edit.Pieces", Collections.singletonList("&eClick &7an item to set its price")).toArray(new String[0]);
				INFO_PURCHASE = Replacer.of(getOrSetDefault("Info.Purchase", Arrays.asList("&7Balance: &f{balance}", "", "&7Click to make a purchase.", " ", "&7The item wil be saved to", "&7your inventory.")).toArray(new String[0]));

				CLASS_NOT_CONFIGURED = getOrSetDefault("Class_Not_Yet_Configured", "&c&oClass yet not configured!");
				SELECT_CLASS = getOrSetDefault("Select_Class", "Select a class");
				TIER_ALREADY_BOUGHT = getOrSetDefault("Tier.Already_Bought", "&4Thou already hast this tier!");
				TIER_TOO_HIGH = getOrSetDefault("Tier.Too_High", "&4This tier is too high for you!");
				CHOOSE_A_REWARD = getOrSetDefault("Choose_Reward", "Choose a Reward");
				MORE_NUGGETS_NEEDED = Replacer.of(getOrSetDefault("More_Nuggets_Needed", "&4You need {reward_cost} Nuggets for this"));
				BOUGHT = Replacer.of(getOrSetDefault("Bought_Reward", "&2Bought! Remaining: {remaining}"));

				TIER_MAXIMUM = getOrSetDefault("Tier.Maximum_Reached", "&2You have the maximum tier");
				TIER_TOP = getOrSetDefault("Tier.Top", "&cThis is the top tier");
				TIER_NEXT = getOrSetDefault("Tier.Next", "&dNext Tier ->");
				TIER_IS_LOCKED = getOrSetDefault("Tier.Locked", "&cTier is locked");
				TIER_ADDED = getOrSetDefault("Tier.Added", "&7Added Tier");
				NOT_SET_HELMET = getOrSetDefault("Not_Set.Helmet", "&7Helmet not set");
				NOT_SET_CHESTPLATE = getOrSetDefault("Not_Set.Chestplate", "&7Chestplate not set");
				NOT_SET_LEGGINGS = getOrSetDefault("Not_Set.Leggings", "&7Leggings not set");
				NOT_SET_BOOTS = getOrSetDefault("Not_Set.Boots", "&7Boots not set");

				PURCHASED_TIER_MESSAGE = Replacer.of(getOrSetDefault("Tier.Purchased", "&6You have purchased {name} Tier {tier}"));
				PURCHASED_TIER_TITLE = Replacer.of(getOrSetDefault("Tier.Purchased_Menu_Title", "&2Bought tier {tier} !"));

				ITEMS = getString("Items_Label");
				BLOCKS = getString("Blocks_Label");
				PACKS = getString("Packs_Label");
				CLASS = getString("Class_Label");

				ITEMS_DESCRIPTION = loadDescription("Items_Description");
				BLOCKS_DESCRIPTION = loadDescription("Blocks_Description");
				PACKS_DESCRIPTION = loadDescription("Packs_Description");
				CLASS_DESCRIPTION = loadDescription("Class_Description");

				INFO_PLAYER = loadDescription("Info_Player");
				INFO_ADMIN = loadDescription("Info_Admin");
				LABEL = getOrSetDefault("Label", "&8Rewards");
			}

			private static List<String> loadDescription(final String path) {
				return Arrays.asList(getString(path).split("\n"));
			}
		}
	}

	public static class Commands {
		public static String DISALLOWED_WHILE_PLAYING;

		private static void init() {
			pathPrefix("Commands");

			DISALLOWED_WHILE_PLAYING = getString("Disallowed_While_Playing");
		}

		public static class Join {
			public static String SUGGEST;

			private static void init() {
				pathPrefix("Commands.Join");

				SUGGEST = getString("Suggest");
			}
		}

		public static class Leave {
			public static String NOT_PLAYING;
			public static String SUCCESS;

			private static void init() {
				pathPrefix("Commands.Leave");

				NOT_PLAYING = getString("Not_Playing");
				SUCCESS = getString("Success");
			}
		}

		public static class Start {
			public static String NOT_LOBBY;
			public static String SUCCESS;
			public static String FAIL;

			private static void init() {
				pathPrefix("Commands.Start");

				NOT_LOBBY = getString("Not_Lobby");
				SUCCESS = getString("Success");
				FAIL = getString("Fail");
			}
		}

		public static class Stop {
			public static String ALREADY_STOPPED;
			public static String SUCCESS;

			private static void init() {
				pathPrefix("Commands.Stop");

				ALREADY_STOPPED = getString("Already_Stopped");
				SUCCESS = getString("Success");
			}
		}

		public static class Menu {
			public static String LOOKUP_FAILED;

			private static void init() {
				pathPrefix("Commands.Menu");

				LOOKUP_FAILED = getString("Lookup_Failed");
			}
		}

		public static class Nuggets {
			public static String SPECIFY_PLAYER;
			public static String INVALID_PARAM;
			public static String INVALID_AMOUNT;
			public static String BALANCE;
			public static String BALANCE_OTHER;
			public static String SET;
			public static String GAVE;
			public static String TOOK;

			private static void init() {
				pathPrefix("Commands.Nuggets");

				SPECIFY_PLAYER = getString("Specify_Player");
				BALANCE = getString("Balance");
				BALANCE_OTHER = getString("Balance_Other");
				INVALID_PARAM = getString("Invalid_Parameter");
				INVALID_AMOUNT = getString("Invalid_Amount");
				SET = getString("Set");
				GAVE = getString("Gave");
				TOOK = getString("Took");
			}
		}

		public static class Edit {
			public static String ARENA_RUNNING;
			public static String ALREADY_EDITING;
			public static String ENABLED;
			public static String DISABLED;

			private static void init() {
				pathPrefix("Commands.Edit");

				ARENA_RUNNING = getString("Arena_Running");
				ALREADY_EDITING = getString("Already_Editing");
				ENABLED = getString("Enabled");
				DISABLED = getString("Disabled");
			}
		}

		public static class Find {
			public static String WRONG_SYNTAX;
			public static String NOT_FOUND;
			public static String NOT_FOUND_LOCATION;
			public static String FOUND;
			public static String FOUND_OTHER;
			public static String FOUND_LOCATION;

			private static void init() {
				pathPrefix("Commands.Find");

				WRONG_SYNTAX = getString("Wrong_Syntax");
				NOT_FOUND = getString("Not_Found");
				NOT_FOUND_LOCATION = getString("Not_Found_Location");
				FOUND = getString("Found");
				FOUND_OTHER = getString("Found_Other");
				FOUND_LOCATION = getString("Found_Location");
			}
		}

		public static class Change_State {
			public static Replacer DISABLED;
			public static Replacer ENABLED;

			private static void init() {
				pathPrefix("Commands.Change_State");
				DISABLED = getReplacer("Disabled");
				ENABLED = getReplacer("Enabled");
			}
		}
	}

	public static class Conversation {
		public static String INVALID_INPUT;
		public static String CANCELLED;
		public static String NUMBER_HELP;

		private static void init() {
			pathPrefix("Conversation");

			INVALID_INPUT = getString("Invalid_Input");
			CANCELLED = getString("Stopped");
			NUMBER_HELP = getString("Number_Help");
		}

		public static class New {
			public static String HELP;
			public static String ALREADY_EXISTS;
			public static String NO_SPACES;
			public static String SUCCESS;
			public static String SUCCESS_ARENA;
			public static String ERROR;

			private static void init() {
				pathPrefix("Conversation.New");

				HELP = getString("Help");
				ALREADY_EXISTS = getString("Already_Exists");
				NO_SPACES = getString("No_Spaces");
				SUCCESS = getString("Success");
				SUCCESS_ARENA = getString("Success_Arena");
				ERROR = getString("Error");
			}
		}

		public static class Tier {
			public static String HELP;
			public static String PRICE_SET;

			private static void init() {
				pathPrefix("Conversation.Tier");

				HELP = getString("Help");
				PRICE_SET = getString("Price_Set");
			}
		}

		public static class Reward {
			public static String HELP;
			public static String PRICE_SET;

			private static void init() {
				pathPrefix("Conversation.Reward");

				HELP = getString("Help");
				PRICE_SET = getString("Price_Set");
			}
		}

		public static class SpawnerMinPlayers {
			public static String HELP;
			public static String SET;

			private static void init() {
				pathPrefix("Conversation.Spawner_Minimum_Players");

				HELP = getString("Help");
				SET = getString("Set");
			}
		}

		public static class SpawnerChance {
			public static String HELP;
			public static String SET;

			private static void init() {
				pathPrefix("Conversation.Spawner_Chance");

				HELP = getString("Help");
				SET = getString("Set");
			}
		}

		public static class Phase {
			public static String MATCHER;
			public static String[] HELP;
			public static String SET_FROM;
			public static String SET_ON;
			public static String SET_TILL;
			public static String SET_BETWEEN;

			private static void init() {
				pathPrefix("Conversation.Phase");

				MATCHER = getString("Input_Matcher");

				HELP = getStringArray("Help");
				SET_FROM = getString("Set_From");
				SET_ON = getString("Set_On");
				SET_TILL = getString("Set_Till");
				SET_BETWEEN = getString("Set_Between");
			}
		}
	}

	public static class Signs {
		public static String REMOVED_OUTSIDE_SIGN;
		public static String REMOVED;
		public static String OUTSIDE_ARENA;
		public static String OBJECT_NOT_FOUND;
		public static String CREATED;

		private static void init() {
			pathPrefix("Signs");

			REMOVED_OUTSIDE_SIGN = getString("Removed_Outside_Arena");
			REMOVED = getString("Removed");
			OUTSIDE_ARENA = getString("Outside_Arena");
			OBJECT_NOT_FOUND = getString("Object_Not_Found");
			CREATED = getString("Created");
		}

		public static class Arena {
			public static String[] HELP;

			private static void init() {
				pathPrefix("Signs.Arena");

				HELP = getStringArray("Help");
			}
		}

		public static class Class {
			public static String[] HELP;

			private static void init() {
				pathPrefix("Signs.Class");

				HELP = getStringArray("Help");
			}
		}

		public static class Upgrade {
			public static String[] HELP;
			public static String COST_MISSING;
			public static String COST_INVALID;

			private static void init() {
				pathPrefix("Signs.Upgrade");

				HELP = getStringArray("Help");
				COST_MISSING = getString("Cost_Missing");
				COST_INVALID = getString("Cost_Invalid");
			}
		}

		public static class Power {
			public static String[] HELP;
			public static String TYPE_INVALID;

			private static void init() {
				pathPrefix("Signs.Power");

				HELP = getStringArray("Help");
				TYPE_INVALID = getString("Type_Invalid");
			}
		}
	}

	public static class Player {
		public static String NEVER_PLAYED;
		public static String NEVER_PLAYED_ARENA;
		public static String NOT_FOUND;
		public static String NOT_ONLINE;
		public static String NOT_PLAYING;

		private static void init() {
			pathPrefix("Player");

			NEVER_PLAYED = getString("Never_Played");
			NEVER_PLAYED_ARENA = getString("Never_Played_Arena");
			NOT_FOUND = getString("Not_Found");
			NOT_ONLINE = getString("Not_Online");
			NOT_PLAYING = getString("Not_Playing");
		}
	}

	public static class Class {
		public static String SELECTED, NO_PERMISSION, NOT_AVAILABLE;

		private static void init() {
			pathPrefix("Class");

			SELECTED = getString("Selected");
			NO_PERMISSION = getString("No_Permission");
			NOT_AVAILABLE = getString("Not_Available");
		}
	}

	public static class World {
		public static String NOT_FOUND;

		private static void init() {
			pathPrefix("World");

			NOT_FOUND = getString("Not_Found");
		}
	}

	public static class Cases {
		public static CasusHelper PLAYER;
		public static CasusHelper LEVEL;
		public static CasusHelper EXP;
		public static CasusHelper NUGGET;
		public static CasusHelper HOUR;
		public static CasusHelper MINUTE;
		public static CasusHelper SECOND;
		public static CasusHelper LIFE;

		private static void init() {
			pathPrefix("Cases");

			PLAYER = getCasus("Player");
			LEVEL = getCasus("Level");
			EXP = getCasus("Exp");
			NUGGET = getCasus("Nugget");
			HOUR = getCasus("Hour");
			MINUTE = getCasus("Minute");
			SECOND = getCasus("Second");
			LIFE = getCasus("Life");
			LEVEL = getCasus("Level");
		}
	}

	public static class Parts {
		public static String COST;
		public static String USAGE;
		public static String AMOUNT;
		public static String STATE;
		public static String SIZE;
		public static String PLAYERS;
		public static String NONE;
		public static String BUY;
		public static String EDIT;
		public static String PURCHASE;
		public static String PRICE;

		private static void init() {
			pathPrefix("Parts");

			USAGE = getString("Usage");
			AMOUNT = getString("Amount");
			STATE = getString("State");
			SIZE = getString("Size");
			PLAYERS = getString("Players");
			NONE = getString("None");
			BUY = getOrSetDefault("Buy", "Buy");
			EDIT = getOrSetDefault("Edit", "Edit");
			COST = getOrSetDefault("Cost", "Cost");
			PURCHASE = getOrSetDefault("Purchase", "Purchase");
			PRICE = getOrSetDefault("Price", "Price");
		}
	}
}
