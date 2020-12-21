package org.mineacademy.game.menu;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.model.MenuClickLocation;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.game.model.Upgrade;
import org.mineacademy.game.util.Constants;

public class UpgradeItemContainerMenu extends Menu {

	private final Upgrade upgrade;

	public UpgradeItemContainerMenu(Menu parent, String upgradeName) {
		super(parent);

		setSize(9 * 4);
		setTitle("Select Items for This Upgrade");

		upgrade = CoreArenaPlugin.getUpgradesManager().findUpgrade(upgradeName);
		Valid.checkNotNull(upgradeName, "Report / Upgrade == null");
	}

	@Override
	public final void onMenuClose(Player pl, Inventory inv) {
		upgrade.setItems(Arrays.copyOfRange(inv.getContents(), 0, getSize() - 9));

		CompSound.CHEST_CLOSE.play(pl, 1, 1);
		Common.tell(pl, "&2Your changes have been saved.");
	}

	@Override
	public final ItemStack getItemAt(int slot) {
		if (slot < 9 * 3 && upgrade.getItems() != null && upgrade.getItems().length > slot)
			return upgrade.getItems()[slot];

		// Bottom bar
		if (slot > 9 * 3)
			return Constants.Items.MENU_FILLER;

		return null;
	}

	@Override
	public final boolean isActionAllowed(MenuClickLocation clickLocation, int slot, ItemStack clickedSlot, ItemStack cursor) {
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