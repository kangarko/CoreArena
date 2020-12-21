package org.mineacademy.game.item;

import java.util.Arrays;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.model.SimpleEnchant;
import org.mineacademy.fo.remain.CompItemFlag;
import org.mineacademy.fo.remain.CompMaterial;

import lombok.Getter;

public class ExplosiveArrow {

	private static final ExplosiveArrow instance = new ExplosiveArrow();

	public static ItemStack getItem() {
		return instance.getItemStack();
	}

	@Getter
	private final ItemStack itemStack;

	{
		itemStack = ItemCreator.of(CompMaterial.ARROW)
				.name("&4Explosive Arrow")
				.enchant(new SimpleEnchant(Enchantment.DURABILITY, 1))
				.flag(CompItemFlag.HIDE_ENCHANTS)
				.lores(Arrays.asList(
						"",
						"&7The ammunition used",
						"&7by &5Explosive Bow."))
				.build().make();
	}

}
