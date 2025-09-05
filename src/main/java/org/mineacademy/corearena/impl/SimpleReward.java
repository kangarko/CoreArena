package org.mineacademy.corearena.impl;

import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.model.Reward;
import org.mineacademy.corearena.type.RewardType;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.model.ConfigSerializable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleReward implements ConfigSerializable, Reward {

	private final RewardType type;
	private ItemStack item;
	private int cost;

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Reward && ((Reward) obj).getItem().equals(this.item) && ((Reward) obj).getType() == this.type;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{" + this.cost + ", " + (this.item != null ? this.item.getType() : "null_item") + "}";
	}

	@Override
	public SerializedMap serialize() {
		final SerializedMap map = new SerializedMap();

		map.put("type", this.type);
		map.put("item", this.item);
		map.put("cost", this.cost);

		return map;
	}

	public static SimpleReward deserialize(SerializedMap map) {
		if (!map.containsKey("item"))
			return null;

		final ItemStack item = map.get("item", ItemStack.class);
		final int cost = map.getInteger("cost");
		final RewardType type = map.get("type", RewardType.class);

		return new SimpleReward(type, item, cost);
	}

	public static SimpleReward fromItem(RewardType type, ItemStack item, int cost) {
		return new SimpleReward(type, item, cost);
	}
}
