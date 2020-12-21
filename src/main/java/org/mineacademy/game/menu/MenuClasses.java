package org.mineacademy.game.menu;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.game.conversation.AddNewConvo.Created;
import org.mineacademy.game.model.ArenaClass;

public class MenuClasses extends AbstractListMenu {

	@Deprecated
	private ClassMenuClickListener menuMaker;

	protected MenuClasses() {
		this(null);
	}

	protected MenuClasses(Menu parent) {
		super(parent != null ? parent : new CoreMenu(), "Class", CoreArenaPlugin.getClassManager().getAvailable());

		if (getCustomTitle() != null)
			setTitle(getCustomTitle());
	}

	protected String getCustomTitle() {
		return null;
	}

	protected final void setMenuMaker(ClassMenuClickListener menuMaker) {
		Valid.checkBoolean(this.menuMaker == null, "Already set!");

		this.menuMaker = menuMaker;
	}

	@Override
	protected Button getListButton(String listName, int listIndex) {
		return new ClassButton(listName);
	}

	@Override
	protected final Created getCreatedObject() {
		return Created.CLASS;
	}

	private final class ClassButton extends Button {

		private final ArenaClass clazz;

		private ClassButton(String className) {
			this(CoreArenaPlugin.getClassManager().findClass(className));
		}

		public ClassButton(ArenaClass clazz) {
			this.clazz = clazz;
		}

		@Override
		public void onClickedInMenu(Player pl, Menu menu, ClickType click) {
			final Menu newMenu = menuMaker != null ? menuMaker.getMenuForClass(clazz) : new IndividualClassMenu(clazz, true);

			newMenu.displayTo(pl);
		}

		@Override
		public ItemStack getItem() {
			return ItemCreator.of(clazz.getIcon()).name("&f" + clazz.getName()).lores(Arrays.asList("", "&7Click to open class menu.")).hideTags(true).build().make();
		}
	}

	public interface ClassMenuClickListener {
		Menu getMenuForClass(ArenaClass clazz);
	}
}
