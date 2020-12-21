package org.mineacademy.game.impl;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.mineacademy.fo.BlockUtil;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.game.model.ArenaRegion;

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
		return BlockUtil.getBlocks(primary, secondary);
	}

	@Override
	public List<Entity> getEntities() {
		final List<Entity> found = new LinkedList<>();

		final int xMin = (int) primary.getX() >> 4;
		final int xMax = (int) secondary.getX() >> 4;
		final int zMin = (int) primary.getZ() >> 4;
		final int zMax = (int) secondary.getZ() >> 4;

		for (int cx = xMin; cx <= xMax; ++cx)
			for (int cz = zMin; cz <= zMax; ++cz)
				for (final Entity en : getWorld().getChunkAt(cx, cz).getEntities()) {
					final Location l;
					if (en.isValid() && (l = en.getLocation()) != null && isWithin(l))
						found.add(en);
				}

		return found;
	}

	@Override
	public boolean isWithin(@NonNull Location loc) {
		if (!loc.getWorld().equals(primary.getWorld()))
			return false;

		final int x = (int) loc.getX();
		final int y = (int) loc.getY();
		final int z = (int) loc.getZ();

		return x >= primary.getX() && x <= secondary.getX()
				&& y >= primary.getY() && y <= secondary.getY()
				&& z >= primary.getZ() && z <= secondary.getZ();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{" + Common.shortLocation(primary) + " - " + Common.shortLocation(secondary) + "}";
	}

	@Override
	public SerializedMap serialize() {
		final SerializedMap map = new SerializedMap();

		map.put("Primary", primary);
		map.put("Secondary", secondary);

		return map;
	}

	public static SimpleCuboidRegion deserialize(SerializedMap map) {
		Valid.checkBoolean(map.containsKey("Primary") && map.containsKey("Secondary"), "The region must have Primary and a Secondary location");

		final Location prim = map.getLocation("Primary");
		final Location sec = map.getLocation("Secondary");

		return new SimpleCuboidRegion(prim, sec);
	}
}