package org.mineacademy.corearena.impl;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.data.AllData.ArenaPlayer;
import org.mineacademy.corearena.exception.CancelledException;
import org.mineacademy.corearena.menu.MenuMonsterSpawn;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.model.ArenaData;
import org.mineacademy.corearena.model.ArenaRegion;
import org.mineacademy.corearena.model.Setup;
import org.mineacademy.corearena.model.SpawnPoint;
import org.mineacademy.corearena.tool.SelectorTool;
import org.mineacademy.corearena.type.SpawnPointType;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.menu.tool.Tool;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Setup that covers Lobbby, Region and Join Sign.
 */
@RequiredArgsConstructor
public final class SimpleSetup implements Setup {

	@Getter(value = AccessLevel.PROTECTED)
	private final Arena arena;

	@Override
	public boolean isReady() {
		return this.isLobbySet() && this.isRegionSet() && this.isPlayerSpawnpointSet();
	}

	@Override
	public boolean isLobbySet() {
		return this.getData().getLobby() != null;
	}

	@Override
	public boolean isRegionSet() {
		final ArenaRegion rg = this.getData().getRegion();

		return rg != null && rg.getPrimary() != null && rg.getSecondary() != null;
	}

	@Override
	public boolean areJoinSignsSet() {
		return this.getData().getSigns() != null;
	}

	@Override
	public void onEnterEditMode(Player byWhom) {
		{ // Set cache
			final ArenaPlayer data = CoreArenaPlugin.getDataFor(byWhom);

			data.setCurrentSetup(this.arena);
			data.getSetupCache().showSidebar(byWhom);
		}

		{ // Render blocks
			for (final Tool tool : Tool.getTools())
				if (tool instanceof SelectorTool) {
					final SelectorTool selector = (SelectorTool) tool;

					selector.onArenaEnterEditMode(byWhom, this.arena);
				}
		}
	}

	@Override
	public void onLeaveEditMode(Player player) {
		Valid.checkBoolean(CoreArenaPlugin.getSetupManager().isArenaEdited(this.arena), "Arena " + this.arena.getName() + " is not edited!");

		{ // Set cache
			final ArenaPlayer data = CoreArenaPlugin.getDataFor(player);

			data.getSetupCache().hideSidebar(player);
			data.removeCurrentSetup();
		}

		for (final Tool tool : Tool.getTools())
			if (tool instanceof SelectorTool) {
				final SelectorTool selector = (SelectorTool) tool;

				selector.onArenaLeaveEditMode(this.arena);
			}
	}

	@Override
	public boolean isPlayerSpawnpointSet() {
		return this.getPlayerSpawnPoints() > 0;
	}

	public int getPlayerSpawnPoints() {
		return this.getData().getSpawnPoints(SpawnPointType.PLAYER).size();
	}

	public int getMobSpawnPoints() {
		return this.getData().getSpawnPoints(SpawnPointType.MONSTER).size();
	}

	@Override
	public void onSetupClick(Player pl, Action action, Block clickedBlock) throws CancelledException {
		if (action == Action.RIGHT_CLICK_BLOCK) {
			final SpawnPoint point = this.getData().findSpawnPoint(clickedBlock.getLocation());

			if (point instanceof SimpleSpawnPointMonster) {
				new MenuMonsterSpawn((SimpleSpawnPointMonster) point, this.getArena()).displayTo(pl);

				throw new CancelledException();
			}
		}
	}

	@Override
	public boolean isEdited() {
		return CoreArenaPlugin.getSetupManager().isArenaEdited(this.arena);
	}

	@Override
	public Player getEditor() {
		return this.isEdited() ? CoreArenaPlugin.getSetupManager().getEditorOf(this.arena) : null;
	}

	protected ArenaData getData() {
		return this.arena.getData();
	}
}