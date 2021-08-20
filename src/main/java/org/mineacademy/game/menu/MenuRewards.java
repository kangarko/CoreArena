package org.mineacademy.game.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.ItemUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.ButtonMenu;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.model.MenuClickLocation;
import org.mineacademy.fo.model.Replacer;
import org.mineacademy.fo.model.SimpleEnchant;
import org.mineacademy.fo.remain.CompItemFlag;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.game.conversation.RewardCostConvo;
import org.mineacademy.game.impl.ArenaPlayer;
import org.mineacademy.game.impl.SimpleReward;
import org.mineacademy.game.manager.RewardsManager;
import org.mineacademy.game.model.ArenaClass;
import org.mineacademy.game.model.ClassTier;
import org.mineacademy.game.model.Reward;
import org.mineacademy.game.settings.Localization;
import org.mineacademy.game.settings.Settings;
import org.mineacademy.game.type.RewardType;
import org.mineacademy.game.util.Constants;

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
			Common.tell(player, Localization.Menu.USE_CLASS_MENU);
	}

	private MenuRewards(Player player, MenuMode mode) {
		super(mode == MenuMode.PURCHASE ? null : new CoreMenu());

		setSize(9 * 5);
		setTitle(Localization.Menu.Rewards.LABEL);
		setViewer(player);

		this.mode = mode;
		data = CoreArenaPlugin.getDataFor(getViewer());

		nuggets = data.getNuggets();

		itemsButton = new ButtonMenu(new ItemRewardMenu(mode, RewardType.ITEM), ItemCreator
				.of(Settings.Rewards.ITEMS)
				.name("&a&l" + getBuyOrEdit(Localization.Menu.Rewards.ITEMS))
				.lores(Localization.Menu.Rewards.ITEMS_DESCRIPTION));

		blocksButton = new ButtonMenu(new ItemRewardMenu(mode, RewardType.BLOCK), ItemCreator
				.of(Settings.Rewards.BLOCKS)
				.name("&6&l" + getBuyOrEdit(Localization.Menu.Rewards.BLOCKS))
				.lores(Localization.Menu.Rewards.BLOCKS_DESCRIPTION));

		packsButton = new ButtonMenu(new ItemRewardMenu(mode, RewardType.PACK), ItemCreator
				.of(Settings.Rewards.PACKS)
				.name("&f&l" + getBuyOrEdit(Localization.Menu.Rewards.PACKS))
				.lores(Localization.Menu.Rewards.PACKS_DESCRIPTION));

		tiersButton = mode == MenuMode.PURCHASE ? new Button() {

			@Override
			public ItemStack getItem() {
				return ItemCreator
						.of(CompMaterial.DIAMOND_SWORD)
						.enchant(new SimpleEnchant(Enchantment.DURABILITY))
						.name("&b&l" + Localization.Menu.Rewards.CLASS)
						.lores(Localization.Menu.Rewards.CLASS_DESCRIPTION)
						.flag(CompItemFlag.HIDE_ENCHANTS)
						.build().makeMenuTool();
			}

			@Override
			public final void onClickedInMenu(Player pl, Menu menu, ClickType click) {
				showTierMenu(pl);
			}
		} : Button.makeEmpty();
	}

	private void showTierMenu(Player pl) {
		final MenuClasses classMenu = new MenuClasses(MenuRewards.this) {
			@Override
			protected String getCustomTitle() {
				return Localization.Menu.Rewards.SELECT_CLASS;
			}

			@Override
			protected Button getAddButton() {
				return null;
			}

			@Override
			protected Button getListButton(String listName, int listIndex) {
				return CoreArenaPlugin.getClassManager().findClass(listName).isValid()
						? super.getListButton(listName, listIndex)
						: Button.makeDummy(ItemCreator.of(CompMaterial.WHITE_STAINED_GLASS_PANE, "&e" + listName, "", Localization.Menu.Rewards.CLASS_NOT_CONFIGURED));
			}
		};

		classMenu.setMenuMaker(clazz -> new UpgradePreviewMenu(clazz, PreviewMode.ARMOR, classMenu));
		classMenu.displayTo(pl);
	}

	@Override
	protected final String[] getInfo() {
		List<String> locale = mode == MenuMode.PURCHASE ? Localization.Menu.Rewards.INFO_PLAYER : Localization.Menu.Rewards.INFO_ADMIN;
		locale = Replacer.replaceArray(locale, "nuggets", Localization.Currency.format(data.getNuggets()));

		return locale.toArray(new String[0]);
	}

	private String getBuyOrEdit(String label) {
		return (mode == MenuMode.PURCHASE ? Localization.Parts.BUY : Localization.Parts.EDIT) + " " + label;
	}

	@Override
	public Menu newInstance() {
		return new MenuRewards(getViewer(), mode);
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

			setSize(9 * 4);
			setTitle("&0" + (mode == MenuMode.PURCHASE
					? Localization.Menu.Rewards.CHOOSE_A_REWARD
					: "Editing " + (mode == MenuMode.EDIT_PRICES ? "Prices of " : "") + type));

			this.mode = mode;
			rewardType = type;

			modesButton = mode == MenuMode.PURCHASE ? Button.makeEmpty() : new Button() {

				@Override
				public void onClickedInMenu(Player pl, Menu menu, ClickType click) {
					saveChangedItems(pl);

					new ItemRewardMenu(mode == MenuMode.EDIT_ITEMS ? MenuMode.EDIT_PRICES : MenuMode.EDIT_ITEMS, rewardType).displayTo(pl);
				}

				final String EDITED = Localization.Edit.Menu.START_EDITING.replaceAll("mode", mode.toString().replace("EDIT_", "").toLowerCase());

				@Override
				public ItemStack getItem() {
					return ItemCreator
							.of(mode == MenuMode.EDIT_ITEMS ? CompMaterial.STICK : CompMaterial.GOLD_NUGGET)
							.name(EDITED)
							.enchant(new SimpleEnchant(Enchantment.ARROW_DAMAGE))
							.hideTags(true)
							.lores(Arrays.asList(
									"",
									"&7" + (mode == MenuMode.EDIT_ITEMS ? Localization.Edit.Menu.EDITING_PIECES : Localization.Edit.Menu.EDITING_ITEMS)))
							.build().makeMenuTool();
				}
			};
		}

		@Override
		public void onMenuClose(Player pl, Inventory inv) {
			saveChangedItems(pl);
		}

		private void saveChangedItems(Player pl) {
			if (mode != MenuMode.EDIT_ITEMS)
				return;

			final Inventory top = pl.getOpenInventory().getTopInventory();

			if (top.getSize() != getSize()) {
				//
				Common.tell(pl, "&cYour changes could not be saved, your top inventory was changed to " + top.getType() + " (" + pl.getOpenInventory().getTitle() + ", " + top.getSize() + "), size " + getSize() + " expected.");

				return;
			}

			Valid.checkBoolean(top.getSize() == getSize(), "Report / Illegal size " + top.getSize() + " != " + getSize());

			final RewardsManager manager = CoreArenaPlugin.getRewadsManager();
			final List<Reward> rewards = new ArrayList<>();

			for (int i = 0; i < top.getSize() - 9; i++) {
				final ItemStack is = top.getItem(i);

				if (is == null || is.getType() == Material.AIR) {
					rewards.add(null);
					continue;
				}

				// Reason for the hard way: preserve the cost
				final Reward oldReward = Common.getOrDefault(getRewardAt(i), SimpleReward.fromItem(rewardType, is, Constants.Costs.REWARD_STARTING_COST));
				oldReward.setItem(is);

				rewards.add(oldReward);
			}

			manager.setRewards(rewardType, rewards);

			CompSound.CHEST_CLOSE.play(pl, 1, 1);
			Common.tell(pl, Localization.Edit.SAVED_CHANGES);
		}

		@Override
		public ItemStack getItemAt(int slot) {
			final Reward reward = getRewardAt(slot);

			if (reward != null && reward.getItem() != null) {
				final List<String> lore = new ArrayList<>();

				//if (reward.getItem().hasItemMeta() && reward.getItem().getItemMeta().hasLore())
				//	lore.addAll(reward.getItem().getItemMeta().getLore());

				lore.add("");
				lore.add("&d" + Localization.Parts.COST + ": &f" + Localization.Currency.format(reward.getCost()));

				return mode == MenuMode.PURCHASE || mode == MenuMode.EDIT_PRICES ? ItemCreator
						.of(reward.getItem().clone())
						.lores(lore)
						.build().make() : reward.getItem();
			}

			if (slot == getSize() - 5)
				return modesButton.getItem();

			if (slot > 9 * 3 - 1)
				return ItemCreator.of(CompMaterial.fromLegacy("STAINED_GLASS_PANE", 8)).name(" ").build().make();

			return null;
		}

		private Reward getRewardAt(int slot) {
			final List<Reward> loaded = CoreArenaPlugin.getRewadsManager().getRewards(rewardType);

			return loaded.size() > slot ? loaded.get(slot) : null;
		}

		@Override
		public boolean isActionAllowed(MenuClickLocation clickLocation, int slot, ItemStack clickedSlot, ItemStack cursor) {
			return mode == MenuMode.EDIT_ITEMS && (clickLocation != MenuClickLocation.MENU || slot < 9 * 3);
		}

		@Override
		public void onMenuClick(Player pl, int slot, InventoryAction action, ClickType click, ItemStack cursor, ItemStack item, boolean cancelled) {
			if (slot > 9 * 3)
				return;

			final Reward reward = getRewardAt(slot);

			if (reward == null)
				return;

			if (mode == MenuMode.EDIT_PRICES)
				new RewardCostConvo(ItemRewardMenu.this, reward).start(pl);
			else if (mode == MenuMode.PURCHASE) {
				{ // Check if player has enough nuggets
					if (nuggets < reward.getCost()) {
						CompSound.NOTE_BASS.play(pl, 1F, (float) Math.random());
						final String replaced = Localization.Menu.Rewards.MORE_NUGGETS_NEEDED
								.replaceAll("{reward_cost}", reward.getCost());
						animateTitle(replaced);

						return;
					}
				}

				{ // Take nuggets and save
					nuggets = nuggets - reward.getCost();

					data.setNuggets(nuggets);
				}

				{ // Update the balance
					restartMenu();
				}

				{ // Give the purchased item
					pl.getOpenInventory().getBottomInventory().addItem(reward.getItem());
				}

				{ // Notify
					CompSound.LEVEL_UP.play(pl, 1, 1);

					final String bought_Replaced = Localization.Menu.Rewards.BOUGHT
							.replaceAll("{remaining}", Localization.Currency.format(nuggets));
					Common.tell(pl, "&6You have purchased " + Common.article(ItemUtil.bountifyCapitalized(reward.getItem().getType())));
					animateTitle(bought_Replaced);
				}
			}
		}

		@Override
		protected String[] getInfo() {
			switch (mode) {
				case EDIT_PRICES:
					return Localization.Menu.Rewards.INFO_EDIT_PIECES;
				case EDIT_ITEMS:
					return Localization.Menu.Rewards.INFO_EDIT_ITEMS;
				case PURCHASE:
					return Localization.Menu.Rewards.INFO_PURCHASE
							.find("balance")
							.replace(nuggets)
							.getReplacedMessage();
			}

			// Will never be reached
			return null;
		}

		@Override
		public Menu newInstance() {
			return new ItemRewardMenu(mode, rewardType);
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

			setSize(9 * 6);

			this.clazz = clazz;
			Valid.checkNotNull(clazz, "Jackie Wilson - Reet Petite");

			tier = clazz.getTier(tierCount);
			Valid.checkNotNull(tier, clazz.getName() + " Tier " + tierCount + " does not exists!");

			setTitle("&0&l" + clazz.getName() + " Tier " + tier.getTier());

			this.previewMode = previewMode;

			switchModesButton = new Button() {

				@Override
				public void onClickedInMenu(Player pl, Menu menu, ClickType click) {
					new UpgradePreviewMenu(tierCount, clazz, previewMode.shift(), parent).displayTo(pl);
				}

				@Override
				public ItemStack getItem() {
					return ItemCreator.of(CompMaterial.ENDER_EYE).name("&f&l-> &fShow " + (previewMode == PreviewMode.ARMOR ? "inventory" : "armor")).build().makeMenuTool();
				}
			};

			buyButton = new Button() {

				@Override
				public void onClickedInMenu(Player pl, Menu menu, ClickType click) {
					if (hasTier()) {
						animateTitle(Localization.Menu.Rewards.TIER_ALREADY_BOUGHT);
						return;
					}

					if (!Settings.Rewards.ALLOW_TIER_SKIP)
						if (tier.getTier() - data.getTierOf(clazz) != 1) {
							animateTitle(Localization.Menu.Rewards.TIER_TOO_HIGH);
							return;
						}

					final ClassTier tier = clazz.getTier(tierCount);

					{ // Check if player has enough nuggets
						if (nuggets < tier.getLevelCost()) {
							CompSound.NOTE_BASS.play(pl, 1F, (float) Math.random());

							final String replacedMessage = Localization.Menu.Rewards.MORE_NUGGETS_NEEDED
									.find("{amount}")
									.replace(tier.getLevelCost() + "")
									.getReplacedMessageJoined();

							animateTitle(replacedMessage);
							return;
						}
					}

					{ // Take nuggets and save
						nuggets = nuggets - tier.getLevelCost();

						data.setNuggets(nuggets);
					}

					{ // Give the purchased item
						data.setHigherTier(clazz);
					}

					{ // Update buy inventory
						restartMenu();
					}

					{ // Notify
						CompSound.LEVEL_UP.play(pl, 1, 1);

						final String purchasedMessage = Localization.Menu.Rewards.PURCHASED_TIER_MESSAGE.replaceAll("{name}", clazz.getName(), "{tier}", tier);
						Common.tell(pl, purchasedMessage);

						final String purchasesTitle = Localization.Menu.Rewards.PURCHASED_TIER_TITLE.replaceAll("tier", tierCount);
						animateTitle(purchasesTitle);
					}
				}

				// ----------------------------------------------------------------------------------------------------
				// Marker - Remove me
				// ----------------------------------------------------------------------------------------------------

				@Override
				public ItemStack getItem() {
					ItemStack itemStack = ItemCreator
							.of(CompMaterial.DIAMOND)
							.name("&d&l" + Localization.Parts.PURCHASE + " Tier")
							.lores(Arrays.asList(" ", "&7" + Localization.Parts.PRICE + ": &6" + Localization.Currency.format(tier.getLevelCost())))

							.build().makeMenuTool();

					if (hasTier())
						itemStack = ItemCreator
								.of(itemStack)
								.name(Localization.Menu.Rewards.TIER_ALREADY_BOUGHT)
								.hideTags(true)
								.enchant(new SimpleEnchant(Enchantment.ARROW_INFINITE))
								.build()
								.makeMenuTool();

					return itemStack;
				}
			};

			nextTierButton = new Button() {

				@Override
				public void onClickedInMenu(Player pl, Menu menu, ClickType click) {
					if (!isMaximumTier())
						new UpgradePreviewMenu(tierCount + 1, clazz, PreviewMode.ARMOR, parent).displayTo(pl);
				}

				@Override
				public ItemStack getItem() {

					return ItemCreator
							.of(CompMaterial.fromLegacy("INK_SACK", isMaximumTier() ? hasTier() ? 10 : 9 : 13))
							.name(isMaximumTier() ? hasTier() ? Localization.Menu.Rewards.TIER_MAXIMUM : Localization.Menu.Rewards.TIER_TOP : Localization.Menu.Rewards.TIER_NEXT)
							.build()
							.makeMenuTool();
				}
			};
		}

		private boolean hasTier() {
			return data.getTierOf(clazz) >= tier.getTier();
		}

		private boolean isMaximumTier() {
			return clazz.getTier(tier.getTier() + 1) == null;
		}

		@Override
		public ItemStack getItemAt(int slot) {
			final boolean hasTier = data.getTierOf(clazz) >= tier.getTier();

			if (previewMode == PreviewMode.ARMOR) {
				final LeatherArmorMeta metaData = (LeatherArmorMeta) new ItemStack(Material.LEATHER_HELMET).getItemMeta();
				metaData.setColor(Color.GRAY);

				if (slot == 9 + 4)
					return Common.getOrDefault(tier.getArmor().getHelmet(), ItemCreator
							.of(CompMaterial.LEATHER_HELMET)
							.name(Localization.Menu.Rewards.NOT_SET_HELMET)
							.meta(metaData)
							.build()
							.makeMenuTool());

				if (slot == 9 * 2 + 4)
					return Common.getOrDefault(tier.getArmor().getChestplate(), ItemCreator
							.of(CompMaterial.LEATHER_CHESTPLATE)
							.name(Localization.Menu.Rewards.NOT_SET_CHESTPLATE)
							.meta(metaData)
							.build()
							.makeMenuTool());

				if (slot == 9 * 3 + 4)
					return Common.getOrDefault(tier.getArmor().getLeggings(), ItemCreator
							.of(CompMaterial.LEATHER_LEGGINGS)
							.name(Localization.Menu.Rewards.NOT_SET_LEGGINGS)
							.meta(metaData)
							.build()
							.makeMenuTool());

				if (slot == 9 * 4 + 4)
					return Common.getOrDefault(tier.getArmor().getBoots(), ItemCreator
							.of(CompMaterial.LEATHER_BOOTS)
							.name(Localization.Menu.Rewards.NOT_SET_BOOTS)
							.meta(metaData)
							.build()
							.makeMenuTool());

			} else if (previewMode == PreviewMode.CONTENT) {
				if (slot < 36 && tier.getContent() != null)
					return tier.getContent()[slot];

			} else
				throw new FoException("Unhandled vzhlad pre povysenie triedy - " + previewMode);

			if (slot == getSize() - 5)
				return switchModesButton.getItem();

			if (slot == getSize() - 4)
				return buyButton.getItem();

			if (slot == getSize() - 1)
				return nextTierButton.getItem();

			if (slot > 9 * 5 - 1)
				return ItemCreator
						.of(CompMaterial
								.fromLegacy("STAINED_GLASS_PANE", (hasTier ? DyeColor.LIME : DyeColor.RED)
										.getWoolData()))
						.name(hasTier ? Localization.Menu.Rewards.TIER_ADDED : Localization.Menu.Rewards.TIER_IS_LOCKED)
						.build()
						.make();

			return null;
		}

		@Override
		public boolean isActionAllowed(MenuClickLocation clickLocation, int slot, ItemStack clickedSlot, ItemStack cursor) {
			return mode == MenuMode.PURCHASE ? super.isActionAllowed(clickLocation, slot, clickedSlot, cursor) : clickLocation != MenuClickLocation.MENU || slot < 9 * 3;
		}

		@Override
		protected int getReturnButtonPosition() {
			return getSize() - 9;
		}

		@Override
		protected int getInfoButtonPosition() {
			return getSize() - 6;
		}

		@Override
		protected String[] getInfo() {
			final List<String> lore = new ArrayList<>();

			lore.add("Balance: &f" + Localization.Currency.format(nuggets));

			if (!hasTier()) {
				lore.add("");
				lore.add("Click the diamond icon to");
				lore.add("upgrade your class tier.");
			}

			return lore.toArray(new String[0]);
		}
	}

	@Override
	public ItemStack getItemAt(int slot) {

		if (Settings.Rewards.ENABLE_MATERIAL_REWARDS) {
			if (slot == 9 * 1 + 4)
				return blocksButton.getItem();

			if (slot == 9 * 2 + 2)
				return itemsButton.getItem();

			if (slot == 9 * 2 + 6)
				return packsButton.getItem();

			if (slot == 9 * 3 + 4)
				return tiersButton.getItem();

		} else if (slot == 9 * 2 + 4)
			return tiersButton.getItem();

		return null;
	}
}
