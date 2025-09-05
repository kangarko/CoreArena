package org.mineacademy.corearena.impl;

import org.bukkit.Location;
import org.mineacademy.corearena.model.SpawnPoint;
import org.mineacademy.corearena.type.SpawnPointType;
import org.mineacademy.fo.SerializeUtil;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.exception.FoException;

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

		map.put("location", this.location);

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
		return this.type + "_SpawnPoint" + "{" + (this.getLocation() != null ? SerializeUtil.serializeLocation(this.getLocation()) : "null") + "}";
	}
}
