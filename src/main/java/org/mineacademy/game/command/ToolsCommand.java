package org.mineacademy.game.command;

import org.mineacademy.fo.menu.Menu;
import org.mineacademy.game.menu.MenuGameTools;

public final class ToolsCommand extends ItemMenuSubCommand {

	public ToolsCommand() {
		super("tools|t");

		setDescription("Get tools to edit the arena with.");
	}

	@Override
	protected Menu getMenu() {
		return MenuGameTools.getInstance();
	}
}