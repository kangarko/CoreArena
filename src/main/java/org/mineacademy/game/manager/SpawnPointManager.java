package org.mineacademy.game.manager;

import java.util.List;

import org.bukkit.entity.Player;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.model.RandomNoRepeatPicker;
import org.mineacademy.game.model.ArenaData;
import org.mineacademy.game.model.SpawnPoint;
import org.mineacademy.game.type.SpawnPointType;

import lombok.Getter;

public final class SpawnPointManager {

	private final RandomSpawnPointPicker randomPicker = new RandomSpawnPointPicker();

	@Getter
	private final ArenaData data;

	public SpawnPointManager(ArenaData data) {
		this.data = data;
	}

	public SpawnPoint getRandomSpawnPoint(Player pl, SpawnPointType type) {
		final List<SpawnPoint> list = data.getSpawnPoints(type);
		Valid.checkNotEmpty(list, "Spawn points must be set!");

		return randomPicker.pickFromFor(list, pl);
	}
}

class RandomSpawnPointPicker extends RandomNoRepeatPicker<SpawnPoint> {

	@Override
	protected boolean canObtain(Player pl, SpawnPoint picked) {
		return true;
	}
}
