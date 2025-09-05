package org.mineacademy.corearena.menu;

import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.tool.CloneSpawnerToolOff;
import org.mineacademy.corearena.tool.LobbyTool;
import org.mineacademy.corearena.tool.RegionArenaTool;
import org.mineacademy.corearena.tool.SpawnpointMonsterTool;
import org.mineacademy.corearena.tool.SpawnpointPlayerTool;
import org.mineacademy.fo.menu.MenuTools;

import lombok.Getter;

public class MenuGameTools extends MenuTools {

	@Getter
	private static final MenuGameTools instance = new MenuGameTools();

	private MenuGameTools() {
	}

	@Override
	protected final Object[] compileTools() {
		return new Object[] {
				LobbyTool.getTool(),
				RegionArenaTool.getTool(),
				SpawnpointPlayerTool.getTool(),
				SpawnpointMonsterTool.getTool(),
				CloneSpawnerToolOff.getInstance()
		};
	}

	@Override
	protected final String[] getInfo() {
		return new String[] {
				"Use the following tools when",
				"editing an arena.",
				"Run &e/" + CoreArenaPlugin.getInstance().getDefaultCommandGroup().getLabel() + " edit <arena> &7first."
		};
	}
}
