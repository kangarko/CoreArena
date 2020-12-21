package org.mineacademy.game.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.StrictList;
import org.mineacademy.fo.menu.MenuPagged;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.game.impl.ArenaPlayer;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.model.ArenaClass;
import org.mineacademy.game.model.ArenaMenu;
import org.mineacademy.game.model.ClassTier;
import org.mineacademy.game.settings.Localization;
import org.mineacademy.game.type.MenuType;
import org.mineacademy.game.type.TierMode;

public final class MenuInArenaClasses extends MenuPagged<ArenaClass> implements ArenaMenu {

	private final Arena arena;
	private final ArenaPlayer data;
	private final Player player;

	public MenuInArenaClasses(Arena arena, Player player) {
		super(9 * 1, arena.getSettings().allowOwnEquipment() ? new StrictList<>() : CoreArenaPlugin.getClassManager().getClasses());

		this.arena = arena;
		this.data = CoreArenaPlugin.getDataFor(player);
		this.player = player;

		setTitle("Select Your Class");
	}

	@Override
	protected ItemStack convertToItemStack(ArenaClass item) {
		if (!item.mayObtain(player))
			return null;

		return item.isValid() ? buildItem(item) : ItemCreator.of(CompMaterial.WHITE_STAINED_GLASS_PANE, "&f" + item.getName(), "", "&c&oClass not yet configured!").build().make();
	}

	private ItemStack buildItem(ArenaClass clazz) {
		final ClassTier tier = clazz.getMinimumTier(data.getTierOf(clazz));
		final List<String> lore = new ArrayList<>();

		if (tier != null) {
			lore.add("&8Tier " + tier.getTier());
			lore.add("");

			if (isEligibleForTier(clazz))
				lore.add("&7Click to equip this class.");

			else
				lore.addAll(Arrays.asList(
						"&cThe arena requires",
						"&cat least Tier " + arena.getSettings().getMinimumTier()));
		} else
			lore.add("&7This class is not yet configured.");

		return ItemCreator
				.of(clazz.getIcon())
				.name("&f" + clazz.getName())
				.lores(lore)
				.hideTags(true)
				.build().make();
	}

	@Override
	protected void onPageClick(Player player, ArenaClass clazz, ClickType click) {
		final ClassTier tier = clazz.getMinimumTier(data.getTierOf(clazz));

		if (tier == null)
			return;

		if (!isEligibleForTier(clazz)) {
			animateTitle("&4Arena requires higher tier!");

			return;
		}

		player.closeInventory();
		clazz.giveToPlayer(player, TierMode.PREVIEW);

		CompSound.ENDERDRAGON_WINGS.play(player, 1F, 1F);
		Common.tell(player, Localization.Class.SELECTED.replace("{class}", clazz.getName()));
	}

	private boolean isEligibleForTier(ArenaClass clazz) {
		Valid.checkNotNull(arena, "Arena == null");

		return data.getTierOf(clazz) >= arena.getSettings().getMinimumTier();
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