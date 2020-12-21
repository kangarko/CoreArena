package org.mineacademy.game.impl;

import org.bukkit.Location;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.game.type.SpawnPointType;

public final class SimpleSpawnPointPlayer extends SimpleSpawnPoint {

	public SimpleSpawnPointPlayer(Location location) {
		super(SpawnPointType.PLAYER, location);
	}

	public static SimpleSpawnPoint deserialize(SerializedMap map) {
		final Location loc = map.getLocation("location");

		return new SimpleSpawnPointPlayer(loc);
	}
}