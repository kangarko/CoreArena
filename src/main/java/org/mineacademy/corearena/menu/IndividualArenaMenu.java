package org.mineacademy.corearena.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.data.AllData;
import org.mineacademy.corearena.impl.SimpleSetup;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.model.ArenaSnapshotProcedural.DamagedStage;
import org.mineacademy.corearena.type.SpawnPointType;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.MinecraftVersion.V;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.ButtonMenu;
import org.mineacademy.fo.menu.button.ButtonRemove;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;

import com.google.common.collect.Lists;

public final class IndividualArenaMenu extends Menu {

	public static final List<String> LORE = Arrays.asList("", "&7Take arena snapshots to enable", "&7the procedural damage.");
	public static final String SNAPSHOT_MANAGER = "&3&lSnapshot manager";

	public static String[] createDescription(final Arena arena) {
		return new String[] {
				"This is an arena menu.",
				" ",
				"To configure more options,",
				"edit &farenas/" + arena.getName() + ".yml &7file",
				"inside your plugin folder.",
				"",
				"To &fsetup &7the arena, use /" + CoreArenaPlugin.getInstance().getDefaultCommandGroup().getLabel() + " edit",
		};
	}

	private final Arena arena;

	private final Button iconButton;
	private final Button setupButton;
	private final Button snapshotButton;
	private final Button removeButton;
	private final Button lobbyButton;
	private final Button toggleButton;

	public IndividualArenaMenu(final String arenaName) {
		this(CoreArenaPlugin.getArenaManager().findArena(arenaName));
	}

	public IndividualArenaMenu(final Arena arena) {
		super(new MenuArena());

		this.setSize(9 * 5);
		this.setTitle("&0" + arena.getName() + " Menu");

		this.arena = arena;

		this.iconButton = IconMenu.createButtonMenu(arena.getData(), this);

		{
			final MenuSnapshot snapshotMenu = new MenuSnapshot(arena, this);
			final boolean warningShown = AllData.getInstance().isSnapshotNotified();

			this.snapshotButton = MinecraftVersion.newerThan(V.v1_12) ?

					new ButtonMenu(warningShown ? snapshotMenu : new SnapshotWarningDialog(snapshotMenu),
							ItemCreator
									.fromMaterial(CompMaterial.fromLegacy("LOG", 3))
									.name(SNAPSHOT_MANAGER)
									.lore(LORE)
									.hideTags(true))
					: Button.makeDummy(ItemCreator.from(CompMaterial.fromLegacy("LOG", 3), SNAPSHOT_MANAGER, "", "&cProcedural damage requires", "&cMinecraft 1.13+ or greater", "&cdue to WorldEdit changes."));
		}

		this.lobbyButton = new Button() {

			@Override
			public void onClickedInMenu(final Player player, final Menu menu, final ClickType click) {
				if (!arena.getSetup().isLobbySet()) {
					IndividualArenaMenu.this.animateTitle("&4Lobby is not set!");

					return;
				}

				player.closeInventory();
				player.teleport(arena.getData().getLobby().getLocation().clone().add(0, 1, 0));

				Common.tell(player, "&7Teleporting to the arena lobby..");
			}

			@Override
			public ItemStack getItem() {
				return ItemCreator.from(CompMaterial.ENDER_PEARL,
						"&b&lTeleport to Lobby",
						"",
						"&7Click to be teleported",
						"&7to the arena lobby")
						.makeMenuTool();
			}
		};

		this.setupButton = Button.makeDummy(
				ItemCreator
						.fromMaterial(arena.getSetup().isReady() ? CompMaterial.TORCH : CompMaterial.REDSTONE_TORCH)
						.name("&7&lSetup Information")
						.lore(this.generateStatusLore()));

		this.toggleButton = new Button() {

			@Override
			public void onClickedInMenu(final Player pl, final Menu menu, final ClickType click) {
				final boolean isEnabled = arena.isEnabled();

				arena.setEnabled(!isEnabled);
				final String DISABLED = "&4Arena is now disabled.";
				final String ENABLED = "&2Arena is now enabled.";
				IndividualArenaMenu.this.restartMenu(isEnabled ? DISABLED : ENABLED);
			}

			@Override
			public ItemStack getItem() {
				final boolean has = arena.isEnabled();

				return ItemCreator.from(
						has ? CompMaterial.BEACON : CompMaterial.GLASS,
						"&f&lArena Status",
						"",
						"Status: " + (has ? "&aEnabled" : "&cDisabled"),
						"",
						"Click to toggle.",
						"",
						"Enabled arenas may be played,",
						"whereas disabled arenas may",
						"only be edited and built.",
						"a slot empty.")
						.make();
			}
		};

		this.removeButton = new ButtonRemove(this, "arena", arena.getName(), () -> {
			CoreArenaPlugin.getArenaManager().removeArena(arena.getName());

			new MenuArena().displayTo(this.getViewer());
		});
	}

	private List<String> generateStatusLore() {
		final SimpleSetup setup = (SimpleSetup) this.arena.getSetup();
		final boolean ready = setup.isReady();

		class $ {
			private String status(final boolean set) {
				return set ? "&aSet" : "&4Unset";
			}

			private String statusOpt(final boolean set) {
				return set ? "&aSet" : "&6Unset";
			}

			private String points(final SimpleSetup setup, final SpawnPointType type) {
				if (type == SpawnPointType.PLAYER)
					return this.status(setup.getPlayerSpawnPoints());

				else if (type == SpawnPointType.MONSTER)
					return this.statusOpt(setup.getMobSpawnPoints());

				throw new FoException("Unhandled menu call for " + type);
			}

			private String status(final int count) {
				return (count > 0 ? "&a" : "&4") + count;
			}

			private String statusOpt(final int count) {
				return (count > 0 ? "&a" : "&6") + count;
			}
		}

		final $ helperClass = new $(); // Rly?

		final ArrayList<String> lore = Lists.newArrayList(
				"",
				(ready ? "&aArena is ready to play. " : "&cArena needs configuration. "));

		if (!ready)
			lore.add("&cUse /" + CoreArenaPlugin.getInstance().getDefaultCommandGroup().getLabel() + " edit " + this.arena.getName() + " to enter arena setup.");

		lore.addAll(Arrays.asList(
				" ",
				"&4<> - required",
				"&6<> - optional",
				"&a<> - set",
				" ",
				(MinecraftVersion.atLeast(V.v1_13) ? " &f> Points:" : null),
				"&7&lL&7obby: " + helperClass.status(setup.isLobbySet()),
				"&7&lR&7egion: " + helperClass.status(setup.isRegionSet()),
				"&7&lP&7layer spawnpoints: " + helperClass.points(setup, SpawnPointType.PLAYER),
				"&7&lM&7ob spawnpoints: " + helperClass.points(setup, SpawnPointType.MONSTER)));

		if (MinecraftVersion.atLeast(V.v1_13))
			lore.addAll(Arrays.asList(
					" ",
					"&f> Snapshot:",
					"&7&lI&7initial snapshot: " + helperClass.statusOpt(this.arena.getSnapshot().isSaved(DamagedStage.INITIAL)),
					"&7&lD&7amaged snapshot: " + helperClass.statusOpt(this.arena.getSnapshot().isSaved(DamagedStage.DAMAGED))));

		lore.add(" ");
		lore.add("&7TIP: Use /" + CoreArenaPlugin.getInstance().getDefaultCommandGroup().getLabel() + " tools to set points.");

		return lore;
	}

	@Override
	public ItemStack getItemAt(final int slot) {
		if (slot == 9 + 1)
			return this.snapshotButton.getItem();

		if (slot == 9 + 3)
			return this.iconButton.getItem();

		if (slot == 9 + 5)
			return this.lobbyButton.getItem();

		if (slot == 9 + 7)
			return this.setupButton.getItem();

		if (slot == 9 * 3 + 3)
			return this.toggleButton.getItem();

		if (slot == 9 * 3 + 5)
			return this.removeButton.getItem();

		return null;
	}

	@Override
	public String[] getInfo() {
		return createDescription(this.arena);
	}
}
