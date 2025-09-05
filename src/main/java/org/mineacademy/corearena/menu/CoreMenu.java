package org.mineacademy.corearena.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.menu.MenuRewards.MenuMode;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.ButtonMenu;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.remain.CompMaterial;

public class CoreMenu extends Menu {

	private final Button arenasButton;
	private final Button classesButton;
	private final Button upgradesButton;
	private final Button rewardsButton;

	public CoreMenu() {
		super(null);

		this.setSize(9 * 3);
		this.setTitle(Platform.getPlugin().getName() + " Menu");

		this.arenasButton = new ButtonMenu(MenuArena.class, CompMaterial.CHEST, "&6&lArenas");
		this.classesButton = new ButtonMenu(MenuClasses.class, CompMaterial.IRON_SWORD, "&f&lClasses");
		this.upgradesButton = new ButtonMenu(MenuUpgrades.class, CompMaterial.ENCHANTED_BOOK, "&d&lUpgrades");

		this.rewardsButton = new Button() {

			@Override
			public final void onClickedInMenu(Player pl, Menu menu, ClickType click) {
				MenuRewards.showRewardsMenu(pl, MenuMode.EDIT_ITEMS);

				//new MenuRewards(pl, MenuMode.EDIT_ITEMS).displayTo(pl);
			}

			@Override
			public ItemStack getItem() {
				return ItemCreator.fromMaterial(CompMaterial.DIAMOND).name("&b&lRewards").makeMenuTool();
			}
		};
	}

	@Override
	public final ItemStack getItemAt(int slot) {
		if (slot == 9 + 1)
			return this.arenasButton.getItem();

		if (slot == 9 + 3)
			return this.classesButton.getItem();

		if (slot == 9 + 5)
			return this.upgradesButton.getItem();

		if (slot == 9 + 7)
			return this.rewardsButton.getItem();

		return null;
	}

	@Override
	protected final String[] getInfo() {
		return null;
	}

	@Override
	protected final boolean addReturnButton() {
		return false;
	}
}
