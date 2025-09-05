package org.mineacademy.corearena.menu;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.conversation.TierCostConvo;
import org.mineacademy.corearena.impl.SimpleArmorContent;
import org.mineacademy.corearena.impl.SimpleTier;
import org.mineacademy.corearena.model.ArenaClass;
import org.mineacademy.corearena.model.ClassTier;
import org.mineacademy.corearena.util.Constants;
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
import org.mineacademy.fo.settings.Lang;

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

		this.setTitle("&0Class " + clazz.getName() + " Menu");

		this.clazz = clazz;

		this.editButton = new ButtonMenu(new ClassPreviewMenu(1),
				ItemCreator
						.from(CompMaterial.WRITABLE_BOOK,
								"&a&lEdit class",
								"",
								"Edit what items the player",
								"shall receive when they",
								"equip the class."));

		this.iconButton = IconMenu.createButtonMenu(clazz, this);

		this.removeButton = new ButtonRemove(this, "class", clazz.getName(), () -> {
			CoreArenaPlugin.getClassManager().removeClass(clazz.getName());

			new MenuClasses().displayTo(this.getViewer());
		});
	}

	@Override
	public ItemStack getItemAt(int slot) {
		if (slot == 9 + 1)
			return this.editButton.getItem();

		if (slot == 9 + 3)
			return this.iconButton.getItem();

		if (slot == 9 + 5)
			return this.removeButton.getItem();

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

			this.setSize(9 * 6);
			this.setTitle("&0Editing " + IndividualClassMenu.this.clazz.getName() + " Tier " + tier);

			this.tier = tier;

			final ClassTier existingTier = this.getTierIfExist();

			this.priceButton = tier != 1 && existingTier != null ? new Button() {

				@Override
				public void onClickedInMenu(Player pl, Menu menu, ClickType click) {
					new TierCostConvo(ClassPreviewMenu.this, IndividualClassMenu.this.clazz, existingTier).start(pl);
				}

				@Override
				public ItemStack getItem() {
					final ClassTier tier = ClassPreviewMenu.this.getTierIfExist();

					return ItemCreator
							.fromMaterial(CompMaterial.GOLD_NUGGET)
							.name("&ePrice: " + Lang.numberFormat("currency-name", tier != null ? tier.getLevelCost() : ClassPreviewMenu.this.getDefaultCost()))
							.lore(
									"",
									"&7Click to set the price.")
							.makeMenuTool();
				}
			}
					: Button.makeDummy(tier == 1 ? ItemCreator.from(
							CompMaterial.FEATHER,
							"&eThis is the initial tier",
							"",
							"The first tier is available",
							"to everyone from the start.",
							"",
							"Players can upgrade to a higher",
							"tier as they receive Nuggets.")
							: ItemCreator.from(
									CompMaterial.GOLD_NUGGET,
									"&7&oTier not yet created",
									" ",
									"Place some items here and return.",
									"The tier will be automatically saved."));

			this.tierDownButton = new Button() {

				@Override
				public void onClickedInMenu(Player pl, Menu menu, ClickType click) {
					if (tier > 1)
						new ClassPreviewMenu(tier - 1).displayTo(pl);
				}

				@Override
				public ItemStack getItem() {
					return ItemCreator.from(
							tier == 1 ? CompMaterial.GRAY_DYE : CompMaterial.PINK_DYE,
							"&f" + (tier == 1 ? "This tier is the starting one" : "Edit lower tier")).makeMenuTool();
				}
			};

			this.tierUpButton = new Button() {

				@Override
				public void onClickedInMenu(Player pl, Menu menu, ClickType click) {
					if (tier <= TIER_LIMIT && ClassPreviewMenu.this.getTierIfExist() != null)
						new ClassPreviewMenu(tier + 1).displayTo(pl);
				}

				@Override
				public ItemStack getItem() {
					return (ClassPreviewMenu.this.getTierIfExist() == null
							? ItemCreator.from(CompMaterial.GRAY_DYE, "&7Create this tier first!")
							: ItemCreator.fromMaterial(tier <= TIER_LIMIT ? CompMaterial.LIME_DYE : CompMaterial.GRAY_DYE).name("&f" + (tier <= TIER_LIMIT ? "Edit higher tier" : "This is the maximum tier"))).makeMenuTool();
				}
			};
		}

		@Override
		public void onMenuClose(Player pl, Inventory inv) {
			// Valid.checkBoolean(inv.getSize() == getSize(), "Report / Illegal size " + inv.getSize() + " != " + getSize());

			final int normalInvSize = PlayerUtil.PLAYER_INV_SIZE;

			final ItemStack[] content = Arrays.copyOfRange(inv.getContents(), 0, normalInvSize);
			final ItemStack[] armorContent = Arrays.copyOfRange(inv.getContents(), ARMOR_SLOT_BEGINNING_INDEX, ARMOR_SLOT_BEGINNING_INDEX + 4);

			final ClassTier existing = this.getTierIfExist();

			// Completely empty
			if (Valid.isNullOrEmpty(content) && Valid.isNullOrEmpty(armorContent)) {

				// Not yet saved
				if (existing == null)
					return;

				// Remove empty tier
				IndividualClassMenu.this.clazz.removeTier(existing);
				return;
			}

			final SimpleArmorContent armor = SimpleArmorContent.fromItemStacks(armorContent);

			final SimpleTier tierClass = new SimpleTier(IndividualClassMenu.this.clazz.getName(), this.tier, existing != null ? existing.getLevelCost() : this.getDefaultCost(), content, armor);
			IndividualClassMenu.this.clazz.addOrUpdateTier(tierClass);

			CompSound.BLOCK_CHEST_CLOSE.play(pl, 1, 1);
			Common.tell(pl, "&2Your changes have been saved.");
		}

		@Override
		public ItemStack getItemAt(int slot) {

			{ // Tier Content
				final ClassTier tier = this.getTierIfExist();

				if (tier != null) {
					if (slot < 36 && tier.getContent() != null)
						return tier.getContent()[slot];

					if (slot >= ARMOR_SLOT_BEGINNING_INDEX && slot <= ARMOR_SLOT_BEGINNING_INDEX + 3 && tier.getArmor() != null)
						return tier.getArmor().getByOrder(slot % ARMOR_SLOT_BEGINNING_INDEX);
				}
			}

			{ // Armor Info
				if (slot == 9 * 4)
					return this.makeArmorSlot(1, "helmet");

				if (slot == 9 * 4 + 1)
					return this.makeArmorSlot(2, "chestplate");

				if (slot == 9 * 4 + 2)
					return this.makeArmorSlot(3, "leggings");

				if (slot == 9 * 4 + 3)
					return this.makeArmorSlot(4, "boots");
			}

			{ // Functional and helper buttons
				if (slot == 9 * 5 + 4)
					return this.priceButton.getItem();

				if (slot == 9 * 5 + 6)
					return this.tierDownButton.getItem();

				if (slot == 9 * 5 + 7)
					return this.tierUpButton.getItem();

				if (slot >= 9 * 4 && slot < 9 * 5 || slot >= 49)
					return ItemCreator.fromMaterial(CompMaterial.GRAY_STAINED_GLASS_PANE).name(" ").make();
			}

			return null;
		}

		private ItemStack makeArmorSlot(int amount, String name) {
			return ItemCreator.fromMaterial(CompMaterial.GRAY_STAINED_GLASS_PANE).amount(amount).name("&7Place &f" + name + " &7below:").makeMenuTool();
		}

		@Override
		protected int getInfoButtonPosition() {
			return 9 * 5 + 5;
		}

		@Override
		public boolean isActionAllowed(MenuClickLocation clickLocation, int slot, ItemStack clickedSlot, ItemStack cursor, InventoryAction action) {
			final boolean armorSlot = slot >= ARMOR_SLOT_BEGINNING_INDEX && slot <= ARMOR_SLOT_BEGINNING_INDEX + 3;
			final String type = slot == ARMOR_SLOT_BEGINNING_INDEX ? "helmet" : slot == ARMOR_SLOT_BEGINNING_INDEX + 1 ? "chestplate" : slot == ARMOR_SLOT_BEGINNING_INDEX + 2 ? "leggings" : slot == ARMOR_SLOT_BEGINNING_INDEX + 3 ? "boots" : "error";

			return armorSlot ? this.checkValidArmor(clickedSlot, cursor, type) : clickLocation != MenuClickLocation.OUTSIDE && slot < 9 * 4;
		}

		private ClassTier getTierIfExist() {
			return IndividualClassMenu.this.clazz.getTier(this.tier);
		}

		private int getDefaultCost() {
			return (int) MathUtil.calculate(Constants.Costs.TIER_STARTING_COST.replace("{currentTier}", this.tier + ""));
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
					this.animateTitle("&4Only insert " + type.toLowerCase() + (type.toLowerCase().endsWith("s") ? "" : "s") + " here!");

					if (clickedSlot != null)
						clickedSlot.setAmount(0);

					return false;
				}

			return true;
		}

		@Override
		public Menu newInstance() {
			return new ClassPreviewMenu(this.tier);
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
