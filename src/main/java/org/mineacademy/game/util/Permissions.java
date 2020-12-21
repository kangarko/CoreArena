package org.mineacademy.game.util;

public class Permissions {

	public static final class Commands {

		// Join arenas
		public static final String JOIN = "corearena.command.join.{arena}";

		// Leave arena
		public static final String LEAVE = "corearena.command.leave";

		// List available arenas
		public static final String LIST = "corearena.command.list";

		// Rewards' menu.
		public static final String REWARDS = "corearena.command.rewards";

		// Class' menu.
		public static final String CLASS = "corearena.command.class";

		/**     ADMIN COMMANDS BELOW    */

		// Toggle an arena's edit mode
		public static final String EDIT = "corearena.command.edit";

		// Items' menu
		public static final String ITEMS = "corearena.command.items";

		// Tools' menu
		public static final String TOOLS = "corearena.command.tools";

		// Open admin menu
		public static final String MENU = "corearena.command.menu";

		// Manage player nuggets
		public static final String NUGGET = "corearena.command.nugget";

		// Break the plugin
		public static final String RELOAD = "corearena.command.reload";

		// Start pending arenas
		public static final String START = "corearena.command.start";

		// Stop running arenas
		public static final String STOP = "corearena.command.stop";

		// Create new arenas
		public static final String NEW = "corearena.command.new";

		// Find arenas at player's location
		public static final String FIND = "corearena.command.find";

		// Reply to server's conversation
		public static final String CONVERSATION = "corearena.command.conversation";

		// Teleport to an arena.
		public static final String TP = "corearena.command.tp";
	}

	public static final class Chat {

		// Being a message with '!' to send it to everyone on the server instead of only players in your arena.
		public static final String GLOBAL = "corearena.chat.global";

		// Permission to have a red name in the arena chat.
		@Deprecated // Not customizable
		public static final String RED_COLOR = "corearena.chat.color.red";
	}

	public static final class Bypass {

		// Allow running commands while playing in an arena
		public static final String ARENA_COMMANDS = "corearena.bypass.arenacommands";
	}

	public static final class Tools {

		// Grants permission to right click with tools as specified in the tools menu and use them.
		public static final String TOOLS = "corearena.tools";
	}
}
