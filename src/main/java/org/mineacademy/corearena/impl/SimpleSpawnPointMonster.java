package org.mineacademy.corearena.impl;

import java.util.List;

import org.bukkit.Location;
import org.mineacademy.corearena.model.ActivePeriod;
import org.mineacademy.corearena.model.ActivePeriod.ActiveMode;
import org.mineacademy.corearena.type.SpawnPointType;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.collection.SerializedMap;

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
	public SimpleSpawnPointMonster clone() {
		final SimpleSpawnPointMonster clone = new SimpleSpawnPointMonster(this.getLocation().clone());

		clone.spawnedTypes = new SpawnedEntity[this.spawnedTypes.length];

		for (int i = 0; i < this.spawnedTypes.length; i++)
			if (this.spawnedTypes[i] != null)
				clone.spawnedTypes[i] = this.spawnedTypes[i].clone();

		clone.activePeriod = new ActivePeriod(this.activePeriod.getMode(), this.activePeriod.getStartLimit(), this.activePeriod.getEndLimit());
		clone.minimumPlayers = this.minimumPlayers;
		clone.chance = this.chance;
		clone.spawnAllSlots = this.spawnAllSlots;

		return clone;
	}

	@Override
	public SerializedMap serialize() {
		final SerializedMap map = super.serialize();

		map.put("activePeriod", this.activePeriod);
		map.put("minPlayers", this.minimumPlayers);
		map.put("chance", this.chance);
		map.put("spawnAllSlots", this.spawnAllSlots);
		map.putIfExists("spawns", this.spawnedTypes);

		return map;
	}

	@Override
	public String toString() {
		final String chanceF = this.chance == 100 ? "" : this.chance + "% chance, ";
		final String minPlayersF = this.minimumPlayers == 1 ? "" : this.minimumPlayers + " min. players, ";
		final String spawnedTypesF = this.spawnedTypes == null ? "spawns nothing" : Common.join(this.spawnedTypes, SpawnedEntity::format);

		return "Spawner{" + this.activePeriod.formatPeriod() + " phase, " + chanceF + minPlayersF + spawnedTypesF + "}";
	}

	public static SimpleSpawnPointMonster deserialize(SerializedMap map) {
		final Location loc = map.get("location", Location.class);
		final SimpleSpawnPointMonster spawn = new SimpleSpawnPointMonster(loc);

		{
			final List<SpawnedEntity> list = map.getList("spawns", SpawnedEntity.class);

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