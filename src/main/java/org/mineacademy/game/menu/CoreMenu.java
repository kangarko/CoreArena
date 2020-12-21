package org.mineacademy.game.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.ButtonMenu;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.game.menu.MenuRewards.MenuMode;

public class CoreMenu extends Menu {

	private final Button arenasButton;
	private final Button classesButton;
	private final Button upgradesButton;
	private final Button rewardsButton;

	public CoreMenu() {
		super(null);

		setSize(9 * 3);
		setTitle(SimplePlugin.getNamed() + " Menu");

		arenasButton = new ButtonMenu(MenuArena.class, CompMaterial.CHEST, "&6&lArenas");
		classesButton = new ButtonMenu(MenuClasses.class, CompMaterial.IRON_SWORD, "&f&lClasses");
		upgradesButton = new ButtonMenu(MenuUpgrades.class, CompMaterial.ENCHANTED_BOOK, "&d&lUpgrades");

		rewardsButton = new Button() {

			@Override
			public final void onClickedInMenu(Player pl, Menu menu, ClickType click) {
				MenuRewards.showRewardsMenu(pl, MenuMode.EDIT_ITEMS);

				//new MenuRewards(pl, MenuMode.EDIT_ITEMS).displayTo(pl);
			}

			@Override
			public ItemStack getItem() {
				return ItemCreator.of(CompMaterial.DIAMOND).name("&b&lRewards").build().makeMenuTool();
			}
		};
	}

	@Override
	public final ItemStack getItemAt(int slot) {
		if (slot == 9 + 1)
			return arenasButton.getItem();

		if (slot == 9 + 3)
			return classesButton.getItem();

		if (slot == 9 + 5)
			return upgradesButton.getItem();

		if (slot == 9 + 7)
			return rewardsButton.getItem();

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
