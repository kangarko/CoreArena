package org.mineacademy.game.impl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.game.model.ArmorContent;

public class SimpleArmorContent implements ArmorContent {

	private final ItemStack[] items;

	private SimpleArmorContent(ItemStack[] items) {
		Valid.checkBoolean(items.length == 4, "Armor must have the length of 4, not " + items.length);

		this.items = items;
	}

	@Override
	public final ItemStack getHelmet() {
		return items[0];
	}

	@Override
	public final ItemStack getChestplate() {
		return items[1];
	}

	@Override
	public final ItemStack getLeggings() {
		return items[2];
	}

	@Override
	public final ItemStack getBoots() {
		return items[3];
	}

	@Override
	public final ItemStack getByOrder(int order) {
		return items[order];
	}

	@Override
	public final void giveTo(Player pl) {
		final PlayerInventory inv = pl.getInventory();

		inv.setHelmet(getHelmet());
		inv.setChestplate(getChestplate());
		inv.setLeggings(getLeggings());
		inv.setBoots(getBoots());
	}

	@Override
	public SerializedMap serialize() {
		final SerializedMap map = new SerializedMap();

		map.putIfExist("helmet", getHelmet());
		map.putIfExist("chestplate", getChestplate());
		map.putIfExist("leggings", getLeggings());
		map.putIfExist("boots", getBoots());

		return map;
	}

	public static final SimpleArmorContent deserialize(SerializedMap map) {
		final ItemStack helmet = map.getItem("helmet");
		final ItemStack chestplate = map.getItem("chestplate");
		final ItemStack leggings = map.getItem("leggings");
		final ItemStack boots = map.getItem("boots");

		return fromItemStacks(helmet, chestplate, leggings, boots);
	}

	public static final SimpleArmorContent fromItemStacks(ItemStack... items) {
		return new SimpleArmorContent(items);
	}
}