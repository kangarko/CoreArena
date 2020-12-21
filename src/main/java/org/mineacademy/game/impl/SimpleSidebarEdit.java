package org.mineacademy.game.impl;

import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.model.SimpleScoreboard;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.model.ArenaSnapshotProcedural.DamagedStage;

public final class SimpleSidebarEdit extends SimpleScoreboard {

	private final Arena arena;

	public SimpleSidebarEdit(final Arena arena) {
		this.arena = arena;

		setTitle(" ");
		setUpdateDelayTicks(20);
	}

	@Override
	protected void onUpdate() {
		getRows().clear();

		final SimpleSetup s = (SimpleSetup) arena.getSetup();
		addRows(
				"  &6&lEditing Arena " + arena.getName(),
				" ",
				"&6" + Common.scoreboardLine(23),
				"",
				$("&7Lobby: ") + status(s.isLobbySet()),
				$("&7Region (2 points): ") + status(s.isRegionSet()),
				$("&7Player Spawnpoints: ") + status(s.getPlayerSpawnPoints()));

		addRows($("&7Monster Spawnpoints: ") + statusOpt(s.getMobSpawnPoints()));

		addRows(
				"",
				$("&7Initial Snapshot: ") + statusOpt(arena.getSnapshot().isSaved(DamagedStage.INITIAL)),
				$("&7Damaged Snapshot: ") + statusOpt(arena.getSnapshot().isSaved(DamagedStage.DAMAGED)),
				"",
				"&6To quit editing, type:",
				"&7/" + CoreArenaPlugin.getInstance().getMainCommand().getLabel() + " edit " + arena.getName(),
				""/*,*/
		/*"&4" + Common.scoreboardLine(22)*/);
	}

	private String $(final String s) {
		final String c = Common.lastColor(s);

		return c + "&l" + s.substring(2, 3) + c + s.substring(3, s.length());
	}

	private String status(final boolean is) {
		return is ? "&aSet" : "&cPending";
	}

	private String statusOpt(final boolean is) {
		return is ? "&aSet" : "&6Not set";
	}

	private String status(final int nonNull) {
		return nonNull > 0 ? "&a" + nonNull : "&c" + nonNull;
	}

	private String statusOpt(final int nonNull) {
		return nonNull > 0 ? "&a" + nonNull : "&6" + nonNull;
	}

	@Override
	protected String replaceVariables(final Player player, final String message) {
		return arena.getMessenger().replaceVariables(message);
	}
}