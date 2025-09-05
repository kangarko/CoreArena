package org.mineacademy.corearena.command;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.model.ArenaManager;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.SerializeUtil;
import org.mineacademy.fo.settings.Lang;

final class FindCommand extends AbstractCoreSubcommand {

	public FindCommand() {
		super("find|f");

		this.setValidArguments(0, 4);
		this.setDescription("Find arena at a location.");
		this.setUsage("[player] or [x y z] or [world x y z]");
	}

	@Override
	protected void onCommand() {
		final ArenaManager manager = this.getArenas();
		Arena arena;

		if (this.args.length == 0) {
			arena = manager.findArena(this.getPlayer().getLocation());
			this.checkNotNull(arena, Lang.component("command-find-not-found"));

			this.returnTell(Lang.component("command-find-found", "arena", arena.getName()));
		}

		if (this.args.length == 1) {
			final String other = this.args[0];
			final Player otherPlayer = this.findPlayer(other);

			arena = manager.findArena(otherPlayer);
			this.checkNotNull(arena, Lang.component("player-not-playing", "player", otherPlayer.getName()));

			this.returnTell(Lang.component("command-find-found-other", "player", otherPlayer.getName(), "arena", arena.getName()));
		}

		world:
		if (this.args.length == 3 || this.args.length == 4) {
			int arg = 0;

			final String worldNameRaw = this.args.length == 3 ? this.getPlayer().getWorld().getName() : this.args[arg++];
			final World world = this.findWorld(worldNameRaw);

			final Integer x = this.getNumber(arg++);
			final Integer y = this.getNumber(arg++);
			final Integer z = this.getNumber(arg++);

			if (x == null || y == null || z == null)
				break world;

			final Location loc = new Location(world, x, y, z);

			arena = manager.findArena(loc);
			this.checkNotNull(arena, Lang.component("command-find-not-found-location", "location", SerializeUtil.serializeLocation(loc)));

			this.returnTell(Lang.component("command-find-found-location", "arena", arena.getName(), "location", SerializeUtil.serializeLocation(loc)));
		}

		this.returnTell(Lang.component("command-find-wrong-syntax"));
	}

	private Integer getNumber(int arg) {
		try {
			return Integer.parseInt(this.args[arg]);
		} catch (final NumberFormatException ex) {
			return null;
		}
	}

	@Override
	protected List<String> tabComplete() {

		if (this.args.length == 1)
			return this.completeLastWord(Common.getWorldNames(), Common.getPlayerNames());

		if (this.isPlayer()) {
			final String arg = this.args[0];

			if (Common.getWorldNames().contains(arg)) {
				final Location location = this.getPlayer().getLocation();

				if (this.args.length == 2)
					return this.completeLastWord(location.getBlockX());

				else if (this.args.length == 3)
					return this.completeLastWord(location.getBlockY());

				else if (this.args.length == 4)
					return this.completeLastWord(location.getBlockZ());
			}
		}

		return NO_COMPLETE;
	}
}