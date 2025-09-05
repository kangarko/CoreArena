package org.mineacademy.corearena.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.conversation.AddNewConvo.Created;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;

public class MenuUpgrades extends AbstractListMenu {

	protected MenuUpgrades() {
		super("Upgrade", CoreArenaPlugin.getUpgradesManager().getAvailable());
	}

	@Override
	protected final Button getListButton(String listName, int listIndex) {
		return new UpgradeButton(listName, listIndex);
	}

	@Override
	protected final Created getCreatedObject() {
		return Created.UPGRADE;
	}

	private static final class UpgradeButton extends Button {

		private final String upgrade;
		private final int colorMod;

		public UpgradeButton(String upgrade, int colorOffset) {
			this.upgrade = upgrade;
			this.colorMod = colorOffset % 15;
		}

		@Override
		public void onClickedInMenu(Player pl, Menu menu, ClickType click) {
			new IndividualUpgradeMenu(this.upgrade, true).displayTo(pl);
		}

		@Override
		public ItemStack getItem() {
			return ItemCreator.fromMaterial(CompMaterial.fromLegacy("WOOL", this.colorMod)).name(this.upgrade).lore("", "&7Click to open upgrade menu.").make();
		}
	}
}
