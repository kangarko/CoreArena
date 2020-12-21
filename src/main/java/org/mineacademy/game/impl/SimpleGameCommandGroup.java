package org.mineacademy.game.impl;

import org.mineacademy.fo.command.ReloadCommand;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.game.command.ClassCommand;
import org.mineacademy.game.command.ConvoCommand;
import org.mineacademy.game.command.EditCommand;
import org.mineacademy.game.command.EggCommand;
import org.mineacademy.game.command.FindCommand;
import org.mineacademy.game.command.ItemsCommand;
import org.mineacademy.game.command.JoinCommand;
import org.mineacademy.game.command.LeaveCommand;
import org.mineacademy.game.command.ListCommand;
import org.mineacademy.game.command.MenuCommand;
import org.mineacademy.game.command.NewCommand;
import org.mineacademy.game.command.NuggetCommand;
import org.mineacademy.game.command.RewardsCommand;
import org.mineacademy.game.command.StartCommand;
import org.mineacademy.game.command.StopCommand;
import org.mineacademy.game.command.ToggleCommand;
import org.mineacademy.game.command.ToolsCommand;
import org.mineacademy.game.command.TpCommand;

public final class SimpleGameCommandGroup extends SimpleCommandGroup {

	@Override
	protected void registerSubcommands() {
		registerHelpLine(" &6&lPlayer Commands");

		registerSubcommand(new JoinCommand());
		registerSubcommand(new LeaveCommand());
		registerSubcommand(new ListCommand());
		registerSubcommand(new ClassCommand());
		registerSubcommand(new RewardsCommand());

		registerHelpLine(" ", " &6&lAdmin Commands");

		registerSubcommand(new MenuCommand());
		registerSubcommand(new NewCommand());
		registerSubcommand(new StartCommand());
		registerSubcommand(new StopCommand());
		registerSubcommand(new ToggleCommand());
		registerSubcommand(new EditCommand());
		registerSubcommand(new EggCommand());
		registerSubcommand(new FindCommand());
		registerSubcommand(new TpCommand());
		registerSubcommand(new NuggetCommand());
		registerSubcommand(new ItemsCommand());
		registerSubcommand(new ToolsCommand());
		registerSubcommand(new ConvoCommand());
		registerSubcommand(new ReloadCommand());
	}
}
