package org.mineacademy.corearena.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.data.AllData.ArenaPlayer;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.model.ArenaClass;
import org.mineacademy.corearena.model.ArenaMenu;
import org.mineacademy.corearena.model.ClassTier;
import org.mineacademy.corearena.type.MenuType;
import org.mineacademy.corearena.type.TierMode;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.menu.MenuPaged;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.settings.Lang;

public final class MenuInArenaClasses extends MenuPaged<ArenaClass> implements ArenaMenu {

	private final Arena arena;
	private final ArenaPlayer data;

	public MenuInArenaClasses(Arena arena, Player player) {
		super(9 * 1, arena.getSettings().allowOwnEquipment() ? new ArrayList<>()
				: CoreArenaPlugin.getClassManager().getClasses()
						.stream()
						.filter(item -> item.mayObtain(player))
						.collect(Collectors.toList()));

		this.arena = arena;
		this.data = CoreArenaPlugin.getDataFor(player);

		this.setTitle(Lang.legacy("menu-classes-title"));
	}

	@Override
	protected ItemStack convertToItemStack(ArenaClass item) {
		return item.isValid() ? this.buildItem(item)
				: ItemCreator.from(
						CompMaterial.WHITE_STAINED_GLASS_PANE, "&f" + item.getName(),
						"",
						Lang.legacy("menu-classes-not-configured")).make();
	}

	private ItemStack buildItem(ArenaClass clazz) {
		final ClassTier tier = clazz.getMinimumTier(this.data.getTierOf(clazz));
		final List<String> lore = new ArrayList<>();

		if (tier != null) {
			lore.add(Lang.legacy("menu-classes-tier", "tier", tier.getTier()));
			lore.add("");

			if (this.isEligibleForTier(clazz))
				lore.add(Lang.legacy("menu-classes-click-to-equip"));

			else
				for (final String line : Lang.legacy("menu-classes-not-enough-tier", "tier", this.arena.getSettings().getMinimumTier()).split("\n"))
					lore.add(line);
		} else
			lore.add(Lang.legacy("menu-classes-not-configured"));

		return ItemCreator
				.fromItemStack(clazz.getIcon())
				.name("&f" + clazz.getName())
				.lore(lore)
				.hideTags(true)
				.make();
	}

	@Override
	protected void onPageClick(Player player, ArenaClass clazz, ClickType click) {
		final ClassTier tier = clazz.getMinimumTier(this.data.getTierOf(clazz));

		if (tier == null)
			return;

		if (!this.isEligibleForTier(clazz)) {
			this.animateTitle(Lang.legacy("menu-classes-requires-higher-tier"));

			return;
		}

		player.closeInventory();
		clazz.giveToPlayer(player, TierMode.PREVIEW, true);
	}

	private boolean isEligibleForTier(ArenaClass clazz) {
		Valid.checkNotNull(this.arena, "Arena == null");

		return this.data.getTierOf(clazz) >= this.arena.getSettings().getMinimumTier();
	}

	@Override
	protected boolean addReturnButton() {
		return false;
	}

	@Override
	protected String[] getInfo() {
		return null;
	}

	@Override
	public MenuType getMenuType() {
		return MenuType.CLASSES;
	}
}