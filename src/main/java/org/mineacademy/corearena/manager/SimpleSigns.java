package org.mineacademy.corearena.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.model.ArenaSign;
import org.mineacademy.corearena.model.ArenaSign.SignType;
import org.mineacademy.corearena.model.ArenaSigns;
import org.mineacademy.fo.Valid;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimpleSigns implements ArenaSigns {

	private final Map<SignType, List<ArenaSign>> signs;

	@Override
	public final ArenaSign getSignAt(Location loc) {
		for (final List<ArenaSign> signs : this.signs.values())
			for (final ArenaSign sign : signs)
				if (Valid.locationEquals(sign.getLocation(), loc))
					return sign;

		return null;
	}

	@Override
	public final List<ArenaSign> getSigns(SignType type) {
		return this.signs.computeIfAbsent(type, b -> new ArrayList<>());
	}

	@Override
	public final void updateSigns(SignType type, Arena arena) {
		if (this.signs.containsKey(type))
			for (final ArenaSign sign : this.signs.get(type))
				sign.updateState();
	}
}
