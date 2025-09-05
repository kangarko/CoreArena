package org.mineacademy.corearena.tool;

import java.util.List;

import org.mineacademy.corearena.model.SpawnPoint;
import org.mineacademy.corearena.type.SpawnPointType;
import org.mineacademy.corearena.visualize.VisualizeMode;

abstract class SpawnpointTool extends SelectorTool {

	protected abstract SpawnPointType getType();

	@Override
	protected final void renderExistingBlocks() {
		if (this.getType() != null) {
			final List<SpawnPoint> points = this.getData().getSpawnPoints(this.getType());

			for (final SpawnPoint point : points)
				this.getVisualizer().show(point.getLocation(), VisualizeMode.MASK);
		}
	}

	@Override
	protected final void unrenderExistingBlocks() {
		if (this.getType() != null) {
			final List<SpawnPoint> points = this.getData().getSpawnPoints(this.getType());

			for (final SpawnPoint point : points)
				this.getVisualizer().hide(point.getLocation());
		}
	}
}
