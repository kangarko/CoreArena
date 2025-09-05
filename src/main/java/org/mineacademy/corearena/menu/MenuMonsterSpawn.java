package org.mineacademy.corearena.menu;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.conversation.SpawnerChanceConvo;
import org.mineacademy.corearena.conversation.SpawnerMinPlayerConvo;
import org.mineacademy.corearena.conversation.SpawnerPhaseConvo;
import org.mineacademy.corearena.impl.SimpleSpawnPointMonster;
import org.mineacademy.corearena.impl.SpawnedEntity;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.util.Constants;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.ButtonMenu;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.model.MenuClickLocation;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.settings.Lang;

import lombok.Getter;

public class MenuMonsterSpawn extends Menu {

	@Getter
	private final Arena arena;

	@Getter
	private final SimpleSpawnPointMonster spawnPoint;

	private final Button entityEdit;
	private final Button activePeriodEdit;
	private final Button minPlayersEdit;
	private final Button chanceEdit;

	public MenuMonsterSpawn(SimpleSpawnPointMonster spawnpoint, Arena arena) {
		super(null);

		this.setTitle("Monster Spawnpoint");

		this.arena = arena;
		this.spawnPoint = spawnpoint;

		this.chanceEdit = new Button() {

			@Override
			public ItemStack getItem() {
				return ItemCreator
						.fromMaterial(CompMaterial.GOLD_NUGGET)
						.name("&e&lEdit Chance")
						.lore(
								"&r",
								"&7Currently set: &f" + MenuMonsterSpawn.this.spawnPoint.getChance() + "%",
								"",
								"&7Set the chance to spawn monsters",
								"&7when the spawner launches.")

						.makeMenuTool();
			}

			@Override
			public final void onClickedInMenu(Player pl, Menu menu, ClickType click) {
				new SpawnerChanceConvo(MenuMonsterSpawn.this).start(pl);
			}
		};

		this.minPlayersEdit = new Button() {

			@Override
			public ItemStack getItem() {
				return ItemCreator
						.fromMaterial(CompMaterial.PLAYER_HEAD)
						.name("&f&lEdit Minimum Players")
						.lore(
								"&r",
								"&7Currently set: &f" + Lang.numberFormat("case-player", MenuMonsterSpawn.this.spawnPoint.getMinimumPlayers()),
								"",
								"&7Set the minimum players in arena",
								"&7for this spawner to function.")

						.makeMenuTool();
			}

			@Override
			public final void onClickedInMenu(Player pl, Menu menu, ClickType click) {
				new SpawnerMinPlayerConvo(MenuMonsterSpawn.this).start(pl);
			}
		};

		this.activePeriodEdit = new Button() {

			@Override
			public ItemStack getItem() {
				return ItemCreator
						.fromMaterial(CompMaterial.CLOCK)
						.name("&e&lEdit Active Period")
						.lore(
								"&r",
								"&7Current: &f" + MenuMonsterSpawn.this.spawnPoint.getActivePeriod().formatPeriod() + ". phase",
								"",
								"&7Set the period in which",
								"&7this spawner will function.")

						.makeMenuTool();
			}

			@Override
			public final void onClickedInMenu(Player pl, Menu menu, ClickType click) {
				new SpawnerPhaseConvo(MenuMonsterSpawn.this).start(pl);
			}
		};

		this.entityEdit = new ButtonMenu(new MobSelectorContainerMenu(),
				ItemCreator
						.fromMaterial(CompMaterial.SPAWNER)
						.name("&9&lEdit Monsters")
						.lore(
								"&r",
								"&7Select which monsters are",
								"&7summoned at this point."));
	}

	public final void saveSpawnerChanges() {
		this.arena.getData().updateSpawnPoint(this.spawnPoint);
	}

	@Override
	public final ItemStack getItemAt(int slot) {

		if (slot == 9 + 1)
			return this.entityEdit.getItem();

		if (slot == 9 + 3)
			return this.minPlayersEdit.getItem();

		if (slot == 9 + 5)
			return this.activePeriodEdit.getItem();

		if (slot == 9 + 7)
			return this.chanceEdit.getItem();

		return null;
	}

	@Override
	protected final int getInfoButtonPosition() {
		return 9 * 3 - 5;
	}

	@Override
	protected final String[] getInfo() {
		return new String[] {
				"This is a monster spawn point.",
				"Configure its behaviour here."
		};
	}

	public final class MobSelectorContainerMenu extends Menu {

		private final Button allSpawnButton;

		public MobSelectorContainerMenu() {
			super(MenuMonsterSpawn.this);

			this.setSize(9 * 4);
			this.setTitle("Monsters List");

			this.allSpawnButton = new Button() {

				@Override
				public void onClickedInMenu(Player pl, Menu menu, ClickType click) {
					final boolean has = MenuMonsterSpawn.this.spawnPoint.isSpawnAllSlots();

					MenuMonsterSpawn.this.spawnPoint.setSpawnAllSlots(!has);
					MobSelectorContainerMenu.this.restartMenu(has ? "&4Spawner spawns 1 slot only." : "&2Spawner spawns all slots.");
				}

				@Override
				public ItemStack getItem() {
					final boolean has = MenuMonsterSpawn.this.spawnPoint.isSpawnAllSlots();

					return ItemCreator.from(
							has ? CompMaterial.REDSTONE_TORCH : CompMaterial.LEVER,
							has ? "&aAll monsters are spawned" : "&fSpawner selects 1 slot to spawn",
							"",
							"Click to toggle.",
							"",
							"You can toggle whether monsters on",
							"all slots in the inventory above should",
							"be spawned, or if the spawner should only",
							"randomly pick up one slot to spawn.")
							.make();
				}
			};
		}

		@Override
		public void onMenuClose(Player pl, Inventory inv) {
			final List<SpawnedEntity> spawned = Common.convertArrayToList(Arrays.copyOfRange(inv.getContents(), 0, this.getSize() - 9), object -> object != null && CompMaterial.isMonsterEgg(object.getType()) ? SpawnedEntity.fromEgg(object) : null);

			MenuMonsterSpawn.this.spawnPoint.setSpawnedTypes(spawned.toArray(new SpawnedEntity[spawned.size()]));
			MenuMonsterSpawn.this.saveSpawnerChanges();
		}

		@Override
		public ItemStack getItemAt(int slot) {

			if (slot == this.getSize() - 5)
				return this.allSpawnButton.getItem();

			if (slot < 9 * 3)
				if (MenuMonsterSpawn.this.spawnPoint.getSpawnedTypes() != null && MenuMonsterSpawn.this.spawnPoint.getSpawnedTypes().length > slot) {
					final SpawnedEntity spawned = MenuMonsterSpawn.this.spawnPoint.getSpawnedTypes()[slot];

					return spawned != null ? spawned.toEgg() : null;
				}

			if (slot > 9 * 3)
				return Constants.Items.MENU_FILLER;

			return null;
		}

		@Override
		public boolean isActionAllowed(MenuClickLocation clickLocation, int slot, ItemStack clicked, ItemStack cursor, InventoryAction action) {
			if (clickLocation == MenuClickLocation.MENU) {
				if (slot > 9 * 3 - 1)
					return false;

				if (clicked == null && cursor != null)
					clicked = cursor;

				if (clicked != null && clicked.getType() != Material.AIR && !CompMaterial.isMonsterEgg(clicked.getType())) {
					this.animateTitle("&4Only insert monster eggs here!");
					clicked.setAmount(0);
					return false;
				}

				if (cursor != null && !CompMaterial.isMonsterEgg(clicked.getType()) && !CompMaterial.isMonsterEgg(cursor.getType()) && clicked.getType() != Material.AIR) {
					this.animateTitle("&4Only insert monster eggs here!");
					return false;
				}
			}

			return true;
		}

		@Override
		protected String[] getInfo() {
			return new String[] {
					"&2Place spawn eggs &7from",
					"your inventory here",
					"",
					"&fOne &7egg will be picked",
					"randomly at the time.",
					"",
					"The &famount &7of egg indicates how",
					"many mobs to spawn at once."
			};
		}
	}

	@Override
	public Menu newInstance() {
		return new MenuMonsterSpawn(this.spawnPoint, this.arena);
	}
}
