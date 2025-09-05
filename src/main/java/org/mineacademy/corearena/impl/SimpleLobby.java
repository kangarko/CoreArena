package org.mineacademy.corearena.impl;

import org.bukkit.Location;
import org.mineacademy.corearena.model.Lobby;

import lombok.NonNull;

public final class SimpleLobby implements Lobby {

	private final Location location;

	public SimpleLobby(@NonNull Location location) {
		this.location = location;
	}

	@Override
	public Location getLocation() {
		return this.location;
	}
}
