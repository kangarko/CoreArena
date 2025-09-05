package org.mineacademy.corearena.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.conversation.AddNewConvo;
import org.mineacademy.corearena.conversation.AddNewConvo.Created;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.model.InventoryDrawer;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;

/**
 * An incremental menu that list items.
 */
abstract class AbstractListMenu extends Menu {

	private final String listName;
	private final List<String> list;

	protected AbstractListMenu(String listName, Iterable<String> list) {
		this(new CoreMenu(), listName, list);
	}

	protected AbstractListMenu(Menu parent, String listName, Iterable<String> list) {
		super(parent);

		this.listName = listName;
		this.list = new ArrayList<>();

		for (final String string : list)
			this.list.add(string);

		this.setSize(18 + 9 * (this.list.size() / 9));
		this.setTitle(listName + " Menu");
	}

	@Override
	protected final List<Button> getButtonsToAutoRegister() {
		final List<Button> items = new ArrayList<>(this.getSize());

		for (int i = 0; i < this.list.size(); i++) {
			final Button button = this.getListButton(this.list.get(i), i);

			if (button != null)
				items.add(button);
		}

		{
			this.fillSpace(items, 2);

			if (this.getAddButton() != null && this.getCreatedObject() != null && items.size() < this.getSize())
				items.add(this.getAddButton());
		}

		/*{
			fillSpace(items, 1);

			if (getReturnButton() != null && items.size() < getSize())
				items.add(getReturnButton());
		}*/

		return items;
	}

	private final void fillSpace(List<Button> items, int preserve) {
		for (int i = items.size(); i < this.getSize() - preserve; i++)
			items.add(Button.makeEmpty());
	}

	protected abstract Button getListButton(String listName, int listIndex);

	protected abstract Created getCreatedObject();

	protected Button getAddButton() {
		final String menuIsAbout = this.listName.toLowerCase();

		return new Button() {

			@Override
			public final void onClickedInMenu(Player pl, Menu menu, ClickType click) {
				new AddNewConvo(AbstractListMenu.this.getCreatedObject(), menu).start(pl);
			}

			@Override
			public ItemStack getItem() {
				return ItemCreator
						.fromMaterial(CompMaterial.EMERALD)
						.name("&a&lCreate new")
						.lore(
								"",
								"&7Click to make a new " + menuIsAbout + ".")
						.make();
			}
		};
	}

	@Override
	protected final void onPreDisplay(InventoryDrawer inv) {
		for (final Button item : this.getButtonsToAutoRegister())
			inv.pushItem(item.getItem());
	}

	@Override
	protected final String[] getInfo() {
		return null;
	}
}
