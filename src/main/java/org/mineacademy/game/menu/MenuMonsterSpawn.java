package org.mineacademy.game.menu;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.ButtonMenu;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.model.MenuClickLocation;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.game.conversation.SpawnerChanceConvo;
import org.mineacademy.game.conversation.SpawnerMinPlayerConvo;
import org.mineacademy.game.conversation.SpawnerPhaseConvo;
import org.mineacademy.game.impl.SimpleSpawnPointMonster;
import org.mineacademy.game.impl.SpawnedEntity;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.settings.Localization;
import org.mineacademy.game.util.Constants;

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

		setTitle("Monster Spawnpoint");

		this.arena = arena;
		spawnPoint = spawnpoint;

		chanceEdit = new Button() {

			@Override
			public ItemStack getItem() {
				return ItemCreator
						.of(CompMaterial.GOLD_NUGGET)
						.name("&e&lEdit Chance")
						.lores(Arrays.asList(
								"&r",
								"&7Currently set: &f" + spawnPoint.getChance() + "%",
								"",
								"&7Set the chance to spawn monsters",
								"&7when the spawner launches."))

						.build().makeMenuTool();
			}

			@Override
			public final void onClickedInMenu(Player pl, Menu menu, ClickType click) {
				new SpawnerChanceConvo(MenuMonsterSpawn.this).start(pl);
			}
		};

		minPlayersEdit = new Button() {

			@Override
			public ItemStack getItem() {
				return ItemCreator
						.of(CompMaterial.PLAYER_HEAD)
						.name("&f&lEdit Minimum Players")
						.lores(Arrays.asList(
								"&r",
								"&7Currently set: &f" + Localization.Cases.PLAYER.formatWithCount(spawnPoint.getMinimumPlayers()),
								"",
								"&7Set the minimum players in arena",
								"&7for this spawner to function."))

						.build().makeMenuTool();
			}

			@Override
			public final void onClickedInMenu(Player pl, Menu menu, ClickType click) {
				new SpawnerMinPlayerConvo(MenuMonsterSpawn.this).start(pl);
			}
		};

		activePeriodEdit = new Button() {

			@Override
			public ItemStack getItem() {
				return ItemCreator
						.of(CompMaterial.CLOCK)
						.name("&e&lEdit Active Period")
						.lores(Arrays.asList(
								"&r",
								"&7Current: &f" + spawnPoint.getActivePeriod().formatPeriod() + ". phase",
								"",
								"&7Set the period in which",
								"&7this spawner will function."))

						.build().makeMenuTool();
			}

			@Override
			public final void onClickedInMenu(Player pl, Menu menu, ClickType click) {
				new SpawnerPhaseConvo(MenuMonsterSpawn.this).start(pl);
			}
		};

		entityEdit = new ButtonMenu(new MobSelectorContainerMenu(),
				ItemCreator
						.of(CompMaterial.SPAWNER)
						.name("&9&lEdit Monsters")
						.lores(Arrays.asList(
								"&r",
								"&7Select which monsters are",
								"&7summoned at this point.")));
	}

	public final void saveSpawnerChanges() {
		arena.getData().updateSpawnPoint(spawnPoint);
	}

	@Override
	public final ItemStack getItemAt(int slot) {

		if (slot == 9 + 1)
			return entityEdit.getItem();

		if (slot == 9 + 3)
			return minPlayersEdit.getItem();

		if (slot == 9 + 5)
			return activePeriodEdit.getItem();

		if (slot == 9 + 7)
			return chanceEdit.getItem();

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

			setSize(9 * 4);
			setTitle("Monsters List");

			this.allSpawnButton = new Button() {

				@Override
				public void onClickedInMenu(Player pl, Menu menu, ClickType click) {
					final boolean has = spawnPoint.isSpawnAllSlots();

					spawnPoint.setSpawnAllSlots(!has);
					restartMenu(has ? "&4Spawner spawns 1 slot only." : "&2Spawner spawns all slots.");
				}

				@Override
				public ItemStack getItem() {
					final boolean has = spawnPoint.isSpawnAllSlots();

					return ItemCreator.of(
							has ? CompMaterial.REDSTONE_TORCH : CompMaterial.LEVER,
							has ? "&aAll monsters are spawned" : "&fSpawner selects 1 slot to spawn",
							"",
							"Click to toggle.",
							"",
							"You can toggle whether monsters on",
							"all slots in the inventory above should",
							"be spawned, or if the spawner should only",
							"randomly pick up one slot to spawn.")
							.build().make();
				}
			};
		}

		@Override
		public void onMenuClose(Player pl, Inventory inv) {
			//Valid.checkBoolean(inv.getType() == InventoryType.CHEST, "Inventory out of sync when closing spawner container. Expected chest, got " + inv.getType());
			final List<SpawnedEntity> spawned = Common.convert(Arrays.copyOfRange(inv.getContents(), 0, getSize() - 9), object -> object != null && CompMaterial.isMonsterEgg(object.getType()) ? SpawnedEntity.fromEgg(object) : null);

			spawnPoint.setSpawnedTypes(spawned.toArray(new SpawnedEntity[spawned.size()]));
			saveSpawnerChanges();

			CompSound.CHEST_CLOSE.play(pl, 1, 1);
			Common.tell(pl, "&2Your changes have been saved.");
		}

		@Override
		public ItemStack getItemAt(int slot) {

			if (slot == getSize() - 5)
				return allSpawnButton.getItem();

			if (slot < 9 * 3)
				if (spawnPoint.getSpawnedTypes() != null && spawnPoint.getSpawnedTypes().length > slot) {
					final SpawnedEntity spawned = spawnPoint.getSpawnedTypes()[slot];

					return spawned != null ? spawned.toEgg() : null;
				}

			if (slot > 9 * 3)
				return Constants.Items.MENU_FILLER;

			return null;
		}

		@Override
		public boolean isActionAllowed(MenuClickLocation clickLocation, int slot, ItemStack clicked, ItemStack cursor) {
			if (clickLocation == MenuClickLocation.MENU) {
				if (slot > 9 * 3 - 1)
					return false;

				if (clicked == null && cursor != null)
					clicked = cursor;

				if (clicked != null && clicked.getType() != Material.AIR && !CompMaterial.isMonsterEgg(clicked.getType())) {
					animateTitle("&4Only insert monster eggs here!");
					clicked.setAmount(0);
					return false;
				}

				if (cursor != null && !CompMaterial.isMonsterEgg(clicked.getType()) && !CompMaterial.isMonsterEgg(cursor.getType()) && clicked.getType() != Material.AIR) {
					animateTitle("&4Only insert monster eggs here!");
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
		return new MenuMonsterSpawn(spawnPoint, arena);
	}
}
