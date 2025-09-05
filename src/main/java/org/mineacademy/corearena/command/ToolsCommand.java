package org.mineacademy.corearena.command;

import org.mineacademy.corearena.menu.MenuGameTools;
import org.mineacademy.fo.menu.Menu;

final class ToolsCommand extends ItemMenuSubCommand {

	public ToolsCommand() {
		super("tools|t");

		this.setDescription("Get tools to edit the arena with.");
	}

	@Override
	protected Menu getMenu() {
		return MenuGameTools.getInstance();
	}
}