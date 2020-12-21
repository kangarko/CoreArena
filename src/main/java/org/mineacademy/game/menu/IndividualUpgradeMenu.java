package org.mineacademy.game.menu;

import java.util.Arrays;

import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.ButtonMenu;
import org.mineacademy.fo.menu.button.ButtonRemove;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.game.model.Upgrade;

public final class IndividualUpgradeMenu extends Menu {

	private final Button setItemsButton;

	private final Button removeButton;

	public IndividualUpgradeMenu(String upgrade, boolean returnButton) {
		this(CoreArenaPlugin.getUpgradesManager().findUpgrade(upgrade), returnButton);
	}

	public IndividualUpgradeMenu(Upgrade upgrade, boolean addReturnButton) {
		super(addReturnButton ? new MenuUpgrades() : null);

		setTitle("&0Upgrade " + upgrade.getName() + " Menu");

		Valid.checkNotNull(upgrade, "Report / Upgrade == null");

		setItemsButton = new ButtonMenu(new UpgradeItemContainerMenu(this, upgrade.getName()),
				ItemCreator
						.of(CompMaterial.ANVIL)
						.name("&fItems to Give")

						.lores(
								Arrays.asList(
										"&r",
										"&7Specify which items this upgrade gives.",
										"",
										"&7Once player buys the upgrade,",
										"&7he'll receive all the items")));

		removeButton = new ButtonRemove(this, "upgrade", upgrade.getName(), object -> {
			CoreArenaPlugin.getUpgradesManager().removeUpgrade(object);

			new MenuUpgrades().displayTo(getViewer());
		});
	}

	@Override
	public ItemStack getItemAt(int slot) {
		if (slot == 9 + 1)
			return setItemsButton.getItem();

		if (slot == 9 + 4)
			return removeButton.getItem();

		return null;
	}

	@Override
	protected int getReturnButtonPosition() {
		return 9 + 7;
	}

	@Override
	protected String[] getInfo() {
		return null;
	}
}
