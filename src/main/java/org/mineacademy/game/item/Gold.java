package org.mineacademy.game.item;

import java.util.Arrays;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.tool.Tool;
import org.mineacademy.fo.model.HookManager;
import org.mineacademy.fo.model.SimpleEnchant;
import org.mineacademy.fo.remain.CompItemFlag;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.game.settings.Settings;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Gold extends Tool {

	@Getter
	private static final Gold instance = new Gold();

	@Getter
	private final ItemStack item;

	{
		item = ItemCreator.of(CompMaterial.GOLD_NUGGET)
				.name("&6Gold")
				.enchant(new SimpleEnchant(Enchantment.ARROW_INFINITE, 1))
				.flag(CompItemFlag.HIDE_ENCHANTS)
				.lores(Arrays.asList(
						"",
						"&7Right click to sell",
						"&71x Gold for " + getCurrencyName(Settings.Experience.Gold.CONVERSION_RATIO)))
				.build().make();
	}

	private final String getCurrencyName(double ratio) {
		final String vaultName = ratio == 1 ? HookManager.getCurrencySingular() : HookManager.getCurrencyPlural();
		final String curr = Common.getOrDefault(vaultName, Settings.Experience.Gold.CURRENCY_NAME);
		final String finalName = curr.length() < 2 ? curr + ratio : ratio + "x " + curr;

		return finalName.endsWith(".0") ? finalName.replace(".0", "") : finalName;
	}

	@Override
	public void onBlockClick(PlayerInteractEvent e) {
		if (!e.getAction().toString().contains("RIGHT"))
			return;

		final Player player = e.getPlayer();

		HookManager.deposit(player, Settings.Experience.Gold.CONVERSION_RATIO);
		Common.tell(player, "&7You sold 1x Gold for &6" + getCurrencyName(Settings.Experience.Gold.CONVERSION_RATIO) + " &7and now have &6" + getCurrencyName(HookManager.getBalance(player)) + "&7.");

		Remain.takeHandItem(player);
		e.setCancelled(true);
	}

	@Override
	public boolean ignoreCancelled() {
		return false;
	}
}
