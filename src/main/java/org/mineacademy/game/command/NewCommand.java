package org.mineacademy.game.command;

import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversable;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.game.model.Arena;

public class NewCommand extends AbstractCoreSubcommand {

	public NewCommand() {
		super("new");

		setDescription("Create a new arena.");
		setUsage("<name>");
		setMinArguments(1);
	}

	@Override
	protected final void onCommand() {
		final String name = args[0];

		if (!mayCreate(getPlayer(), name))
			return;

		final Arena arena = CoreArenaPlugin.getArenaManager().createArena(name);
		getSetup().addEditedArena(getPlayer(), arena);

		tell("&7A new Arena " + arena.getName() + " has been &2created&7 and put into edit mode automatically.");
	}

	public static boolean mayCreate(Conversable player, String input) {

		@Deprecated
		class $ {
			private void say(Object obj, String message) {
				if (obj instanceof Conversable)
					Common.tellLaterConversing(1, (Conversable) obj, message);
				else
					Common.tell((CommandSender) obj, message);
			}
		}

		final $ $ = new $();

		if (input.length() < 3 || input.length() > 50) {
			$.say(player, "&cName may be between 3 and 50 letters long.");

			return false;
		}

		if (input.contains(" ")) {
			$.say(player, "&cName may not contains spaces.");

			return false;
		}

		if (input.contains("&")) {
			$.say(player, "&cName may not contains colors.");

			return false;
		}

		if (CoreArenaPlugin.getArenaManager().findArena(input) != null) {
			$.say(player, "&cArena named '" + input + "' already exists.");

			return false;
		}

		return true;
	}
}