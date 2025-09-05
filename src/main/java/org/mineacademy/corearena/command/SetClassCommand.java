package org.mineacademy.corearena.command;

import java.util.List;

import org.bukkit.entity.Player;
import org.mineacademy.corearena.data.AllData.ArenaPlayer;
import org.mineacademy.corearena.data.AllData.ArenaPlayer.ClassCache;
import org.mineacademy.corearena.model.ArenaClass;
import org.mineacademy.corearena.type.TierMode;

final class SetClassCommand extends AbstractCoreSubcommand {

	public SetClassCommand() {
		super("setclass");

		this.setValidArguments(1, 2);
		this.setUsage("<player> or <player> <class>");
		this.setDescription("Set player classes during arena lobby.");
		this.setMinArguments(1);
	}

	@Override
	protected void onCommand() {
		final Player target = this.findPlayer(this.args[0]);
		final ArenaPlayer cache = this.getCache(target);

		if (!ClassCommand.canOpenMenu(target, this.getSender()))
			return;

		final ClassCache classCache = cache.getClassCache();

		if (this.args.length == 1) {
			this.tellInfo("&f" + target.getName() + " &7has class: " + (classCache.assignedClass == null ? "&7&oNone" : classCache.assignedClass.getName()));

			return;
		}

		this.checkArgs(2, "Usage: /{label} {sublabel} <player> <class>");

		final ArenaClass clazz = this.getClass(this.args[1]);
		this.checkNoSuchType(clazz, "class", this.args[1], getClasses().getClassNames());
		this.checkBoolean(clazz.isValid(), "Class '" + clazz.getName() + "' is not configured properly.");

		target.closeInventory();
		clazz.giveToPlayer(target, TierMode.PREVIEW, true);

		this.tellSuccess("Set &f" + target.getName() + "'s &7class to " + clazz.getName() + ".");
	}

	@Override
	public List<String> tabComplete() {
		switch (this.args.length) {
			case 1:
				return this.completeLastWordPlayerNames();
			case 2:
				return this.completeLastWord(getClasses().getClassNames());
		}

		return NO_COMPLETE;
	}
}