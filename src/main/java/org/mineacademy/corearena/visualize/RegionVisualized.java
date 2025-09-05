package org.mineacademy.corearena.visualize;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.mineacademy.fo.BlockUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.model.SimpleRunnable;
import org.mineacademy.fo.model.Task;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.region.Region;
import org.mineacademy.fo.remain.CompParticle;

/**
 *  @deprecated use classes in the new "visual" package
 */
@Deprecated
public final class RegionVisualized extends Region {

	private final static CompParticle DEFAULT_PARTICLE = CompParticle.VILLAGER_HAPPY;

	/**
	 * The list of all particles spawned
	 */
	private final List<Location> particles = new ArrayList<>();

	/**
	 * The task that keeps particles visible by re-rendering them.
	 */
	private Task particleTask;

	public RegionVisualized(final String name, final Location primary, final Location secondary) {
		super(name, primary, secondary);
	}

	public void show(final int durationTicks) {
		this.show(durationTicks, DEFAULT_PARTICLE);
	}

	/**
	 * Start visualizing this region for the given amount of ticks
	 *
	 * @param durationTicks
	 * @param particle
	 */
	public void show(final int durationTicks, final CompParticle particle) {
		this.start(particle);

		Platform.runTask(durationTicks, this::stop);
	}

	private void start(final CompParticle particle) {
		if (this.particleTask != null)
			this.particleTask.cancel();

		this.particles.clear();
		this.particles.addAll(BlockUtil.getBoundingBox(this.getPrimary(), this.getSecondary()));

		this.particleTask = Platform.runTaskTimer(23, new SimpleRunnable() {
			@Override
			public void run() {
				if (RegionVisualized.this.particles.isEmpty()) {
					this.cancel();

					return;
				}

				for (final Location loc : RegionVisualized.this.particles)
					particle.spawn(loc);
			}
		});

	}

	public void restart(final int durationTicks) {
		this.restart(durationTicks, DEFAULT_PARTICLE);
	}

	public void restart(final int durationTicks, final CompParticle particle) {
		this.stop();

		this.show(durationTicks, particle);
	}

	/**
	 * Stops visualization
	 */
	public void stop() {
		this.particles.clear();
	}

	/**
	 * Converts a saved map from your yaml/json file into a region if it contains Primary and Secondary keys
	 *
	 * @param map
	 * @return
	 */
	public static RegionVisualized deserialize(final SerializedMap map) {
		Valid.checkBoolean(map.containsKey("Primary") && map.containsKey("Secondary"), "The region must have Primary and a Secondary location");

		final String name = map.getString("Name");
		final Location prim = map.get("Primary", Location.class);
		final Location sec = map.get("Secondary", Location.class);

		return new RegionVisualized(name, prim, sec);
	}
}
