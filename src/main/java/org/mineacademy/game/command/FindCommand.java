package org.mineacademy.game.command;

import static org.mineacademy.game.settings.Localization.Commands.Find.FOUND;
import static org.mineacademy.game.settings.Localization.Commands.Find.FOUND_LOCATION;
import static org.mineacademy.game.settings.Localization.Commands.Find.FOUND_OTHER;
import static org.mineacademy.game.settings.Localization.Commands.Find.NOT_FOUND;
import static org.mineacademy.game.settings.Localization.Commands.Find.NOT_FOUND_LOCATION;
import static org.mineacademy.game.settings.Localization.Commands.Find.WRONG_SYNTAX;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Common;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.model.ArenaManager;
import org.mineacademy.game.settings.Localization;

public class FindCommand extends AbstractCoreSubcommand {

	public FindCommand() {
		super("find|f");

		setDescription("Find arena at a location.");
		setUsage("[player] or [x y z] or [world x y z]");
	}

	@Override
	protected final void onCommand() {
		final ArenaManager m = getArenas();
		Arena arena;

		if (args.length == 0) {
			arena = m.findArena(getPlayer().getLocation());
			checkNotNull(arena, NOT_FOUND);

			returnTell(FOUND.replace("{arena}", arena.getName()));
		}

		if (args.length == 1) {
			final String other = args[0];
			final Player other_player = Bukkit.getPlayer(other);
			checkNotNull(other_player, Localization.Player.NOT_ONLINE.replace("{player}", other));

			arena = m.findArena(other_player);
			checkNotNull(arena, Localization.Player.NOT_PLAYING.replace("{player}", other_player.getName()));

			returnTell(FOUND_OTHER.replace("{player}", other_player.getName()).replace("{arena}", arena.getName()));
		}

		world:
		if (args.length == 3 || args.length == 4) {
			int arg = 0;

			final String wRaw = args.length == 3 ? getPlayer().getWorld().getName() : args[arg++];
			final World w = Bukkit.getWorld(wRaw);
			checkNotNull(w, Localization.World.NOT_FOUND.replace("{world}", wRaw));

			final Integer x = getNumber(arg++);
			final Integer y = getNumber(arg++);
			final Integer z = getNumber(arg++);

			if (x == null || y == null || z == null)
				break world;

			final Location loc = new Location(w, x, y, z);

			arena = m.findArena(loc);
			checkNotNull(arena, NOT_FOUND_LOCATION.replace("{location}", Common.shortLocation(loc)));

			returnTell(FOUND_LOCATION.replace("{arena}", arena.getName()).replace("{location}", Common.shortLocation(loc)));
		}

		returnTell(WRONG_SYNTAX);
	}

	private final Integer getNumber(int arg) {
		try {
			return Integer.parseInt(args[arg]);
		} catch (final NumberFormatException ex) {
			return null;
		}
	}
}