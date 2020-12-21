package org.mineacademy.game.item;

import java.util.Arrays;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.model.SimpleEnchant;
import org.mineacademy.fo.remain.CompItemFlag;
import org.mineacademy.fo.remain.CompMaterial;

import lombok.Getter;

public class ExplosiveBow {

	private static final ExplosiveBow instance = new ExplosiveBow();

	public static ItemStack getItem() {
		return instance.getItemStack();
	}

	@Getter
	private final ItemStack itemStack;

	{
		itemStack = ItemCreator.of(CompMaterial.BOW)
				.name("&5Explosive Bow")
				.enchant(new SimpleEnchant(Enchantment.ARROW_INFINITE, 1))
				.enchant(new SimpleEnchant(Enchantment.ARROW_DAMAGE, 3))
				.flag(CompItemFlag.HIDE_ENCHANTS)
				.lores(Arrays.asList(
						"",
						"&7Makes shot arrows",
						"&7explode on contact.",
						"",
						"&7Requires &4Explosive Arrow"))
				.build().make();
	}

}
