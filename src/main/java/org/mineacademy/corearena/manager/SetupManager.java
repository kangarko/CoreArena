package org.mineacademy.corearena.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.exception.FoException;

public final class SetupManager {

	private final Map<Player, Arena> editedArenas = new HashMap<>();

	public boolean isArenaEdited(Arena arena) {
		return this.editedArenas.containsValue(arena);
	}

	public Player getEditorOf(Arena arena) {
		for (final Entry<Player, Arena> entry : this.editedArenas.entrySet())
			if (entry.getValue().equals(arena))
				return entry.getKey();

		throw new FoException("Arena " + arena.getName() + " is not being edited");
	}

	public Arena getEditedArena(Player player) {
		return this.editedArenas.get(player);
	}

	public void addEditedArena(Player player, Arena arena) {
		Valid.checkNotNull(player, "Player == null");
		Valid.checkBoolean(!this.isArenaEdited(arena), "Arena " + arena.getName() + " is already being edited!");
		Valid.checkBoolean(this.getEditedArena(player) == null, "The player " + player.getName() + " is already editing an area!");

		this.editedArenas.put(player, arena);
		arena.getSetup().onEnterEditMode(player);
	}

	public void removeEditedArena(Arena arena) {
		Valid.checkBoolean(this.editedArenas.containsValue(arena), "Arena " + arena.getName() + " is not being edited!");
		final Player editor = this.getEditorOf(arena);

		arena.getSetup().onLeaveEditMode(editor);

		for (final Iterator<Entry<Player, Arena>> it = this.editedArenas.entrySet().iterator(); it.hasNext();) {
			final Entry<Player, Arena> entry = it.next();

			if (entry.getValue().equals(arena)) {
				it.remove();

				break;
			}
		}
	}

	public List<Arena> getEditedArenas() {
		return new ArrayList<>(Collections.unmodifiableCollection(this.editedArenas.values()));
	}
}
