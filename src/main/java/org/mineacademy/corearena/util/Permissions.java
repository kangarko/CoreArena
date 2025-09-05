package org.mineacademy.corearena.util;

import org.mineacademy.fo.command.annotation.Permission;
import org.mineacademy.fo.command.annotation.PermissionGroup;

/**
 * Stores all permissions for the plugin.
 */
public final class Permissions {

	@PermissionGroup("Permissions for plugin commands.")
	public static final class Commands {

		@Permission("Join an arena. Replace {arena} with arena name.")
		public static final String JOIN = "corearena.command.join.{arena}";

		@Permission("Leave an arena.")
		public static final String LEAVE = "corearena.command.leave";

		@Permission("List available arenas.")
		public static final String LIST = "corearena.command.list";

		@Permission("Open rewards menu.")
		public static final String REWARDS = "corearena.command.rewards";

		@Permission("Open class menu.")
		public static final String CLASS = "corearena.command.class";

		@Permission("Toggle arena's edit mode.")
		public static final String EDIT = "corearena.command.edit";

		@Permission("Open items menu")
		public static final String ITEMS = "corearena.command.items";

		@Permission("Open tools menu")
		public static final String TOOLS = "corearena.command.tools";

		@Permission("Open arena admin menu.")
		public static final String MENU = "corearena.command.menu";

		@Permission("Manage player nuggets.")
		public static final String NUGGET = "corearena.command.nugget";

		@Permission("Reload the plugin.")
		public static final String RELOAD = "corearena.command.reload";

		@Permission("Start pending arenas.")
		public static final String START = "corearena.command.start";

		@Permission("Stop running arenas.")
		public static final String STOP = "corearena.command.stop";

		@Permission("Create a new arena.")
		public static final String NEW = "corearena.command.new";

		@Permission("Find arenas at players' locations.")
		public static final String FIND = "corearena.command.find";

		@Permission("Reply to server's conversation.")
		public static final String CONVERSATION = "corearena.command.conversation";

		@Permission("Teleport to an arena.")
		public static final String TP = "corearena.command.tp";
	}

	@PermissionGroup("Permissions for in-arena chat.")
	public static final class Chat {

		@Permission("Allows you to prefix chat messages with '!' to send it to everyone on the server instead of only arena players.")
		public static final String GLOBAL = "corearena.chat.global";

		@Permission("Allows you to have a red admin name in the arena chat.")
		public static final String RED_COLOR = "corearena.chat.color.red";
	}

	@PermissionGroup("Permissions for admin bypasses.")
	public static final class Bypass {

		@Permission("Allow running commands while playing in an arena.")
		public static final String ARENA_COMMANDS = "corearena.bypass.arenacommands";
	}

	@PermissionGroup("Permissions for arena tools.")
	public static final class Tools {

		@Permission("Grants permission to right click with tools as specified in the tools menu and use them.")
		public static final String TOOLS = "corearena.tools";
	}
}
