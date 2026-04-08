package org.mineacademy.corearena.item;

import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompEnchantment;
import org.mineacademy.fo.remain.CompItemFlag;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.settings.Lang;

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
				.name(Lang.legacy("item-explosive-bow-name"))
				.enchant(CompEnchantment.ARROW_INFINITE)
				.enchant(CompEnchantment.ARROW_DAMAGE, 3)
				.flags(CompItemFlag.HIDE_ENCHANTS)
				.lore(Lang.legacy("item-explosive-bow-lore").split("\n", -1))
				.make();
	}

}
