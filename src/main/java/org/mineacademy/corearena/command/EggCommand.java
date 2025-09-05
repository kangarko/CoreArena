package org.mineacademy.corearena.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.EntityType;
import org.mineacademy.fo.ChatUtil;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompEntityType;

final class EggCommand extends AbstractCoreSubcommand {

	public EggCommand() {
		super("egg");

		this.setValidArguments(1, 1);
		this.setDescription("Get eggs to use in mob spawners.");
		this.setUsage("<monster>");
	}

	@Override
	protected void onCommand() {
		final String mobName = Common.joinRange(0, this.args);

		final EntityType type = CompEntityType.fromName(mobName);
		this.checkNotNull(type, "No such entity: " + mobName + ". Available: " + Common.join(CompEntityType.getAvailableSpawnable(), list -> list.name().toLowerCase()));

		final String name = ChatUtil.capitalizeFully(type);

		this.checkBoolean(type.isSpawnable() && type.isAlive(), "The entity " + name + " is not a spawnable entity");

		ItemCreator.fromMonsterEgg(type,
				"Spawn " + name,
				"",
				"Place this item in your",
				"monster spawner to",
				"summon this mob.")
				.glow(true)
				.tag("CoreArenaMob", type.toString())
				.give(this.getPlayer());

		this.tellSuccess("You've got an egg for " + name + ". Place this into an arena mob spawner to have some fun! (Please note that using this egg outside of the plugin might not spawn the right mob.)");
	}

	@Override
	public List<String> tabComplete() {
		if (this.args.length == 1)
			return this.completeLastWord(CompEntityType.getAvailableSpawnable());

		return new ArrayList<>();
	}
}