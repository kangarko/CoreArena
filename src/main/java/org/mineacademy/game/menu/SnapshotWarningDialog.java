package org.mineacademy.game.menu;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.game.data.GeneralDataSection;

public class SnapshotWarningDialog extends Menu {

	private final Button enterButton;

	public SnapshotWarningDialog(Menu snapshotMenu) {
		super(null);

		setTitle("Enter Snapshot");

		this.enterButton = new Button() {

			@Override
			public final void onClickedInMenu(Player pl, Menu menu, ClickType click) {
				snapshotMenu.displayTo(pl);

				GeneralDataSection.getInstance().setSnapshotNotified();
			}

			@Override
			public ItemStack getItem() {
				return ItemCreator
						.of(CompMaterial.OAK_DOOR)
						.name("&6&lEnter Menu")
						.lores(Arrays.asList(
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
								"&6Click to enter the menu"))
						.build().makeMenuTool();
			}
		};
	}

	@Override
	public ItemStack getItemAt(int slot) {
		if (slot == 9 + 4)
			return enterButton.getItem();

		return ItemCreator.of(CompMaterial.RED_STAINED_GLASS_PANE).name(" ").build().make();
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
