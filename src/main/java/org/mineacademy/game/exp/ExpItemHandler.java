package org.mineacademy.game.exp;

import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.EntityUtil;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.MinecraftVersion.V;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.model.SimpleEnchant;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.game.model.ExpItem;
import org.mineacademy.game.settings.Settings;
import org.mineacademy.game.util.Constants;

public final class ExpItemHandler {

	public static void spawn(Location location, int exp) {
		if (Settings.Experience.ITEM == CompMaterial.AIR)
			return;

		final ItemStack item = createItem(exp);
		final Item spawned;

		final Consumer<Item> decorator = i -> {
			Remain.setCustomName(i, item.getItemMeta().getDisplayName());

			i.setPickupDelay(10);
			addDroppedTag(i, new ExpItem(exp));
		};

		if (MinecraftVersion.olderThan(V.v1_8)) {
			spawned = location.getWorld().dropItem(location, item);
			decorator.accept(spawned);

		} else
			spawned = EntityUtil.dropItem(location, item, decorator);

		Valid.checkNotNull(spawned, "Report / Unable to spawn experience item");
	}

	private static ItemStack createItem(int exp) {
		return ItemCreator
				.of(Settings.Experience.ITEM)
				.name(Settings.Experience.ITEM_LABEL.replace("{amount}", exp + ""))
				.enchant(new SimpleEnchant(Enchantment.DURABILITY))
				.build().make();
	}

	public static ExpItem getExpItem(Item item) {
		Valid.checkBoolean(isExpItem(item), "Item " + item.getItemStack().getType() + " does not have dropped tag");

		return (ExpItem) item.getMetadata(Constants.Items.EXP_ITEM_TAG).get(0).value();
	}

	public static boolean isExpItem(Entity item) {
		return item.hasMetadata(Constants.Items.EXP_ITEM_TAG);
	}

	static void addDroppedTag(Item item, ExpItem tag) {
		Valid.checkBoolean(!isExpItem(item), "Item " + item.getItemStack().getType() + " has already tag");

		item.setMetadata(Constants.Items.EXP_ITEM_TAG, new FixedMetadataValue(CoreArenaPlugin.getInstance(), tag));
	}
}