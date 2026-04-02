package org.mineacademy.corearena.item;

import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompEnchantment;
import org.mineacademy.fo.remain.CompItemFlag;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.settings.Lang;

public final class ExplosiveArrow {

	public static ItemStack getItem() {
		return ItemCreator.fromMaterial(CompMaterial.ARROW)
				.name(Lang.legacy("item-explosive-arrow-name"))
				.enchant(CompEnchantment.DURABILITY)
				.flags(CompItemFlag.HIDE_ENCHANTS)
				.lore(Lang.legacy("item-explosive-arrow-lore"))
				.make();
	}

}
