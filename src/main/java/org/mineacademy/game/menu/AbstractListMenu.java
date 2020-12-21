package org.mineacademy.game.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.collection.StrictList;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.model.InventoryDrawer;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.game.conversation.AddNewConvo;
import org.mineacademy.game.conversation.AddNewConvo.Created;

/**
 * An incremental menu that list items.
 */
abstract class AbstractListMenu extends Menu {

	private final String listName;
	private final StrictList<String> list;

	protected AbstractListMenu(String listName, Iterable<String> list) {
		this(new CoreMenu(), listName, list);
	}

	protected AbstractListMenu(Menu parent, String listName, Iterable<String> list) {
		super(parent);

		this.listName = listName;
		this.list = new StrictList<>(list);

		setSize(18 + 9 * (this.list.size() / 9));
		setTitle(listName + " Menu");
	}

	@Override
	protected final List<Button> getButtonsToAutoRegister() {
		final List<Button> items = new ArrayList<>(getSize());

		for (int i = 0; i < list.size(); i++) {
			final Button button = getListButton(list.get(i), i);

			if (button != null)
				items.add(button);
		}

		{
			fillSpace(items, 2);

			if (getAddButton() != null && getCreatedObject() != null && items.size() < getSize())
				items.add(getAddButton());
		}

		/*{
			fillSpace(items, 1);
		
			if (getReturnButton() != null && items.size() < getSize())
				items.add(getReturnButton());
		}*/

		return items;
	}

	private final void fillSpace(List<Button> items, int preserve) {
		for (int i = items.size(); i < getSize() - preserve; i++)
			items.add(Button.makeEmpty());
	}

	protected abstract Button getListButton(String listName, int listIndex);

	protected abstract Created getCreatedObject();

	protected Button getAddButton() {
		final String menuIsAbout = listName.toLowerCase();

		return new Button() {

			@Override
			public final void onClickedInMenu(Player pl, Menu menu, ClickType click) {
				new AddNewConvo(getCreatedObject(), menu).start(pl);
			}

			@Override
			public ItemStack getItem() {
				return ItemCreator
						.of(CompMaterial.EMERALD)
						.name("&a&lCreate new")
						.lores(Arrays.asList(
								"",
								"&7Click to make a new " + menuIsAbout + "."))
						.build().make();
			}
		};
	}

	@Override
	protected final void onDisplay(InventoryDrawer inv) {
		for (final Button item : getButtonsToAutoRegister())
			inv.pushItem(item.getItem());
	}

	@Override
	protected final String[] getInfo() {
		return null;
	}
}
