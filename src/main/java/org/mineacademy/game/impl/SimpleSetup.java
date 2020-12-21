package org.mineacademy.game.impl;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.menu.tool.Tool;
import org.mineacademy.fo.menu.tool.ToolRegistry;
import org.mineacademy.game.exception.CancelledException;
import org.mineacademy.game.menu.MenuMonsterSpawn;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.model.ArenaData;
import org.mineacademy.game.model.ArenaRegion;
import org.mineacademy.game.model.Setup;
import org.mineacademy.game.model.SpawnPoint;
import org.mineacademy.game.tool.Selector;
import org.mineacademy.game.type.SpawnPointType;

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
		return isLobbySet() && isRegionSet() && isPlayerSpawnpointSet();
	}

	@Override
	public boolean isLobbySet() {
		return getData().getLobby() != null;
	}

	@Override
	public boolean isRegionSet() {
		final ArenaRegion rg = getData().getRegion();

		return rg != null && rg.getPrimary() != null && rg.getSecondary() != null;
	}

	@Override
	public boolean areJoinSignsSet() {
		return getData().getSigns() != null;
	}

	@Override
	public void onEnterEditMode(Player byWhom) {
		{ // Set cache
			final ArenaPlayer data = CoreArenaPlugin.getDataFor(byWhom);

			data.setCurrentSetup(arena);
			data.getSetupCache().showSidebar(byWhom);
		}

		{ // Render blocks
			for (final Tool tool : ToolRegistry.getTools())
				if (tool instanceof Selector) {
					final Selector selector = (Selector) tool;

					selector.onArenaEnterEditMode(byWhom, arena);
				}
		}
	}

	@Override
	public void onLeaveEditMode(Player player) {
		Valid.checkBoolean(CoreArenaPlugin.getSetupManager().isArenaEdited(arena), "Arena " + arena.getName() + " is not edited!");

		{ // Set cache
			final ArenaPlayer data = CoreArenaPlugin.getDataFor(player);

			data.getSetupCache().hideSidebar(player);
			data.removeCurrentSetup();
		}

		for (final Tool tool : ToolRegistry.getTools())
			if (tool instanceof Selector) {
				final Selector selector = (Selector) tool;

				selector.onArenaLeaveEditMode(arena);
			}
	}

	@Override
	public boolean isPlayerSpawnpointSet() {
		return getPlayerSpawnPoints() > 0;
	}

	public int getPlayerSpawnPoints() {
		return getData().getSpawnPoints(SpawnPointType.PLAYER).size();
	}

	public int getMobSpawnPoints() {
		return getData().getSpawnPoints(SpawnPointType.MONSTER).size();
	}

	@Override
	public void onSetupClick(Player pl, Action action, Block clickedBlock) throws CancelledException {
		if (action == Action.RIGHT_CLICK_BLOCK) {
			final SpawnPoint point = getData().findSpawnPoint(clickedBlock.getLocation());

			if (point != null && point instanceof SimpleSpawnPointMonster) {
				new MenuMonsterSpawn((SimpleSpawnPointMonster) point, getArena()).displayTo(pl);

				throw new CancelledException();
			}
		}
	}

	@Override
	public boolean isEdited() {
		return CoreArenaPlugin.getSetupManager().isArenaEdited(arena);
	}

	@Override
	public Player getEditor() {
		return isEdited() ? CoreArenaPlugin.getSetupManager().getEditorOf(arena) : null;
	}

	protected ArenaData getData() {
		return arena.getData();
	}
}