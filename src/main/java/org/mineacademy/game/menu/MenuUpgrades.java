package org.mineacademy.game.menu;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.game.conversation.AddNewConvo.Created;

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
			new IndividualUpgradeMenu(upgrade, true).displayTo(pl);
		}

		@Override
		public ItemStack getItem() {
			return ItemCreator.of(CompMaterial.fromLegacy("WOOL", colorMod)).name(upgrade).lores(Arrays.asList("", "&7Click to open upgrade menu.")).build().make();
		}
	}
}
