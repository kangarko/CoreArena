package org.mineacademy.corearena.manager;

import java.util.List;

import org.bukkit.entity.Player;
import org.mineacademy.corearena.model.ArenaData;
import org.mineacademy.corearena.model.SpawnPoint;
import org.mineacademy.corearena.type.SpawnPointType;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.model.RandomNoRepeatPicker;
import org.mineacademy.fo.platform.FoundationPlayer;
import org.mineacademy.fo.platform.Platform;

import lombok.Getter;

public final class SpawnPointManager {

	private final RandomSpawnPointPicker randomPicker = new RandomSpawnPointPicker();

	@Getter
	private final ArenaData data;

	public SpawnPointManager(ArenaData data) {
		this.data = data;
	}

	public SpawnPoint getRandomSpawnPoint(Player player, SpawnPointType type) {
		final List<SpawnPoint> list = this.data.getSpawnPoints(type);
		Valid.checkNotEmpty(list, "Spawn points must be set!");

		return this.randomPicker.pickFromFor(list, Platform.toPlayer(player));
	}
}

class RandomSpawnPointPicker extends RandomNoRepeatPicker<SpawnPoint> {

	@Override
	protected boolean canObtain(FoundationPlayer audience, SpawnPoint picked) {
		return true;
	}
}
