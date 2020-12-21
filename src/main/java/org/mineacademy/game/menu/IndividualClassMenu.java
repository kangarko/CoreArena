package org.mineacademy.game.menu;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.ButtonMenu;
import org.mineacademy.fo.menu.button.ButtonRemove;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.model.MenuClickLocation;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.game.conversation.TierCostConvo;
import org.mineacademy.game.impl.SimpleArmorContent;
import org.mineacademy.game.impl.SimpleTier;
import org.mineacademy.game.model.ArenaClass;
import org.mineacademy.game.model.ClassTier;
import org.mineacademy.game.settings.Localization;
import org.mineacademy.game.util.Constants;

public final class IndividualClassMenu extends Menu {

	private final ArenaClass clazz;

	private final Button editButton;
	private final Button iconButton;
	private final Button removeButton;

	public IndividualClassMenu(String clazz, boolean addReturnButton) {
		this(CoreArenaPlugin.getClassManager().findClass(clazz), addReturnButton);
	}

	public IndividualClassMenu(ArenaClass clazz, boolean addReturnButton) {
		super(addReturnButton ? new MenuClasses() : null);

		setTitle("&0Class " + clazz.getName() + " Menu");

		this.clazz = clazz;

		editButton = new ButtonMenu(new ClassPreviewMenu(1),
				ItemCreator
						.of(CompMaterial.WRITABLE_BOOK,
								"&a&lEdit class",
								"",
								"Edit what items the player",
								"shall receive when they",
								"equip the class."));

		iconButton = IconMenu.asButton(clazz, this);

		removeButton = new ButtonRemove(this, "class", clazz.getName(), object -> {
			CoreArenaPlugin.getClassManager().removeClass(object);

			new MenuClasses().displayTo(getViewer());
		});
	}

	@Override
	public ItemStack getItemAt(int slot) {
		if (slot == 9 + 1)
			return editButton.getItem();

		if (slot == 9 + 3)
			return iconButton.getItem();

		if (slot == 9 + 5)
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

	private final class ClassPreviewMenu extends Menu {

		/** 45 */
		private final static int ARMOR_SLOT_BEGINNING_INDEX = 45;
		/** 100 */
		private final static int TIER_LIMIT = 100;

		private final int tier;

		private final Button priceButton;
		private final Button tierDownButton;
		private final Button tierUpButton;

		private ClassPreviewMenu(int tier) {
			super(IndividualClassMenu.this);

			setSize(9 * 6);
			setTitle("&0Editing " + clazz.getName() + " Tier " + tier);

			this.tier = tier;

			final ClassTier existingTier = getTierIfExist();

			priceButton = tier != 1 && existingTier != null ? new Button() {

				@Override
				public void onClickedInMenu(Player pl, Menu menu, ClickType click) {
					new TierCostConvo(ClassPreviewMenu.this, clazz, existingTier).start(pl);
				}

				@Override
				public ItemStack getItem() {
					final ClassTier tier = getTierIfExist();

					return ItemCreator
							.of(CompMaterial.GOLD_NUGGET)
							.name("&ePrice: " + Localization.Currency.format(tier != null ? tier.getLevelCost() : getDefaultCost()))
							.lores(Arrays.asList(
									"",
									"&7Click to set the price."))
							.build().makeMenuTool();
				}
			}
					: Button.makeDummy(tier == 1 ? ItemCreator.of(
							CompMaterial.FEATHER,
							"&eThis is the initial tier",
							"",
							"The first tier is available",
							"to everyone from the start.",
							"",
							"Players can upgrade to a higher",
							"tier as they receive Nuggets.")
							: ItemCreator.of(
									CompMaterial.GOLD_NUGGET,
									"&7&oTier not yet created",
									" ",
									"Place some items here and return.",
									"The tier will be automatically saved."));

			tierDownButton = new Button() {

				@Override
				public void onClickedInMenu(Player pl, Menu menu, ClickType click) {
					if (tier > 1)
						new ClassPreviewMenu(tier - 1).displayTo(pl);
				}

				@Override
				public ItemStack getItem() {
					return ItemCreator.of(
							tier == 1 ? CompMaterial.GRAY_DYE : CompMaterial.PINK_DYE,
							"&f" + (tier == 1 ? "This tier is the starting one" : "Edit lower tier")).build().makeMenuTool();
				}
			};

			tierUpButton = new Button() {

				@Override
				public void onClickedInMenu(Player pl, Menu menu, ClickType click) {
					if (tier <= TIER_LIMIT && getTierIfExist() != null)
						new ClassPreviewMenu(tier + 1).displayTo(pl);
				}

				@Override
				public ItemStack getItem() {
					return (getTierIfExist() == null
							? ItemCreator.of(CompMaterial.GRAY_DYE, "&7Create this tier first!")
							: ItemCreator.of(tier <= TIER_LIMIT ? CompMaterial.LIME_DYE : CompMaterial.GRAY_DYE).name("&f" + (tier <= TIER_LIMIT ? "Edit higher tier" : "This is the maximum tier"))).build().makeMenuTool();
				}
			};
		}

		@Override
		public void onMenuClose(Player pl, Inventory inv) {
			// Valid.checkBoolean(inv.getSize() == getSize(), "Report / Illegal size " + inv.getSize() + " != " + getSize());

			final int normalInvSize = PlayerUtil.USABLE_PLAYER_INV_SIZE;

			final ItemStack[] content = Arrays.copyOfRange(inv.getContents(), 0, normalInvSize);
			final ItemStack[] armorContent = Arrays.copyOfRange(inv.getContents(), ARMOR_SLOT_BEGINNING_INDEX, ARMOR_SLOT_BEGINNING_INDEX + 4);

			final ClassTier existing = getTierIfExist();

			// Completely empty
			if (Valid.isNullOrEmpty(content) && Valid.isNullOrEmpty(armorContent)) {

				// Not yet saved
				if (existing == null)
					return;

				// Remove empty tier
				clazz.removeTier(existing);
				return;
			}

			final SimpleArmorContent armor = SimpleArmorContent.fromItemStacks(armorContent);

			final SimpleTier tierClass = new SimpleTier(clazz.getName(), tier, existing != null ? existing.getLevelCost() : getDefaultCost(), content, armor);
			clazz.addOrUpdateTier(tierClass);

			CompSound.CHEST_CLOSE.play(pl, 1, 1);
			Common.tell(pl, "&2Your changes have been saved.");
		}

		@Override
		public ItemStack getItemAt(int slot) {

			{ // Tier Content
				final ClassTier tier = getTierIfExist();

				if (tier != null) {
					if (slot < 36 && tier.getContent() != null)
						return tier.getContent()[slot];

					if (slot >= ARMOR_SLOT_BEGINNING_INDEX && slot <= ARMOR_SLOT_BEGINNING_INDEX + 3 && tier.getArmor() != null)
						return tier.getArmor().getByOrder(slot % ARMOR_SLOT_BEGINNING_INDEX);
				}
			}

			{ // Armor Info
				if (slot == 9 * 4)
					return makeArmorSlot(1, "helmet");

				if (slot == 9 * 4 + 1)
					return makeArmorSlot(2, "chestplate");

				if (slot == 9 * 4 + 2)
					return makeArmorSlot(3, "leggings");

				if (slot == 9 * 4 + 3)
					return makeArmorSlot(4, "boots");
			}

			{ // Functional and helper buttons
				if (slot == 9 * 5 + 4)
					return priceButton.getItem();

				if (slot == 9 * 5 + 6)
					return tierDownButton.getItem();

				if (slot == 9 * 5 + 7)
					return tierUpButton.getItem();

				if (slot >= 9 * 4 && slot < 9 * 5 || slot >= 49)
					return ItemCreator.of(CompMaterial.GRAY_STAINED_GLASS_PANE).name(" ").build().make();
			}

			return null;
		}

		private ItemStack makeArmorSlot(int amount, String name) {
			return ItemCreator.of(CompMaterial.GRAY_STAINED_GLASS_PANE).amount(amount).name("&7Place &f" + name + " &7below:").build().makeMenuTool();
		}

		@Override
		protected int getInfoButtonPosition() {
			return 9 * 5 + 5;
		}

		@Override
		public boolean isActionAllowed(MenuClickLocation clickLocation, int slot, ItemStack clickedSlot, ItemStack cursor) {
			final boolean armorSlot = slot >= ARMOR_SLOT_BEGINNING_INDEX && slot <= ARMOR_SLOT_BEGINNING_INDEX + 3;
			final String type = slot == ARMOR_SLOT_BEGINNING_INDEX ? "helmet" : slot == ARMOR_SLOT_BEGINNING_INDEX + 1 ? "chestplate" : slot == ARMOR_SLOT_BEGINNING_INDEX + 2 ? "leggings" : slot == ARMOR_SLOT_BEGINNING_INDEX + 3 ? "boots" : "error";

			return armorSlot ? checkValidArmor(clickedSlot, cursor, type) : clickLocation != MenuClickLocation.OUTSIDE && slot < 9 * 4;
		}

		private ClassTier getTierIfExist() {
			return clazz.getTier(tier);
		}

		private int getDefaultCost() {
			return (int) MathUtil.calculate(Constants.Costs.TIER_STARTING_COST.replace("{currentTier}", tier + ""));
		}

		private boolean checkValidArmor(ItemStack clickedSlot, ItemStack cursor, String type) {
			type = type.toUpperCase();

			class $ {
				private boolean isValid(ItemStack item, String checkAgainst) {
					final Material type = item.getType();

					if (checkAgainst.equals("HELMET"))
						return type.toString().contains(checkAgainst) || CompMaterial.isSkull(type);

					return type.toString().contains(checkAgainst);
				}
			}

			final $ $ = new $();

			if (cursor.getType() != Material.AIR)
				if (!$.isValid(cursor, type)) {
					animateTitle("&4Only insert " + type.toLowerCase() + (type.toLowerCase().endsWith("s") ? "" : "s") + " here!");

					if (clickedSlot != null)
						clickedSlot.setAmount(0);

					return false;
				}

			return true;
		}

		@Override
		public Menu newInstance() {
			return new ClassPreviewMenu(tier);
		}

		@Override
		protected String[] getInfo() {
			return new String[] {
					"Edit the items in this class",
					"as you'd like.",
					" ",
					"When finished, simply close",
					"this inventory or return back."
			};
		}
	}
}
