package org.mineacademy.game.impl;

import org.bukkit.Location;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.game.model.SpawnPoint;
import org.mineacademy.game.type.SpawnPointType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class SimpleSpawnPoint implements SpawnPoint {

	private final SpawnPointType type;

	@Setter
	private Location location;

	@Override
	public SerializedMap serialize() {
		final SerializedMap map = new SerializedMap();

		map.put("location", location);

		return map;
	}

	public static final SimpleSpawnPoint deserialize(SerializedMap map, SpawnPointType type) {
		switch (type) {
			case MONSTER:
				return SimpleSpawnPointMonster.deserialize(map);

			case PLAYER:
				return SimpleSpawnPointPlayer.deserialize(map);

			default:
				throw new FoException("Unhandled loading spawnpoint " + type);
		}
	}

	@Override
	public String toString() {
		return type + "_SpawnPoint" + "{" + (getLocation() != null ? Common.shortLocation(getLocation()) : "null") + "}";
	}
}
