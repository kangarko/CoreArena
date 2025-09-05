package org.mineacademy.corearena.menu;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.model.Iconable;
import org.mineacademy.fo.ChatUtil;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.ButtonMenu;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.model.MenuClickLocation;
import org.mineacademy.fo.remain.CompMaterial;

class IconMenu extends Menu {

	private final static int ICON_SLOT = 13;

	private final static List<Integer> DECORATION_SLOTS = Arrays.asList(
			3, 4, 5,
			12, 14,
			21, 22, 23);

	private final Iconable iconable;

	private IconMenu(Iconable iconable, Menu parent) {
		super(parent);

		this.setTitle("Set " + ChatUtil.capitalize(iconable.getObjectName()) + " Icon");

		this.iconable = iconable;
	}

	@Override
	public final ItemStack getItemAt(int slot) {
		if (DECORATION_SLOTS.contains(slot))
			return ItemCreator.from(CompMaterial.MAGENTA_STAINED_GLASS_PANE, " ").makeMenuTool();

		if (slot == ICON_SLOT)
			return this.iconable.hasIcon() ? this.iconable.getIcon() : null;

		return NO_ITEM;
	}

	@Override
	protected int getInfoButtonPosition() {
		return 9 + 1;
	}

	@Override
	protected final int getReturnButtonPosition() {
		return 9 + 7;
	}

	@Override
	public final void onMenuClose(Player player, Inventory inventory) {
		final ItemStack icon = inventory.getItem(ICON_SLOT);

		this.iconable.setIcon(icon != null && icon.getType() != Material.AIR ? icon : null);
	}

	@Override
	public final boolean isActionAllowed(MenuClickLocation clickLocation, int slot, ItemStack clickedSlot, ItemStack cursor, InventoryAction action) {
		return clickLocation == MenuClickLocation.PLAYER_INVENTORY || clickLocation == MenuClickLocation.MENU && slot == ICON_SLOT;
	}

	@Override
	protected final String[] getInfo() {
		return new String[] {
				"Place your icon inside",
				"the fiolett circle and",
				"return to save changes.",
				"",
				"Remove to show the default icon."
		};
	}

	public static final ButtonMenu createButtonMenu(Iconable iconable, Menu parent) {
		return new ButtonMenu(new IconMenu(iconable, parent), ItemCreator.from(CompMaterial.GLASS,
				"&f&lEdit Icon",
				" ",
				"Set the " + iconable.getObjectName() + " icon as",
				"it appears in the menu."));
	}
}