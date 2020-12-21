package org.mineacademy.game.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.settings.YamlConfig;
import org.mineacademy.game.data.UpgradeDataSection;
import org.mineacademy.game.model.Upgrade;
import org.mineacademy.game.settings.Localization;
import org.mineacademy.game.util.Constants;

import lombok.Getter;

/**
 * Class covering the data section + capable of setting inventory content
 */
@Getter
public final class SimpleUpgrade extends YamlConfig implements Upgrade {

	private final UpgradeDataSection data;

	private String permission;

	private int unlockPhase;

	public SimpleUpgrade(String name) {
		this.data = new UpgradeDataSection(name);

		setHeader(Constants.Header.UPGRADE_FILE);
		loadConfiguration("prototype/upgrade.yml", "upgrades/" + name + ".yml");
	}

	@Override
	protected void onLoadFinish() {
		permission = getString("Permission");
		unlockPhase = getInteger("Available_From_Phase");
	}

	@Override
	public void deleteUpgrade() {
		Valid.checkBoolean(CoreArenaPlugin.getUpgradesManager().findUpgrade(getName()) == null, "Vodstrn " + getName() + " vod vrchu");

		delete();
		data.deleteSection();
	}

	@Override
	public void giveToPlayer(Player pl) {
		final List<ItemStack> itemsToGive = new ArrayList<>();

		if (getItems() != null)
			itemsToGive.addAll(Common.removeNullAndEmpty(getItems()));

		final Map<Integer, ItemStack> leftOvers = PlayerUtil.addItems(pl.getInventory(), itemsToGive.toArray(new ItemStack[itemsToGive.size()]));

		for (final ItemStack is : leftOvers.values())
			if (is.getAmount() > is.getMaxStackSize()) {
				final double count = (double) is.getAmount() / (double) is.getMaxStackSize();
				is.setAmount(is.getMaxStackSize());

				for (int i = 0; i < count; i++)
					pl.getWorld().dropItem(pl.getLocation(), is);

				final int rest = (int) Math.round(Double.valueOf(Double.toString(count).split("\\.")[1]) * is.getMaxStackSize());

				is.setAmount(rest);
				pl.getWorld().dropItem(pl.getLocation(), is);

			} else
				pl.getWorld().dropItemNaturally(pl.getLocation(), is);

		if (!leftOvers.isEmpty())
			Common.tell(pl, Localization.Upgrades.LACK_SPACE);
	}

	public boolean isDataValid() {
		return data.isSectionValid();
	}

	@Override
	public ItemStack[] getItems() {
		return data.getItems();
	}

	@Override
	public void setItems(ItemStack[] items) {
		data.setItems(items);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof SimpleUpgrade && ((SimpleUpgrade) obj).getName().equals(getName());
	}
}