package org.mineacademy.corearena.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.conversation.RewardCostConvo;
import org.mineacademy.corearena.data.AllData.ArenaPlayer;
import org.mineacademy.corearena.impl.SimpleReward;
import org.mineacademy.corearena.manager.RewardsManager;
import org.mineacademy.corearena.model.ArenaClass;
import org.mineacademy.corearena.model.ClassTier;
import org.mineacademy.corearena.model.Reward;
import org.mineacademy.corearena.settings.Settings;
import org.mineacademy.corearena.type.RewardType;
import org.mineacademy.corearena.util.Constants;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.ButtonMenu;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.model.MenuClickLocation;
import org.mineacademy.fo.model.Variables;
import org.mineacademy.fo.remain.CompEnchantment;
import org.mineacademy.fo.remain.CompItemFlag;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.fo.settings.Lang;

public class MenuRewards extends Menu {

	private final MenuMode mode;

	public enum MenuMode {
		PURCHASE,
		EDIT_ITEMS,
		EDIT_PRICES
	}

	private final ArenaPlayer data;

	private int nuggets;

	private final Button itemsButton;
	private final Button blocksButton;
	private final Button packsButton;
	/* hidden on edit */
	private final Button tiersButton;

	public static void showRewardsMenu(Player player, MenuMode mode) {
		if (Settings.Rewards.ENABLE_MATERIAL_REWARDS)
			new MenuRewards(player, mode).displayTo(player);
		else if (mode == MenuMode.PURCHASE)
			new MenuRewards(player, mode).showTierMenu(player);
		else
			Common.tell(player, Lang.component("menu-use-class-menu"));
	}

	private MenuRewards(Player player, MenuMode mode) {
		super(mode == MenuMode.PURCHASE ? null : new CoreMenu());

		this.setSize(9 * 5);
		this.setTitle(Lang.legacy("menu-rewards-label"));
		this.setViewer(player);

		this.mode = mode;
		this.data = CoreArenaPlugin.getDataFor(this.getViewer());

		this.nuggets = this.data.getNuggets();

		this.itemsButton = new ButtonMenu(new ItemRewardMenu(mode, RewardType.ITEM), ItemCreator
				.fromMaterial(Settings.Rewards.ITEMS)
				.name("&a&l" + this.getBuyOrEdit(Lang.legacy("menu-rewards-items")))
				.lore(Lang.legacy("menu-rewards-items-description")));

		this.blocksButton = new ButtonMenu(new ItemRewardMenu(mode, RewardType.BLOCK), ItemCreator
				.fromMaterial(Settings.Rewards.BLOCKS)
				.name("&6&l" + this.getBuyOrEdit(Lang.legacy("menu-rewards-blocks")))
				.lore(Lang.legacy("menu-rewards-blocks-description")));

		this.packsButton = new ButtonMenu(new ItemRewardMenu(mode, RewardType.PACK), ItemCreator
				.fromMaterial(Settings.Rewards.PACKS)
				.name("&f&l" + this.getBuyOrEdit(Lang.legacy("menu-rewards-packs")))
				.lore(Lang.legacy("menu-rewards-packs-description")));

		this.tiersButton = mode == MenuMode.PURCHASE ? new Button() {

			@Override
			public ItemStack getItem() {
				return ItemCreator
						.fromMaterial(CompMaterial.DIAMOND_SWORD)
						.enchant(CompEnchantment.DURABILITY)
						.name(Lang.legacy("menu-rewards-class"))
						.lore(Lang.legacy("menu-rewards-class-description"))
						.flags(CompItemFlag.HIDE_ENCHANTS)
						.makeMenuTool();
			}

			@Override
			public final void onClickedInMenu(Player pl, Menu menu, ClickType click) {
				MenuRewards.this.showTierMenu(pl);
			}
		} : Button.makeEmpty();
	}

	private void showTierMenu(Player pl) {
		final MenuClasses classMenu = new MenuClasses(MenuRewards.this) {

			@Override
			protected String getCustomTitle() {
				return Lang.legacy("menu-rewards-select-class");
			}

			@Override
			protected Button getAddButton() {
				return null;
			}

			@Override
			protected Button getListButton(String listName, int listIndex) {
				return CoreArenaPlugin.getClassManager().findClass(listName).isValid()
						? super.getListButton(listName, listIndex)
						: Button.makeDummy(ItemCreator.from(CompMaterial.WHITE_STAINED_GLASS_PANE, "&e" + listName, "", Lang.legacy("menu-rewards-class-not-configured")));
			}
		};

		classMenu.setMenuMaker(clazz -> new UpgradePreviewMenu(clazz, PreviewMode.ARMOR, classMenu));
		classMenu.displayTo(pl);
	}

	@Override
	protected final String[] getInfo() {
		final String[] locale = (this.mode == MenuMode.PURCHASE ? Lang.legacy("menu-rewards-info-player") : Lang.legacy("menu-rewards-info-admin")).split("\n");

		return Variables.builder().placeholder("nuggets", Lang.numberFormat("currency-name", this.data.getNuggets())).replaceLegacyArray(locale);
	}

	private String getBuyOrEdit(String label) {
		return (this.mode == MenuMode.PURCHASE ? Lang.legacy("part-buy") : Lang.legacy("part-edit")) + " " + label;
	}

	@Override
	public Menu newInstance() {
		return new MenuRewards(this.getViewer(), this.mode);
	}

	private enum PreviewMode {
		ARMOR,
		CONTENT;

		private PreviewMode shift() {
			return this == ARMOR ? CONTENT : ARMOR;
		}
	}

	//
	// This menu handles da updates de la items, blocks or packs
	//
	final class ItemRewardMenu extends Menu {

		private final MenuMode mode;

		private final RewardType rewardType;

		/* hidden on purchases */
		private final Button modesButton;

		private ItemRewardMenu(MenuMode mode, RewardType type) {
			super(MenuRewards.this, true);

			this.setSize(9 * 4);
			this.setTitle(Lang.legacy("menu-rewards-choose-a-reward"));

			this.mode = mode;
			this.rewardType = type;

			this.modesButton = mode == MenuMode.PURCHASE ? Button.makeEmpty() : new Button() {

				@Override
				public void onClickedInMenu(Player pl, Menu menu, ClickType click) {
					ItemRewardMenu.this.saveChangedItems(pl);

					new ItemRewardMenu(mode == MenuMode.EDIT_ITEMS ? MenuMode.EDIT_PRICES : MenuMode.EDIT_ITEMS, ItemRewardMenu.this.rewardType).displayTo(pl);
				}

				final String EDITED = Lang.legacy("edit-menu-start-editing", "mode", mode.toString().replace("EDIT_", "").toLowerCase());

				@Override
				public ItemStack getItem() {
					return ItemCreator
							.fromMaterial(mode == MenuMode.EDIT_ITEMS ? CompMaterial.STICK : CompMaterial.GOLD_NUGGET)
							.name(this.EDITED)
							.enchant(CompEnchantment.ARROW_DAMAGE)
							.hideTags(true)
							.lore(Arrays.asList(
									"",
									"&7" + (mode == MenuMode.EDIT_ITEMS ? Lang.legacy("edit-menu-editing-pieces") : Lang.legacy("edit-menu-editing-items"))))
							.makeMenuTool();
				}
			};
		}

		@Override
		public void onMenuClose(Player pl, Inventory inv) {
			this.saveChangedItems(pl);
		}

		private void saveChangedItems(Player player) {
			if (this.mode != MenuMode.EDIT_ITEMS)
				return;

			final Inventory top = Remain.getTopInventoryFromOpenInventory(player);

			if (top.getSize() != this.getSize()) {
				Common.tell(player, "&cYour changes could not be saved, your top inventory was changed to " + top.getType() + ", size " + this.getSize() + " expected.");

				return;
			}

			Valid.checkBoolean(top.getSize() == this.getSize(), "Report / Illegal size " + top.getSize() + " != " + this.getSize());

			final RewardsManager manager = CoreArenaPlugin.getRewadsManager();
			final List<Reward> rewards = new ArrayList<>();

			for (int i = 0; i < top.getSize() - 9; i++) {
				final ItemStack is = top.getItem(i);

				if (is == null || is.getType() == Material.AIR) {
					rewards.add(null);
					continue;
				}

				// Reason for the hard way: preserve the cost
				final Reward oldReward = Common.getOrDefault(this.getRewardAt(i), SimpleReward.fromItem(this.rewardType, is, Constants.Costs.REWARD_STARTING_COST));
				oldReward.setItem(is);

				rewards.add(oldReward);
			}

			manager.setRewards(this.rewardType, rewards);

			CompSound.BLOCK_CHEST_CLOSE.play(player, 1, 1);
			Common.tell(player, Lang.component("edit-saved-changes"));
		}

		@Override
		public ItemStack getItemAt(int slot) {
			final Reward reward = this.getRewardAt(slot);

			if (reward != null && reward.getItem() != null) {
				final List<String> lore = new ArrayList<>();

				lore.add("");
				lore.add("&d" + Lang.legacy("part-cost") + ": &f" + Lang.numberFormat("currency-name", reward.getCost()));

				return this.mode == MenuMode.PURCHASE || this.mode == MenuMode.EDIT_PRICES ? ItemCreator
						.fromItemStack(reward.getItem().clone())
						.lore(lore)
						.make() : reward.getItem();
			}

			if (slot == this.getSize() - 5)
				return this.modesButton.getItem();

			if (slot > 9 * 3 - 1)
				return ItemCreator.fromMaterial(CompMaterial.fromLegacy("STAINED_GLASS_PANE", 8)).name(" ").make();

			return null;
		}

		private Reward getRewardAt(int slot) {
			final List<Reward> loaded = CoreArenaPlugin.getRewadsManager().getRewards(this.rewardType);

			return loaded.size() > slot ? loaded.get(slot) : null;
		}

		@Override
		public boolean isActionAllowed(MenuClickLocation clickLocation, int slot, ItemStack clickedSlot, ItemStack cursor, InventoryAction action) {
			return this.mode == MenuMode.EDIT_ITEMS && (clickLocation != MenuClickLocation.MENU || slot < 9 * 3);
		}

		@Override
		public void onMenuClick(Player player, int slot, InventoryAction action, ClickType click, ItemStack cursor, ItemStack item, boolean cancelled) {
			if (slot > 9 * 3)
				return;

			final Reward reward = this.getRewardAt(slot);

			if (reward == null)
				return;

			if (this.mode == MenuMode.EDIT_PRICES)
				new RewardCostConvo(ItemRewardMenu.this, reward).start(player);

			else if (this.mode == MenuMode.PURCHASE) {

				{ // Check if player has enough of dem nuggets
					if (MenuRewards.this.nuggets < reward.getCost()) {
						CompSound.BLOCK_NOTE_BLOCK_BASS.play(player, 1F, (float) Math.random());
						this.animateTitle(Lang.legacy("menu-rewards-more-nuggets-needed", "reward_cost", Lang.numberFormat("currency-name", reward.getCost())));

						return;
					}
				}

				{ // Take nuggets and save
					MenuRewards.this.nuggets = MenuRewards.this.nuggets - reward.getCost();

					MenuRewards.this.data.setNuggets(MenuRewards.this.nuggets);
				}

				{ // Update the balance
					this.restartMenu();
				}

				boolean droppedOnFloor = false;

				{ // Give the purchased item
					final Map<Integer, ItemStack> overflow = PlayerUtil.addItems(Remain.getBottomInventoryFromOpenInventory(player), reward.getItem());

					if (!overflow.isEmpty()) {
						for (final ItemStack overflownItem : overflow.values()) {
							final Item droppedItem = player.getLocation().getWorld().dropItem(player.getLocation(), overflownItem);

							droppedItem.setPickupDelay(20);
						}

						droppedOnFloor = true;
					}
				}

				{ // Notify
					CompSound.ENTITY_PLAYER_LEVELUP.play(player, 1, 1);

					this.animateTitle(Lang.legacy(droppedOnFloor ? "menu-rewards-bought-overflow" : "menu-rewards-bought", "remaining", Lang.numberFormat("currency-name", MenuRewards.this.nuggets)));
				}
			}
		}

		@Override
		protected String[] getInfo() {
			switch (this.mode) {
				case EDIT_PRICES:
					return Lang.legacy("menu-rewards-info-edit-pieces").split("\n");
				case EDIT_ITEMS:
					return Lang.legacy("menu-rewards-info-edit-items").split("\n");
				case PURCHASE:
					return Lang.legacy("menu-rewards-info-purchase", "balance", MenuRewards.this.nuggets).split("\n");
			}

			// Will never be reached
			return null;
		}

		@Override
		public Menu newInstance() {
			return new ItemRewardMenu(this.mode, this.rewardType);
		}
	}

	//
	// This focking menu shows the player what to expect (nothing)
	//
	final class UpgradePreviewMenu extends Menu {

		private final ArenaClass clazz;
		private final PreviewMode previewMode;

		private final Button switchModesButton;
		private final Button buyButton;
		private final Button nextTierButton;

		private final ClassTier tier;

		private UpgradePreviewMenu(ArenaClass clazz, PreviewMode previewMode, Menu parent) {
			this(1, clazz, previewMode, parent);
		}

		private UpgradePreviewMenu(int tierCount, ArenaClass clazz, PreviewMode previewMode, Menu parent) {
			super(parent);

			this.setSize(9 * 6);

			this.clazz = clazz;
			Valid.checkNotNull(clazz, "Jackie Wilson - Reet Petite");

			this.tier = clazz.getTier(tierCount);
			Valid.checkNotNull(this.tier, clazz.getName() + " Tier " + tierCount + " does not exists!");

			this.setTitle("&0&l" + clazz.getName() + " " + Lang.legacy("menu-rewards-tier-label") + " " + this.tier.getTier());

			this.previewMode = previewMode;

			this.switchModesButton = new Button() {

				@Override
				public void onClickedInMenu(Player pl, Menu menu, ClickType click) {
					new UpgradePreviewMenu(tierCount, clazz, previewMode.shift(), parent).displayTo(pl);
				}

				@Override
				public ItemStack getItem() {
					return ItemCreator.fromMaterial(CompMaterial.ENDER_EYE).name(previewMode == PreviewMode.ARMOR ? Lang.legacy("menu-rewards-tier-show-inventory") : Lang.legacy("menu-rewards-tier-show-armor")).makeMenuTool();
				}
			};

			this.buyButton = new Button() {

				@Override
				public void onClickedInMenu(Player pl, Menu menu, ClickType click) {
					if (UpgradePreviewMenu.this.hasTier()) {
						UpgradePreviewMenu.this.animateTitle(Lang.legacy("menu-rewards-tier-already-bought"));
						return;
					}

					if (!Settings.Rewards.ALLOW_TIER_SKIP)
						if (UpgradePreviewMenu.this.tier.getTier() - MenuRewards.this.data.getTierOf(clazz) != 1) {
							UpgradePreviewMenu.this.animateTitle(Lang.legacy("menu-rewards-tier-too-high"));
							return;
						}

					final ClassTier tier = clazz.getTier(tierCount);

					{ // Check if player has enough nuggets
						if (MenuRewards.this.nuggets < tier.getLevelCost()) {
							CompSound.BLOCK_NOTE_BLOCK_BASS.play(pl, 1F, (float) Math.random());

							UpgradePreviewMenu.this.animateTitle(Lang.legacy("menu-rewards-more-nuggets-needed", "reward_cost", Lang.numberFormat("currency-name", tier.getLevelCost())));
							return;
						}
					}

					{ // Take nuggets and save
						MenuRewards.this.nuggets = MenuRewards.this.nuggets - tier.getLevelCost();

						MenuRewards.this.data.setNuggets(MenuRewards.this.nuggets);
					}

					{ // Give the purchased item
						MenuRewards.this.data.setHigherTier(clazz);
					}

					{ // Update buy inventory
						UpgradePreviewMenu.this.restartMenu();
					}

					{ // Notify
						CompSound.ENTITY_PLAYER_LEVELUP.play(pl, 1, 1);

						final String purchasedMessage = Lang.legacy("menu-rewards-tier-purchased-message", "name", clazz.getName(), "tier", String.valueOf(tierCount));
						Common.tell(pl, purchasedMessage);

						final String purchasesTitle = Lang.legacy("menu-rewards-tier-purchased-title", "tier", String.valueOf(tierCount));
						UpgradePreviewMenu.this.animateTitle(purchasesTitle);
					}
				}

				@Override
				public ItemStack getItem() {
					ItemStack itemStack = ItemCreator
							.fromMaterial(CompMaterial.DIAMOND)
							.name("&d&l" + Lang.legacy("part-purchase") + " " + Lang.legacy("menu-rewards-tier-label"))
							.lore(Arrays.asList(" ", "&7" + Lang.legacy("part-price") + ": &6" + Lang.numberFormat("currency-name", UpgradePreviewMenu.this.tier.getLevelCost())))

							.makeMenuTool();

					if (UpgradePreviewMenu.this.hasTier())
						itemStack = ItemCreator
								.fromItemStack(itemStack)
								.name(Lang.legacy("menu-rewards-tier-already-bought"))
								.hideTags(true)
								.enchant(CompEnchantment.ARROW_INFINITE)
								.makeMenuTool();

					return itemStack;
				}
			};

			this.nextTierButton = new Button() {

				@Override
				public void onClickedInMenu(Player pl, Menu menu, ClickType click) {
					if (!UpgradePreviewMenu.this.isMaximumTier())
						new UpgradePreviewMenu(tierCount + 1, clazz, PreviewMode.ARMOR, parent).displayTo(pl);
				}

				@Override
				public ItemStack getItem() {

					return ItemCreator
							.fromMaterial(CompMaterial.fromLegacy("INK_SACK", UpgradePreviewMenu.this.isMaximumTier() ? UpgradePreviewMenu.this.hasTier() ? 10 : 9 : 13))
							.name(UpgradePreviewMenu.this.isMaximumTier() ? UpgradePreviewMenu.this.hasTier() ? Lang.legacy("menu-rewards-tier-maximum") : Lang.legacy("menu-rewards-tier-top") : Lang.legacy("menu-rewards-tier-next"))
							.makeMenuTool();
				}
			};
		}

		private boolean hasTier() {
			return MenuRewards.this.data.getTierOf(this.clazz) >= this.tier.getTier();
		}

		private boolean isMaximumTier() {
			return this.clazz.getTier(this.tier.getTier() + 1) == null;
		}

		@Override
		public ItemStack getItemAt(int slot) {
			final boolean hasTier = MenuRewards.this.data.getTierOf(this.clazz) >= this.tier.getTier();

			if (this.previewMode == PreviewMode.ARMOR) {
				final LeatherArmorMeta metaData = (LeatherArmorMeta) new ItemStack(Material.LEATHER_HELMET).getItemMeta();
				metaData.setColor(Color.GRAY);

				if (slot == 9 + 4)
					return Common.getOrDefault(this.tier.getArmor().getHelmet(), ItemCreator
							.fromMaterial(CompMaterial.LEATHER_HELMET)
							.name(Lang.legacy("menu-rewards-not-set-helmet"))
							.meta(metaData)

							.makeMenuTool());

				if (slot == 9 * 2 + 4)
					return Common.getOrDefault(this.tier.getArmor().getChestplate(), ItemCreator
							.fromMaterial(CompMaterial.LEATHER_CHESTPLATE)
							.name(Lang.legacy("menu-rewards-not-set-chestplate"))
							.meta(metaData)

							.makeMenuTool());

				if (slot == 9 * 3 + 4)
					return Common.getOrDefault(this.tier.getArmor().getLeggings(), ItemCreator
							.fromMaterial(CompMaterial.LEATHER_LEGGINGS)
							.name(Lang.legacy("menu-rewards-not-set-leggings"))
							.meta(metaData)

							.makeMenuTool());

				if (slot == 9 * 4 + 4)
					return Common.getOrDefault(this.tier.getArmor().getBoots(), ItemCreator
							.fromMaterial(CompMaterial.LEATHER_BOOTS)
							.name(Lang.legacy("menu-rewards-not-set-boots"))
							.meta(metaData)

							.makeMenuTool());

			} else if (this.previewMode == PreviewMode.CONTENT) {
				if (slot < 36 && this.tier.getContent() != null)
					return this.tier.getContent()[slot];

			} else
				throw new FoException("Unhandled vzhlad pre povysenie triedy - " + this.previewMode);

			if (slot == this.getSize() - 5)
				return this.switchModesButton.getItem();

			if (slot == this.getSize() - 4)
				return this.buyButton.getItem();

			if (slot == this.getSize() - 1)
				return this.nextTierButton.getItem();

			if (slot > 9 * 5 - 1)
				return ItemCreator
						.fromMaterial(CompMaterial
								.fromLegacy("STAINED_GLASS_PANE", (hasTier ? DyeColor.LIME : DyeColor.RED)
										.getWoolData()))
						.name(hasTier ? Lang.legacy("menu-rewards-tier-added") : Lang.legacy("menu-rewards-tier-is-locked"))

						.make();

			return null;
		}

		@Override
		public boolean isActionAllowed(MenuClickLocation clickLocation, int slot, ItemStack clickedSlot, ItemStack cursor, InventoryAction action) {
			return MenuRewards.this.mode == MenuMode.PURCHASE ? super.isActionAllowed(clickLocation, slot, clickedSlot, cursor, action) : clickLocation != MenuClickLocation.MENU || slot < 9 * 3;
		}

		@Override
		protected int getReturnButtonPosition() {
			return this.getSize() - 9;
		}

		@Override
		protected int getInfoButtonPosition() {
			return this.getSize() - 6;
		}

		@Override
		protected String[] getInfo() {
			final List<String> lore = new ArrayList<>();

			for (final String line : Lang.legacy("menu-rewards-tier-lore-balance", "balance", Lang.numberFormat("currency-name", MenuRewards.this.nuggets)).split("\n"))
				lore.add(line);

			if (!this.hasTier())
				for (final String line : Lang.legacy("menu-rewards-tier-lore-upgrade").split("\n"))
					lore.add(line);

			return lore.toArray(new String[0]);
		}
	}

	@Override
	public ItemStack getItemAt(int slot) {

		if (Settings.Rewards.ENABLE_MATERIAL_REWARDS) {
			if (slot == 9 * 1 + 4)
				return this.blocksButton.getItem();

			if (slot == 9 * 2 + 2)
				return this.itemsButton.getItem();

			if (slot == 9 * 2 + 6)
				return this.packsButton.getItem();

			if (slot == 9 * 3 + 4)
				return this.tiersButton.getItem();

		} else if (slot == 9 * 2 + 4)
			return this.tiersButton.getItem();

		return null;
	}
}