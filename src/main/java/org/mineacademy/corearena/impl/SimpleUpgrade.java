package org.mineacademy.corearena.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.data.AllData;
import org.mineacademy.corearena.model.Upgrade;
import org.mineacademy.corearena.util.Constants;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.settings.Lang;
import org.mineacademy.fo.settings.YamlConfig;

import lombok.Getter;

/**
 * Class covering the data section + capable of setting inventory content
 */
@Getter
public final class SimpleUpgrade extends YamlConfig implements Upgrade {

	private final AllData.UpgradeData data;

	private String permission;

	private int unlockPhase;

	public SimpleUpgrade(String name) {
		this.data = AllData.getInstance().loadUpgradeData(name);

		this.setHeader(Constants.Header.UPGRADE_FILE);
		this.loadAndExtract("prototype/upgrade.yml", "upgrades/" + name + ".yml");
	}

	@Override
	protected void onLoad() {
		this.permission = this.getString("Permission");
		this.unlockPhase = this.getInteger("Available_From_Phase");
	}

	@Override
	public void deleteUpgrade() {
		Valid.checkBoolean(CoreArenaPlugin.getUpgradesManager().findUpgrade(this.getName()) == null, "Vodstrn " + this.getName() + " vod vrchu");

		this.getFile().delete();
		this.data.clear();
	}

	@Override
	public void giveToPlayer(Player player) {
		final List<ItemStack> itemsToGive = new ArrayList<>();

		if (this.getItems() != null)
			itemsToGive.addAll(Common.removeNullAndEmpty(Arrays.asList(this.getItems())));

		final Map<Integer, ItemStack> leftOvers = PlayerUtil.addItems(player.getInventory(), itemsToGive.toArray(new ItemStack[itemsToGive.size()]));

		for (final ItemStack leftOver : leftOvers.values())
			if (leftOver.getAmount() > leftOver.getMaxStackSize()) {
				final double count = (double) leftOver.getAmount() / (double) leftOver.getMaxStackSize();
				leftOver.setAmount(leftOver.getMaxStackSize());

				for (int i = 0; i < count; i++)
					player.getWorld().dropItem(player.getLocation(), leftOver);

				final int rest = (int) Math.round(Double.valueOf(Double.toString(count).split("\\.")[1]) * leftOver.getMaxStackSize());

				leftOver.setAmount(rest);
				player.getWorld().dropItem(player.getLocation(), leftOver);

			} else
				player.getWorld().dropItemNaturally(player.getLocation(), leftOver);

		if (!leftOvers.isEmpty()) {
			Messenger.error(player, Lang.component("upgrade-lack-space"));

			for (final ItemStack leftoverItem : leftOvers.values())
				player.getWorld().dropItemNaturally(player.getLocation(), leftoverItem);
		}
	}

	public boolean isDataValid() {
		return this.data.isValid();
	}

	@Override
	public ItemStack[] getItems() {
		return this.data.getItems();
	}

	@Override
	public void setItems(ItemStack[] items) {
		this.data.setItems(items);
	}

	@Override
	public String getName() {
		return this.getFileName();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof SimpleUpgrade && ((SimpleUpgrade) obj).getName().equals(this.getName());
	}
}