package org.mineacademy.game.util;

import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;

public final class Constants {

	public static final class NBT {
		public final static String KA_NBT = "KaNbt";
	}

	public static final class Duration {
		public final static int MENU_TITLE = 20;
	}

	public static final class RegionVisualizer {
		public final static int VERTICAL_GAP = 1;
		public final static int HORIZONTAL_GAP = 1;
	}

	public static final class Costs {
		public final static String TIER_STARTING_COST = "20 * (2 ^ {currentTier})";
		public final static int REWARD_STARTING_COST = 10;
	}

	public static final class Symbols {
		public static final String CHAT_EVENT_MESSAGE = "%2$s";
		public static final String[] POWER_SIGN_FORMAT = new String[] {
				"[&lPower&r]",
				"{type}"
		};
	}

	public static final class Folder {
		public static final String ARENAS = "arenas/";
		public static final String CLASS = "classes/";
		public static final String UPGRADES = "upgrades/";
	}

	public static final class Items {
		public static final String EXP_ITEM_TAG = "CoreExpDrop_" + CoreArenaPlugin.getNamed();
		public static final ItemStack MENU_FILLER = ItemCreator.of(CompMaterial.fromLegacy("STAINED_GLASS_PANE", 8)).name(" ").build().make();
		public static final ItemStack DEFAULT_ICON = new ItemStack(CompMaterial.WHITE_STAINED_GLASS.getMaterial());
	}

	public static final class Header {
		public static final String[] ARENA_FILE = new String[] {
				" -------------------------------------------------------",
				" Welcome to the arena settings file. Here you can setup",
				" individual arena properties.",
				" -------------------------------------------------------",
				""
		};
		public static final String[] CLASS_FILE = new String[] {
				" -------------------------------------------------------",
				" Welcome to the class settings file. Here you can setup",
				" which items the player receive after the arena starts.",
				" -------------------------------------------------------",
				""
		};
		public static final String[] UPGRADE_FILE = new String[] {
				" -------------------------------------------------------",
				" Welcome to the upgrades settings file. Here you can set",
				" additional parameters for this upgrade. Please use",
				" the in-game command to edit the upgrades otherwise.",
				" -------------------------------------------------------",
				""
		};
	}
}
