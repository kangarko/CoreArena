package org.mineacademy.corearena.item;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.settings.Settings;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.tool.Tool;
import org.mineacademy.fo.model.HookManager;
import org.mineacademy.fo.remain.CompEnchantment;
import org.mineacademy.fo.remain.CompItemFlag;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.fo.settings.Lang;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Gold extends Tool {

	@Getter
	private static final Gold instance = new Gold();

	@Override
	public ItemStack getItem() {
		return ItemCreator.fromMaterial(CompMaterial.GOLD_NUGGET)
				.name(Lang.legacy("item-gold-name"))
				.lore(Lang.legacy("item-gold-lore", "price", this.getCurrencyName(Settings.Experience.Gold.CONVERSION_RATIO)))
				.enchant(CompEnchantment.ARROW_INFINITE, 1)
				.flags(CompItemFlag.HIDE_ENCHANTS)
				.hideTags(true)
				.tag("CoreArena_Gold", "true")
				.make();
	}

	@Override
	public boolean compareByNbt() {
		return true;
	}

	private String getCurrencyName(double ratio) {
		final String vaultName = ratio == 1 ? HookManager.getCurrencySingular() : HookManager.getCurrencyPlural();
		final String curr = Common.getOrDefault(vaultName, Settings.Experience.Gold.CURRENCY_NAME);
		final String finalName = curr.length() < 2 ? curr + ratio : ratio + "x " + curr;

		return finalName.endsWith(".0") ? finalName.replace(".0", "") : finalName;
	}

	@Override
	public void onBlockClick(PlayerInteractEvent event) {
		if (!event.getAction().toString().contains("RIGHT"))
			return;

		final Player player = event.getPlayer();

		HookManager.deposit(player, Settings.Experience.Gold.CONVERSION_RATIO);
		Common.tell(player, Lang.legacy("item-gold-sold", "price", this.getCurrencyName(Settings.Experience.Gold.CONVERSION_RATIO), "balance", this.getCurrencyName(MathUtil.formatTwoDigitsD(HookManager.getBalance(player)))));

		Remain.takeHandItem(player);
		event.setCancelled(true);
	}

	@Override
	public boolean ignoreCancelled() {
		return false;
	}
}
