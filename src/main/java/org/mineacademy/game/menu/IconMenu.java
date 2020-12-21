package org.mineacademy.game.menu;

import java.util.Arrays;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.ButtonMenu;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.model.MenuClickLocation;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.game.model.Iconable;

class IconMenu extends Menu {
	public static final List<String> LORES = Arrays.asList(
			"&r",
			"&7Set the class icon as",
			"&7it appears in the menu.");

	public static final String EDIT_ICON = "&f&lEdit Icon";

	public static final String MENU_ICON_TITLE = "&0Set Class Icon";

	public static final String[] DESCRIPTION = {
			"Place your icon inside",
			"the fiolett circle and",
			"return to save changes.",
			"",
			"Remove to show the default icon."
	};

	private final static int ICON_SLOT = 13;

	private final static ItemStack decorationItem = ItemCreator.of(CompMaterial.fromLegacy("STAINED_GLASS_PANE", DyeColor.MAGENTA.getWoolData())).name(" ").build().makeMenuTool();
	private final static List<Integer> decorationSlots = Arrays.asList(
			3, 4, 5,
			12, 14,
			21, 22, 23);

	public static final Button asButton(Iconable iconable, Menu parent) {

		return new ButtonMenu(new IconMenu(iconable, parent),
				ItemCreator
						.of(CompMaterial.GLASS)
						.name(EDIT_ICON)
						.lores(LORES));
	}

	private final Iconable iconable;

	private IconMenu(Iconable iconable, Menu parent) {
		super(parent);

		setTitle(MENU_ICON_TITLE);

		this.iconable = iconable;
	}

	@Override
	public final ItemStack getItemAt(int slot) {
		if (decorationSlots.contains(slot))
			return decorationItem;

		if (slot == ICON_SLOT)
			return iconable.hasIcon() ? iconable.getIcon() : null;

		return null;
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
	public final void onMenuClose(Player pl, Inventory inv) {
		final ItemStack icon = inv.getItem(ICON_SLOT);

		iconable.setIcon(icon != null && icon.getType() != Material.AIR ? icon : null);
	}

	@Override
	public final boolean isActionAllowed(MenuClickLocation clickLocation, int slot, ItemStack clickedSlot, ItemStack cursor) {
		return clickLocation == MenuClickLocation.PLAYER_INVENTORY || clickLocation == MenuClickLocation.MENU && slot == ICON_SLOT;
	}

	@Override
	protected final String[] getInfo() {
		return DESCRIPTION;
	}
}