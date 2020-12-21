package org.mineacademy.game.impl;

import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.model.ConfigSerializable;
import org.mineacademy.game.model.Reward;
import org.mineacademy.game.type.RewardType;

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
		return obj instanceof Reward && ((Reward) obj).getItem().equals(item) && ((Reward) obj).getType() == type;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{" + cost + ", " + (item != null ? item.getType() : "null_item") + "}";
	}

	@Override
	public SerializedMap serialize() {
		final SerializedMap map = new SerializedMap();

		map.put("type", type.name());
		map.put("item", item);
		map.put("cost", cost);

		return map;
	}

	public static SimpleReward deserialize(Map<String, Object> map) {
		if (!(map.get("item") instanceof ItemStack))
			return null;

		final ItemStack item = (ItemStack) map.get("item");
		final int cost = (int) map.get("cost");
		final RewardType type = RewardType.valueOf(map.get("type").toString());

		return new SimpleReward(type, item, cost);
	}

	public static SimpleReward fromItem(RewardType type, ItemStack item, int cost) {
		return new SimpleReward(type, item, cost);
	}
}
