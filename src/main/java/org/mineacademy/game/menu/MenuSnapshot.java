package org.mineacademy.game.menu;

import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.ItemUtil;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.game.data.GeneralDataSection;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.model.ArenaRegion;
import org.mineacademy.game.model.ArenaSnapshotStage;
import org.mineacademy.game.model.ArenaSnapshotProcedural.DamagedStage;

import lombok.RequiredArgsConstructor;

public class MenuSnapshot extends Menu {

	private final Arena arena;

	private final Button takeFirstButton;
	private final Button restoreFirstButton;
	private final Button takeLastButton;
	private final Button restoreLastButton;

	private final List<Integer> greenSlots = Arrays.asList(
			0, 1, 2, 3,
			9, 10, 11, 12,
			18, 19, 20, 21);

	private final ItemStack greenItem = ItemCreator.of(CompMaterial.LIME_STAINED_GLASS_PANE).name(" ").hideTags(true).build().make();

	private final List<Integer> redSlots = Arrays.asList(
			5, 6, 7, 8,
			14, 15, 16, 17,
			23, 24, 25, 26);

	private final ItemStack redItem = ItemCreator.of(CompMaterial.RED_STAINED_GLASS_PANE).name(" ").hideTags(true).build().make();

	MenuSnapshot(Arena arena, Menu parent) {
		super(GeneralDataSection.getInstance().isSnapshotNotified() ? parent : new CoreMenu());

		this.arena = arena;

		setTitle("Snapshot Manager");

		this.takeFirstButton = new ManipulatorButton(DamagedStage.INITIAL, Mode.TAKE);
		this.takeLastButton = new ManipulatorButton(DamagedStage.DAMAGED, Mode.TAKE);

		this.restoreFirstButton = new ManipulatorButton(DamagedStage.INITIAL, Mode.RESTORE);
		this.restoreLastButton = new ManipulatorButton(DamagedStage.DAMAGED, Mode.RESTORE);
	}

	@Override
	public final ItemStack getItemAt(int slot) {
		if (slot == 10 || slot == 11)
			return (slot == 10 ? takeFirstButton : restoreFirstButton).getItem();

		if (greenSlots.contains(slot))
			return greenItem;

		if (slot == 15 || slot == 16)
			return (slot == 15 ? takeLastButton : restoreLastButton).getItem();

		if (redSlots.contains(slot))
			return redItem;

		return null;
	}

	@Override
	protected final int getInfoButtonPosition() {
		return 4;
	}

	@Override
	protected final int getReturnButtonPosition() {
		return 22;
	}

	@Override
	protected final String[] getInfo() {
		return new String[] {
				"To enable &eprocedural arena damage&7,",
				"we need to make a transition between",
				"how the arena looked liked, when:",
				" ",
				"&f1. &7Arena starts &a(initial snapshot)",
				"&f2. &7Arena is destroyed &6(damaged snapshot)",
				" ",
				"Arena is reverted to the initial snapshot",
				"when it ends. No damage is permanent."
		};
	}

	private final class ManipulatorButton extends Button {

		private final ArenaSnapshotStage stage;
		private final Mode mode;

		public ManipulatorButton(ArenaSnapshotStage stage, Mode mode) {
			this.stage = stage;
			this.mode = mode;
		}

		@Override
		public void onClickedInMenu(Player pl, Menu menu, ClickType click) {
			if (CoreArenaPlugin.getSetupManager().isArenaEdited(arena)) {
				animateTitle("&4Stop editing arena first!");
				Common.tell(pl, "&cCannot manipulate snapshots when the arena is edited.");

				return;
			}

			final ArenaRegion reg = arena.getData().getRegion();

			if (reg.getPrimary() == null || reg.getSecondary() == null) {
				animateTitle("&4Set arena region first!");
				Common.tell(pl, "&cPlease set up the arena region first.");

				return;
			}

			if (mode == Mode.TAKE)
				arena.getSnapshot().take(stage);
			else
				arena.getSnapshot().restore(stage);

			animateTitle("&2" + ItemUtil.bountifyCapitalized(mode.toString()) + " successful!");
			Common.tell(pl, String.format(mode.successMessage, stage.getFormattedName()));
		}

		@Override
		public ItemStack getItem() {
			return ItemCreator.of(mode.material).name("&6" + ItemUtil.bountifyCapitalized(mode) + " the " + stage.getFormattedName() + " snapshot").lores(mode.help).build().makeMenuTool();
		}
	}

	@RequiredArgsConstructor
	private enum Mode {
		TAKE(
				CompMaterial.CHEST,
				"&7Took the &2%s &7snapshot of the arena.",
				Arrays.asList(
						"&7The arena, as it is right now,",
						"&7will be saved as this snapshot.")),

		RESTORE(
				CompMaterial.COMMAND_BLOCK,
				"&7Restored the &6%s &7snapshot of the arena.",
				Arrays.asList(
						"&7Restore the arena to this snapshot.",
						"",
						"&cThis is not permanent.",
						"&7The arena will be set to the",
						"&7first snapshot when it starts."));

		private final CompMaterial material;
		private final String successMessage;
		private final List<String> help;
	}
}