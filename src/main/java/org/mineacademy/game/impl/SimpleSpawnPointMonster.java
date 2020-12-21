package org.mineacademy.game.impl;

import java.util.List;

import org.bukkit.Location;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.SerializeUtil;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.game.model.ActivePeriod;
import org.mineacademy.game.model.ActivePeriod.ActiveMode;
import org.mineacademy.game.type.SpawnPointType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class SimpleSpawnPointMonster extends SimpleSpawnPoint {

	private SpawnedEntity[] spawnedTypes;

	private ActivePeriod activePeriod = new ActivePeriod(ActiveMode.FROM, 1);

	private int minimumPlayers = 1;
	private int chance = 100;
	private boolean spawnAllSlots = false;

	public SimpleSpawnPointMonster(Location location) {
		super(SpawnPointType.MONSTER, location);
	}

	@Override
	public SerializedMap serialize() {
		final SerializedMap map = super.serialize();

		map.put("activePeriod", activePeriod);
		map.put("minPlayers", minimumPlayers);
		map.put("chance", chance);
		map.put("spawnAllSlots", spawnAllSlots);
		map.putIfExist("spawns", spawnedTypes);

		return map;
	}

	@Override
	public String toString() {
		final String chanceF = chance == 100 ? "" : chance + "% chance, ";
		final String minPlayersF = minimumPlayers == 1 ? "" : minimumPlayers + " min. players, ";
		final String spawnedTypesF = spawnedTypes == null ? "spawns nothing" : Common.join(spawnedTypes, ", ", SpawnedEntity::format);

		return "Spawner{" + activePeriod.formatPeriod() + " phase, " + chanceF + minPlayersF + spawnedTypesF + "}";
	}

	public static SimpleSpawnPointMonster deserialize(SerializedMap map) {
		final Location loc = map.getLocation("location");
		final SimpleSpawnPointMonster spawn = new SimpleSpawnPointMonster(loc);

		{
			final List<SpawnedEntity> list = SerializeUtil.deserializeMapList(map.get("spawns", Object.class), SpawnedEntity.class);

			if (list != null)
				spawn.spawnedTypes = list.toArray(new SpawnedEntity[list.size()]);
		}

		spawn.activePeriod = ActivePeriod.deserialize(map.containsKey("activePeriod") ? map.getMap("activePeriod") : new SerializedMap());
		spawn.minimumPlayers = map.getInteger("minPlayers");

		if (map.containsKey("chance"))
			spawn.chance = map.getInteger("chance");

		if (map.containsKey("spawnAllSlots"))
			spawn.spawnAllSlots = map.getBoolean("spawnAllSlots");

		return spawn;
	}

}