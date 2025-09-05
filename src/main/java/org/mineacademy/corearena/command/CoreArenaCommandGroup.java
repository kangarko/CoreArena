package org.mineacademy.corearena.command;

import org.mineacademy.corearena.util.Permissions;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.command.PermsSubCommand;
import org.mineacademy.fo.command.SimpleCommandGroup;

@AutoRegister
public final class CoreArenaCommandGroup extends SimpleCommandGroup {

	@Override
	protected void registerSubcommands() {
		this.registerSubcommand(AbstractCoreSubcommand.class);
		this.registerDefaultSubcommands();

		this.registerSubcommand(new PermsSubCommand(Permissions.class));
	}
}
