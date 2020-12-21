package org.mineacademy.game.tool;

import java.util.List;

import org.mineacademy.game.model.SpawnPoint;
import org.mineacademy.game.type.SpawnPointType;
import org.mineacademy.game.visualize.VisualizeMode;

abstract class SpawnSelector extends Selector {

	protected abstract SpawnPointType getType();

	@Override
	protected final void renderExistingBlocks() {
		if (getType() != null) {
			final List<SpawnPoint> points = getData().getSpawnPoints(getType());

			for (final SpawnPoint point : points)
				getVisualizer().show(point.getLocation(), VisualizeMode.MASK);
		}
	}

	@Override
	protected final void unrenderExistingBlocks() {
		if (getType() != null) {
			final List<SpawnPoint> points = getData().getSpawnPoints(getType());

			for (final SpawnPoint point : points)
				getVisualizer().hide(point.getLocation());
		}
	}
}
