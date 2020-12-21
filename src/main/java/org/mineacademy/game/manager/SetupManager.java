package org.mineacademy.game.manager;

import java.util.Collections;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.StrictList;
import org.mineacademy.fo.collection.StrictMap;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.game.model.Arena;

public final class SetupManager {

	private final StrictMap<Player, Arena> editedArenas = new StrictMap<>();

	public boolean isArenaEdited(Arena arena) {
		return editedArenas.containsValue(arena);
	}

	public Player getEditorOf(Arena arena) {
		for (final Entry<Player, Arena> e : editedArenas.entrySet())
			if (e.getValue().equals(arena))
				return e.getKey();

		throw new FoException("Arena " + arena.getName() + " is not being edited");
	}

	public Arena getEditedArena(Player player) {
		return editedArenas.get(player);
	}

	public void addEditedArena(Player player, Arena arena) {
		Valid.checkNotNull(player, "Player == null");
		Valid.checkBoolean(!isArenaEdited(arena), "Arena " + arena.getName() + " is already being edited!");
		Valid.checkBoolean(getEditedArena(player) == null, "The player " + player.getName() + " is already editing an area!");

		editedArenas.put(player, arena);
		arena.getSetup().onEnterEditMode(player);
	}

	public void removeEditedArena(Arena arena) {
		Valid.checkBoolean(editedArenas.containsValue(arena), "Arena " + arena.getName() + " is not being edited!");
		final Player editor = getEditorOf(arena);

		arena.getSetup().onLeaveEditMode(editor);
		editedArenas.removeByValue(arena);
	}

	public StrictList<Arena> getEditedArenas() {
		return new StrictList<>(Collections.unmodifiableCollection(editedArenas.values()));
	}
}
