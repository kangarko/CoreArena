package org.mineacademy.corearena.item;

import java.util.Arrays;

import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompEnchantment;
import org.mineacademy.fo.remain.CompItemFlag;
import org.mineacademy.fo.remain.CompMaterial;

import lombok.Getter;

public final class ExplosiveBow {

	private static final ExplosiveBow instance = new ExplosiveBow();

	public static ItemStack getItem() {
		return instance.getItemStack();
	}

	@Getter
	private final ItemStack itemStack;

	{
		this.itemStack = ItemCreator.fromMaterial(CompMaterial.BOW)
				.name("&5Explosive Bow")
				.enchant(CompEnchantment.ARROW_INFINITE)
				.enchant(CompEnchantment.ARROW_DAMAGE, 3)
				.flags(CompItemFlag.HIDE_ENCHANTS)
				.lore(Arrays.asList(
						"",
						"&7Makes shot arrows",
						"&7explode on contact.",
						"",
						"&7Requires &4Explosive Arrow"))
				.make();
	}

}
