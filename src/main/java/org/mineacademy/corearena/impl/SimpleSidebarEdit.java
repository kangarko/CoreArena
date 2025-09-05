package org.mineacademy.corearena.impl;

import org.bukkit.entity.Player;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.model.ArenaSnapshotProcedural.DamagedStage;
import org.mineacademy.corearena.settings.Settings;
import org.mineacademy.fo.model.SimpleScoreboard;

public final class SimpleSidebarEdit extends SimpleScoreboard {

	private final Arena arena;

	public SimpleSidebarEdit(final Arena arena) {
		this.arena = arena;

		this.setTitle("&6&lEditing Arena " + this.arena.getName());
		this.setUpdateDelayTicks(20);
	}

	@Override
	protected void onUpdate() {
		this.getRows().clear();

		final SimpleSetup s = (SimpleSetup) this.arena.getSetup();
		this.addRows(
				" ",
				"&7Lobby: &f" + this.status(s.isLobbySet()),
				"&7Region: &f" + this.status(s.isRegionSet()),
				"&7Player Spawns: &f" + this.status(s.getPlayerSpawnPoints()));

		this.addRows("&7Monster Spawns: &f" + this.statusOpt(s.getMobSpawnPoints()));

		this.addRows(
				"&7Initial Snapshot: &f" + this.statusOpt(this.arena.getSnapshot().isSaved(DamagedStage.INITIAL)),
				"&7Damaged Snapshot: &f" + this.statusOpt(this.arena.getSnapshot().isSaved(DamagedStage.DAMAGED)),
				"",
				"&6To get tools, type:",
				"&7/" + Settings.MAIN_COMMAND_ALIASES.get(0) + " tools",
				"",
				"&6To stop editing, type:",
				"&7/" + Settings.MAIN_COMMAND_ALIASES.get(0) + " edit " + this.arena.getName());
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
		return this.arena.getMessenger().replaceVariables(message);
	}
}