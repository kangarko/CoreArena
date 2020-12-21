package org.mineacademy.game.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Common.TypeConverter;
import org.mineacademy.fo.ItemUtil;
import org.mineacademy.fo.ReflectionUtil;
import org.mineacademy.fo.ReflectionUtil.MissingEnumException;
import org.mineacademy.fo.TabUtil;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompMonsterEgg;

public class EggCommand extends AbstractCoreSubcommand {

	public EggCommand() {
		super("egg");

		setDescription("Get eggs to use in mob spawners.");
		setUsage("<monster>");
		setMinArguments(1);
	}

	@Override
	protected final void onCommand() {
		final String mobName = Common.joinRange(0, args);

		EntityType type = null;

		try {
			type = ReflectionUtil.lookupEnum(EntityType.class, mobName);

		} catch (final MissingEnumException ex) {
			returnTell("&cUnknown entity '" + mobName + "'. Available: " + String.join(", ", Common.convert(getValidEntities(), (TypeConverter<String, String>) ItemUtil::bountifyCapitalized)));
		}

		final String fancyEntity = ItemUtil.bountifyCapitalized(type);
		checkBoolean(canBeSpawned(type), "The entity " + fancyEntity + " cannot be spawned or is unstable and has been disabled.");

		ItemStack egg = ItemCreator.of(
				CompMaterial.SHEEP_SPAWN_EGG,
				"Spawn " + fancyEntity,
				"",
				"Place this item in your",
				"monster spawner to",
				"summon " + fancyEntity)
				.glow(true)
				.build().make();

		egg = CompMonsterEgg.setEntity(egg, type);

		getPlayer().getInventory().addItem(egg);
		tell("You've got an egg for " + fancyEntity + ". Place this into an arena mob spawner to have some fun!");
	}

	private final List<String> getValidEntities() {
		final List<String> list = new ArrayList<>();

		for (final EntityType entity : EntityType.values())
			if (canBeSpawned(entity))
				list.add(entity.toString());

		return list;
	}

	private final boolean canBeSpawned(EntityType entity) {
		return entity.isSpawnable() && entity.isAlive() && !"ARMOR_STAND".equals(entity.toString()) && !"ENDER_DRAGON".equals(entity.toString());
	}

	@Override
	public List<String> tabComplete() {
		if (args.length == 1)
			return TabUtil.complete(args[0], getValidEntities());

		return new ArrayList<>();
	}
}