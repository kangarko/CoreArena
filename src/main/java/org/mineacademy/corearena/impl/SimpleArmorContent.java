package org.mineacademy.corearena.impl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.mineacademy.corearena.model.ArmorContent;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;

public class SimpleArmorContent implements ArmorContent {

	private final ItemStack[] items;

	private SimpleArmorContent(ItemStack[] items) {
		Valid.checkBoolean(items.length == 4, "Armor must have the length of 4, not " + items.length);

		this.items = items;
	}

	@Override
	public final ItemStack getHelmet() {
		return this.items[0];
	}

	@Override
	public final ItemStack getChestplate() {
		return this.items[1];
	}

	@Override
	public final ItemStack getLeggings() {
		return this.items[2];
	}

	@Override
	public final ItemStack getBoots() {
		return this.items[3];
	}

	@Override
	public final ItemStack getByOrder(int order) {
		return this.items[order];
	}

	@Override
	public final void giveTo(Player pl) {
		final PlayerInventory inv = pl.getInventory();

		inv.setHelmet(this.getHelmet());
		inv.setChestplate(this.getChestplate());
		inv.setLeggings(this.getLeggings());
		inv.setBoots(this.getBoots());
	}

	@Override
	public SerializedMap serialize() {
		final SerializedMap map = new SerializedMap();

		map.putIfExists("helmet", this.getHelmet());
		map.putIfExists("chestplate", this.getChestplate());
		map.putIfExists("leggings", this.getLeggings());
		map.putIfExists("boots", this.getBoots());

		return map;
	}

	public static final SimpleArmorContent deserialize(SerializedMap map) {
		final ItemStack helmet = map.get("helmet", ItemStack.class);
		final ItemStack chestplate = map.get("chestplate", ItemStack.class);
		final ItemStack leggings = map.get("leggings", ItemStack.class);
		final ItemStack boots = map.get("boots", ItemStack.class);

		return fromItemStacks(helmet, chestplate, leggings, boots);
	}

	public static final SimpleArmorContent fromItemStacks(ItemStack... items) {
		return new SimpleArmorContent(items);
	}
}