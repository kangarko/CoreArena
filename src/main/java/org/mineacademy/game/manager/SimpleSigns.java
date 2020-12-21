package org.mineacademy.game.manager;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.StrictMap;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.model.ArenaSign;
import org.mineacademy.game.model.ArenaSigns;
import org.mineacademy.game.model.ArenaSign.SignType;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimpleSigns implements ArenaSigns {

	private final StrictMap<SignType, List<ArenaSign>> signs;

	@Override
	public final ArenaSign getSignAt(Location loc) {
		for (final List<ArenaSign> signs : signs.values())
			for (final ArenaSign sign : signs)
				if (Valid.locationEquals(sign.getLocation(), loc))
					return sign;

		return null;
	}

	@Override
	public final List<ArenaSign> getSigns(SignType type) {
		return signs.getOrPut(type, new ArrayList<>());
	}

	@Override
	public final void updateSigns(SignType type, Arena arena) {
		if (signs.contains(type))
			for (final ArenaSign sign : signs.get(type))
				sign.updateState();
	}
}
