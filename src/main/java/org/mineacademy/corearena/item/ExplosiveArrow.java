package org.mineacademy.corearena.item;

import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompEnchantment;
import org.mineacademy.fo.remain.CompItemFlag;
import org.mineacademy.fo.remain.CompMaterial;

import lombok.Getter;

public final class ExplosiveArrow {

	private static final ExplosiveArrow instance = new ExplosiveArrow();

	public static ItemStack getItem() {
		return instance.getItemStack();
	}

	@Getter
	private final ItemStack itemStack;

	{
		this.itemStack = ItemCreator.fromMaterial(CompMaterial.ARROW)
				.name("&4Explosive Arrow")
				.enchant(CompEnchantment.DURABILITY)
				.flags(CompItemFlag.HIDE_ENCHANTS)
				.lore(
						"",
						"&7The ammunition used",
						"&7by &5Explosive Bow.")
				.make();
	}

}
