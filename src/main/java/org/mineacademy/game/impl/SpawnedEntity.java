package org.mineacademy.game.impl;

import java.util.Map;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.ItemUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.model.ConfigSerializable;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompMonsterEgg;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpawnedEntity implements ConfigSerializable {

	private final EntityType type;
	private final int count;

	// For third party plugin support
	private ItemStack customItem;

	@Override
	public SerializedMap serialize() {
		final SerializedMap map = new SerializedMap();

		if (customItem != null)
			map.put("custom_egg", customItem);
		else {
			map.put("entity", type);
			map.put("count", count);
		}

		return map;
	}

	public static SpawnedEntity deserialize(SerializedMap map) {
		if (map.containsKey("custom_egg")) {

			// Remove broken previously stored eggs
			final Object eggMap = map.containsKey("custom_egg") ? map.asMap().get("custom_egg") : null;

			if (eggMap instanceof Map)
				if (((Map<?, ?>) eggMap).containsKey("meta") && !(((Map<?, ?>) eggMap).get("meta") instanceof ItemMeta)) {
					map.remove("custom_egg");
					Common.warning("Detected invalid custom egg in one of your spawners, replacing with sheep.");

					return new SpawnedEntity(EntityType.SHEEP, 1);
				}

			final ItemStack item = map.getItem("custom_egg");
			final SpawnedEntity custom = new SpawnedEntity(CompMonsterEgg.getEntity(item), item.getAmount());

			custom.customItem = item;
			return custom;
		}

		final EntityType type = map.get("entity", EntityType.class);
		final int count = map.getInteger("count");

		return new SpawnedEntity(type, count);
	}

	public ItemStack toEgg() {
		return customItem != null ? customItem : CompMonsterEgg.makeEgg(type, count);
	}

	public boolean isCustom() {
		return customItem != null;
	}

	public static SpawnedEntity fromEgg(ItemStack is) {
		Valid.checkNotNull(is, "Itemstack is null!");
		Valid.checkBoolean(CompMaterial.isMonsterEgg(is.getType()), "Cannot create spawnpoint entity from " + is.getType());

		final EntityType type = CompMonsterEgg.getEntity(is);

		if (isUnknownEntity(is) || type == EntityType.UNKNOWN) {
			final SpawnedEntity spawned = new SpawnedEntity(type, is.getAmount());
			spawned.customItem = is;

			return spawned;
		}

		Valid.checkNotNull(type, "Could not detect monster from egg: " + is);
		Valid.checkBoolean(type.isSpawnable(), "Spawnpoint cannot summon entity: " + type);

		return new SpawnedEntity(type, is.getAmount());
	}

	private static boolean isUnknownEntity(ItemStack is) {
		return is.hasItemMeta() && (is.getItemMeta().hasDisplayName() || is.getItemMeta().hasLore());
	}

	public String format() {
		return getCount() + " " + (getCustomItem() != null ? getCustomItem() : ItemUtil.bountifyCapitalized(getType()));
	}

	@Override
	public String toString() {
		return "SpawnedEntity{" + getCount() + "x " + getType() + " (" + getCustomItem() + ")}";
	}
}
