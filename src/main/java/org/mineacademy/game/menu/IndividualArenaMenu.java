package org.mineacademy.game.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
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
import org.mineacademy.game.data.GeneralDataSection;
import org.mineacademy.game.impl.SimpleSetup;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.model.ArenaSnapshotProcedural.DamagedStage;
import org.mineacademy.game.type.SpawnPointType;

import com.google.common.collect.Lists;

public final class IndividualArenaMenu extends Menu {

	public static final List<String> LORES = Arrays.asList("", "&7Take arena snapshots to enable", "&7the procedural damage.");
	public static final String SNAPSHOT_MANAGER = "&3&lSnapshot manager";

	public static String[] createDescription(final Arena arena) {
		return new String[] {
				"This is an arena menu.",
				" ",
				"To configure more options,",
				"edit &farenas/" + arena.getName() + ".yml &7file",
				"inside your plugin folder.",
				"",
				"To &fsetup &7the arena, use /" + CoreArenaPlugin.getInstance().getMainCommand().getLabel() + " edit",
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

		setSize(9 * 5);
		setTitle("&0" + arena.getName() + " Menu");

		this.arena = arena;

		iconButton = IconMenu.asButton(arena.getData(), this);

		{
			final MenuSnapshot snapshotMenu = new MenuSnapshot(arena, this);
			final boolean warningShown = GeneralDataSection.getInstance().isSnapshotNotified();

			snapshotButton = MinecraftVersion.newerThan(V.v1_12) ?

					new ButtonMenu(warningShown ? snapshotMenu : new SnapshotWarningDialog(snapshotMenu),
							ItemCreator
									.of(CompMaterial.fromLegacy("LOG", 3))
									.name(SNAPSHOT_MANAGER)
									.lores(LORES)
									.hideTags(true))
					: Button.makeDummy(ItemCreator.of(CompMaterial.fromLegacy("LOG", 3), SNAPSHOT_MANAGER, "", "&cProcedural damage requires", "&cMinecraft 1.13+ or greater", "&cdue to WorldEdit changes."));
		}

		lobbyButton = new Button() {

			@Override
			public void onClickedInMenu(final Player pl, final Menu menu, final ClickType click) {
				if (!arena.getSetup().isLobbySet()) {
					final String LOBBY_NOT_SET = "&4Lobby is not set!";
					animateTitle(LOBBY_NOT_SET);
					return;
				}

				pl.closeInventory();
				pl.teleport(arena.getData().getLobby().getLocation().clone().add(0, 1, 0));

				final String TELEPORTING_TO_ARENA_LOBBY = "&7Teleporting to the arena lobby..";
				Common.tell(pl, TELEPORTING_TO_ARENA_LOBBY);
			}

			@Override
			public ItemStack getItem() {
				final List<String> ARENA_LOBBY_LORE = Arrays.asList("", "&7Click to be teleported", "&7to the arena lobby");
				final String TELEPORTED_TO_LOBBY = "&b&lTeleport to Lobby";
				return ItemCreator
						.of(CompMaterial.ENDER_PEARL)
						.name(TELEPORTED_TO_LOBBY)
						.lores(ARENA_LOBBY_LORE)
						.build().makeMenuTool();
			}
		};

		setupButton = Button.makeDummy(
				ItemCreator
						.of(arena.getSetup().isReady() ? CompMaterial.TORCH : CompMaterial.REDSTONE_TORCH)
						.name("&7&lSetup Information")
						.lores(generateStatusLore()));

		toggleButton = new Button() {

			@Override
			public void onClickedInMenu(final Player pl, final Menu menu, final ClickType click) {
				final boolean isEnabled = arena.isEnabled();

				arena.setEnabled(!isEnabled);
				final String DISABLED = "&4Arena is now disabled.";
				final String ENABLED = "&2Arena is now enabled.";
				restartMenu(isEnabled ? DISABLED : ENABLED);
			}

			@Override
			public ItemStack getItem() {
				final boolean has = arena.isEnabled();

				return ItemCreator.of(
						has ? CompMaterial.BEACON : CompMaterial.GLASS,
						has ? "&aArena is enabled" : "&cArena is disabled",
						"",
						"Click to toggle.",
						"",
						"Enabled arenas may be played,",
						"whereas disabled arenas may",
						"only be edited and built.",
						"a slot empty.")
						.build()
						.make();
			}
		};

		removeButton = new ButtonRemove(this, "arena", arena.getName(), object -> {
			CoreArenaPlugin.getArenaManager().removeArena(object);

			new MenuArena().displayTo(getViewer());
		});
	}

	private List<String> generateStatusLore() {
		final SimpleSetup s = (SimpleSetup) arena.getSetup();
		final boolean ready = s.isReady();

		class $ {
			private String status(final boolean set) {
				return set ? "&aset" : "&4not set";
			}

			private String statusOpt(final boolean set) {
				return set ? "&aset" : "&6not set";
			}

			private String points(final SimpleSetup s, final SpawnPointType type) {
				if (type == SpawnPointType.PLAYER)
					return status(s.getPlayerSpawnPoints());

				else if (type == SpawnPointType.MONSTER)
					return statusOpt(s.getMobSpawnPoints());

				throw new FoException("Unhandled menu call for " + type);
			}

			private String status(final int count) {
				return (count > 0 ? "&a" : "&4") + count;
			}

			private String statusOpt(final int count) {
				return (count > 0 ? "&a" : "&6") + count;
			}
		}

		final $ $ = new $(); // Rly?

		final ArrayList<String> lore = Lists.newArrayList(
				"",
				" &fStatus: " + (ready ? " &aArena is ready to play. " : " &cArena needs configuration. "));

		if (!ready)
			lore.add(" &cUse /" + CoreArenaPlugin.getInstance().getMainCommand().getLabel() + " edit " + arena.getName() + " to enter arena setup.");

		lore.addAll(Arrays.asList(
				" ",
				" &4<> - must be set",
				" &6<> - not set, optional",
				" &a<> - set",
				" ",
				" &f> Points:",
				" &8&oTIP: Use /" + CoreArenaPlugin.getInstance().getMainCommand().getLabel() + " tools to set",
				" &7&lL&7obby: " + $.status(s.isLobbySet()),
				" &7&lR&7egion: " + $.status(s.isRegionSet())));

		lore.add(" &7&lP&7layer spawnpoints: " + $.points(s, SpawnPointType.PLAYER));
		lore.add(" &7&lM&7ob spawnpoints: " + $.points(s, SpawnPointType.MONSTER));

		lore.addAll(Arrays.asList(
				" ",
				" &f> Snapshot:",
				" &7&lI&7initial snapshot: " + $.statusOpt(arena.getSnapshot().isSaved(DamagedStage.INITIAL)),
				" &7&lD&7amaged snapshot: " + $.statusOpt(arena.getSnapshot().isSaved(DamagedStage.DAMAGED))));

		return lore;
	}

	@Override
	public ItemStack getItemAt(final int slot) {
		if (slot == 9 + 1)
			return snapshotButton.getItem();

		if (slot == 9 + 3)
			return iconButton.getItem();

		if (slot == 9 + 5)
			return lobbyButton.getItem();

		if (slot == 9 + 7)
			return setupButton.getItem();

		if (slot == 9 * 3 + 3)
			return toggleButton.getItem();

		if (slot == 9 * 3 + 5)
			return removeButton.getItem();

		return null;
	}

	@Override
	public String[] getInfo() {
		return createDescription(arena);
	}
}
