package org.mineacademy.game.menu;

import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.menu.MenuTools;
import org.mineacademy.game.tool.CloneSpawnerToolOff;
import org.mineacademy.game.tool.LobbySelector;
import org.mineacademy.game.tool.RegionSelector;
import org.mineacademy.game.tool.SpawnSelectorMonster;
import org.mineacademy.game.tool.SpawnSelectorPlayer;

import lombok.Getter;

public class MenuGameTools extends MenuTools {

	@Getter
	private static final MenuGameTools instance = new MenuGameTools();

	private MenuGameTools() {
	}

	@Override
	protected final Object[] compileTools() {
		return new Object[] {
				LobbySelector.getTool(),
				RegionSelector.getTool(),
				SpawnSelectorPlayer.getTool(),
				SpawnSelectorMonster.getTool(),
				CloneSpawnerToolOff.getInstance()
		};
	}

	@Override
	protected final String[] getInfo() {
		return new String[] {
				"Use the following tools when",
				"editing an arena.",
				"Run &e/" + CoreArenaPlugin.getInstance().getMainCommand().getLabel() + " edit <arena> &7first."
		};
	}
}
