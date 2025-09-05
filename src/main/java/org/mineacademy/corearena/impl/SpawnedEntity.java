package org.mineacademy.corearena.impl;

import java.util.Map;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.mineacademy.fo.ChatUtil;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.model.ConfigSerializable;
import org.mineacademy.fo.remain.CompEntityType;
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
	public SpawnedEntity clone() {
		final SpawnedEntity spawned = new SpawnedEntity(this.type, this.count);

		if (this.customItem != null)
			spawned.customItem = this.customItem.clone();

		return spawned;
	}

	@Override
	public SerializedMap serialize() {
		final SerializedMap map = new SerializedMap();

		if (this.customItem != null)
			map.put("custom_egg", this.customItem);
		else {
			map.put("entity", this.type);
			map.put("count", this.count);
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

					return new SpawnedEntity(CompEntityType.SHEEP, 1);
				}

			final ItemStack item = map.get("custom_egg", ItemStack.class);
			Valid.checkNotNull(item, "Unable to parse ItemStack from: " + map.asMap().get("custom_egg"));

			final SpawnedEntity custom = new SpawnedEntity(CompMonsterEgg.lookupEntity(item), item.getAmount());
			custom.customItem = item;

			return custom;
		}

		final EntityType type = map.get("entity", EntityType.class);
		final int count = map.getInteger("count");

		return new SpawnedEntity(type, count);
	}

	public ItemStack toEgg() {
		return this.customItem != null ? this.customItem : CompMonsterEgg.toItemStack(this.type, this.count);
	}

	public boolean isCustom() {
		return this.customItem != null;
	}

	public static SpawnedEntity fromEgg(ItemStack item) {
		Valid.checkNotNull(item, "Itemstack is null!");

		final EntityType type = CompMonsterEgg.lookupEntity(item);

		if (!CompMaterial.isMonsterEgg(item.getType()) || isUnknownEntity(item) || type == CompEntityType.UNKNOWN) {
			final SpawnedEntity spawned = new SpawnedEntity(type, item.getAmount());
			spawned.customItem = item;

			return spawned;
		}

		Valid.checkNotNull(type, "Could not detect monster from egg: " + item);
		Valid.checkBoolean(type.isSpawnable(), "Spawnpoint has entity which cannot be spawned: " + type);

		return new SpawnedEntity(type, item.getAmount());
	}

	private static boolean isUnknownEntity(ItemStack item) {
		return item.hasItemMeta() && (item.getItemMeta().hasDisplayName() || item.getItemMeta().hasLore());
	}

	public String format() {
		return this.getCount() + " " + (this.customItem != null ? (this.customItem.hasItemMeta() && this.customItem.getItemMeta().hasDisplayName() ? this.customItem.getItemMeta().getDisplayName() : this.customItem.getType()) : ChatUtil.capitalizeFully(this.type));
	}

	@Override
	public String toString() {
		return "SpawnedEntity{" + this.getCount() + "x " + this.getType() + (this.customItem != null ? " (" + this.customItem + ")" : "") + "}";
	}
}
