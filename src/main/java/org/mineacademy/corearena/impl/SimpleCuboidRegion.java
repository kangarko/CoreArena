package org.mineacademy.corearena.impl;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.mineacademy.corearena.model.ArenaRegion;
import org.mineacademy.fo.BlockUtil;
import org.mineacademy.fo.SerializeUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;

import lombok.Getter;
import lombok.NonNull;

@Getter
public final class SimpleCuboidRegion implements ArenaRegion {

	private final Location primary;
	private final Location secondary;

	public SimpleCuboidRegion(@NonNull Location primary, @NonNull Location secondary) {
		Valid.checkNotNull(primary.getWorld(), "Primary location lacks a world!");
		Valid.checkNotNull(secondary.getWorld(), "Primary location lacks a world!");

		Valid.checkBoolean(primary.getWorld().getName().equals(secondary.getWorld().getName()), "Points must be in one world!");

		// make this easy on us: primary = lowest value, secondary = highest value
		final int x1 = primary.getBlockX(), x2 = secondary.getBlockX(),
				y1 = primary.getBlockY(), y2 = secondary.getBlockY(),
				z1 = primary.getBlockZ(), z2 = secondary.getBlockZ();
		// these methods aren't always available..
		//this.primary = primary.set(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2));
		//this.secondary = secondary.set(Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));\

		primary.setX(Math.min(x1, x2));
		primary.setY(Math.min(y1, y2));
		primary.setZ(Math.min(z1, z2));

		secondary.setX(Math.max(x1, x2));
		secondary.setY(Math.max(y1, y2));
		secondary.setZ(Math.max(z1, z2));

		this.primary = primary;
		this.secondary = secondary;
	}

	@Override
	public List<Block> getBlocks() {
		return BlockUtil.getBlocks(this.primary, this.secondary);
	}

	@Override
	public List<Entity> getEntities() {
		final List<Entity> found = new LinkedList<>();

		final int xMin = (int) this.primary.getX() >> 4;
		final int xMax = (int) this.secondary.getX() >> 4;
		final int zMin = (int) this.primary.getZ() >> 4;
		final int zMax = (int) this.secondary.getZ() >> 4;

		for (int cx = xMin; cx <= xMax; ++cx)
			for (int cz = zMin; cz <= zMax; ++cz)
				for (final Entity en : this.getWorld().getChunkAt(cx, cz).getEntities()) {
					final Location l;

					if (en == null)
						continue;

					if (en.isValid() && (l = en.getLocation()) != null && this.isWithin(l))
						found.add(en);
				}

		return found;
	}

	@Override
	public boolean isWithin(@NonNull Location loc) {
		if (!loc.getWorld().equals(this.primary.getWorld()))
			return false;

		final int x = (int) loc.getX();
		final int y = (int) loc.getY();
		final int z = (int) loc.getZ();

		return x >= this.primary.getX() && x <= this.secondary.getX()
				&& y >= this.primary.getY() && y <= this.secondary.getY()
				&& z >= this.primary.getZ() && z <= this.secondary.getZ();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{" + SerializeUtil.serializeLocation(this.primary) + " - " + SerializeUtil.serializeLocation(this.secondary) + "}";
	}

	@Override
	public SerializedMap serialize() {
		final SerializedMap map = new SerializedMap();

		map.put("Primary", this.primary);
		map.put("Secondary", this.secondary);

		return map;
	}

	public static SimpleCuboidRegion deserialize(SerializedMap map) {
		Valid.checkBoolean(map.containsKey("Primary") && map.containsKey("Secondary"), "The region must have Primary and a Secondary location");

		final Location prim = map.get("Primary", Location.class);
		final Location sec = map.get("Secondary", Location.class);

		return new SimpleCuboidRegion(prim, sec);
	}
}