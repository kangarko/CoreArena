package org.mineacademy.corearena.menu;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.model.Upgrade;
import org.mineacademy.corearena.util.Constants;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.model.MenuClickLocation;
import org.mineacademy.fo.remain.CompSound;

public class UpgradeItemContainerMenu extends Menu {

	private final Upgrade upgrade;

	public UpgradeItemContainerMenu(Menu parent, String upgradeName) {
		super(parent);

		this.setSize(9 * 4);
		this.setTitle("Select Items for This Upgrade");

		this.upgrade = CoreArenaPlugin.getUpgradesManager().findUpgrade(upgradeName);
		Valid.checkNotNull(upgradeName, "Report / Upgrade == null");
	}

	@Override
	public final void onMenuClose(Player pl, Inventory inv) {
		this.upgrade.setItems(Arrays.copyOfRange(inv.getContents(), 0, this.getSize() - 9));

		CompSound.BLOCK_CHEST_CLOSE.play(pl, 1, 1);
		Common.tell(pl, "&2Your changes have been saved.");
	}

	@Override
	public final ItemStack getItemAt(int slot) {
		if (slot < 9 * 3 && this.upgrade.getItems() != null && this.upgrade.getItems().length > slot)
			return this.upgrade.getItems()[slot];

		// Bottom bar
		if (slot > 9 * 3)
			return Constants.Items.MENU_FILLER;

		return null;
	}

	@Override
	public final boolean isActionAllowed(MenuClickLocation clickLocation, int slot, ItemStack clickedSlot, ItemStack cursor, InventoryAction action) {
		return clickLocation == MenuClickLocation.MENU ? slot < 9 * 3 : true;
	}

	@Override
	protected final String[] getInfo() {
		return new String[] {
				"Drag items from your inventory",
				"that you wish the player shall get.",
				"",
				"If he lacks space in his inventory,",
				"items will be dropped near him.",
				"",
				"&8We begin to give the items from",
				"&8the top top left corner."
		};
	}
}