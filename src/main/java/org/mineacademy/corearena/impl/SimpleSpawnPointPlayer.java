package org.mineacademy.corearena.impl;

import org.bukkit.Location;
import org.mineacademy.corearena.type.SpawnPointType;
import org.mineacademy.fo.collection.SerializedMap;

public final class SimpleSpawnPointPlayer extends SimpleSpawnPoint {

	public SimpleSpawnPointPlayer(Location location) {
		super(SpawnPointType.PLAYER, location);
	}

	public static SimpleSpawnPoint deserialize(SerializedMap map) {
		final Location loc = map.get("location", Location.class);

		return new SimpleSpawnPointPlayer(loc);
	}
}