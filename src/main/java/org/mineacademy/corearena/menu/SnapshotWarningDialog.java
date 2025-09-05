package org.mineacademy.corearena.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.data.AllData;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;

public class SnapshotWarningDialog extends Menu {

	private final Button enterButton;

	public SnapshotWarningDialog(Menu snapshotMenu) {
		super(null);

		this.setTitle("Enter Snapshot");

		this.enterButton = new Button() {

			@Override
			public final void onClickedInMenu(Player pl, Menu menu, ClickType click) {
				snapshotMenu.displayTo(pl);

				AllData.getInstance().setSnapshotNotified();
			}

			@Override
			public ItemStack getItem() {
				return ItemCreator
						.fromMaterial(CompMaterial.OAK_DOOR)
						.name("&6&lEnter Menu")
						.lore(
								"",
								"&7Arena snapshots must be rebuilt",
								"&7on the main server thread.",
								"",
								"&cThe bigger your arena is, the longer",
								"&cit will take to render the snapshot.",
								"&7Server may experience a short lag",
								"&7on the start and end of the game.",
								"",
								"&7Please adjust your arenas' size",
								"&7according to your server hardware.",
								"",
								"&6Click to enter the menu")
						.makeMenuTool();
			}
		};
	}

	@Override
	public ItemStack getItemAt(int slot) {
		if (slot == 9 + 4)
			return this.enterButton.getItem();

		return ItemCreator.fromMaterial(CompMaterial.RED_STAINED_GLASS_PANE).name(" ").make();
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
