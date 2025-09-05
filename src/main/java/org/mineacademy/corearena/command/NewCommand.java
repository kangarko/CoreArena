package org.mineacademy.corearena.command;

import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversable;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.fo.Messenger;

final class NewCommand extends AbstractCoreSubcommand {

	public NewCommand() {
		super("new");

		this.setValidArguments(1, 1);
		this.setDescription("Create a new arena.");
		this.setUsage("<name>");
	}

	@Override
	protected void onCommand() {
		final String name = this.args[0];

		if (!mayCreate(this.getPlayer(), name))
			return;

		final Arena arena = CoreArenaPlugin.getArenaManager().createArena(name);
		this.getSetup().addEditedArena(this.getPlayer(), arena);

		this.tellSuccess("Created arena '" + arena.getName() + "'. It was now put into edit mode. Use '/" + this.getLabel() + " tools' to get edit tools.");
	}

	public static boolean mayCreate(Conversable player, String input) {

		@Deprecated
		class $ {
			private void say(Object obj, String message) {
				Messenger.error((CommandSender) obj, message);
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