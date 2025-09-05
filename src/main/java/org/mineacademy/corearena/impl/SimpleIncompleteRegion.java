package org.mineacademy.corearena.impl;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.mineacademy.corearena.model.ArenaRegion;
import org.mineacademy.fo.SerializeUtil;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.exception.FoException;

import lombok.Getter;
import lombok.NonNull;

@Getter
public final class SimpleIncompleteRegion implements ArenaRegion {

	private final Location primary;
	private final Location secondary;

	public SimpleIncompleteRegion(Location primary, Location secondary) {
		this.primary = primary != null && primary.getWorld() != null ? primary : null;
		this.secondary = secondary != null && secondary.getWorld() != null ? secondary : null;
	}

	@Override
	public List<Entity> getEntities() {
		throw new FoException("Region incomplete");
	}

	@Override
	public List<Block> getBlocks() {
		throw new FoException("Region incomplete");
	}

	@Override
	public World getWorld() {
		World w;

		if ((w = this.lookupWorld(this.primary)) != null)
			return w;

		if ((w = this.lookupWorld(this.secondary)) != null)
			return w;

		return null;
	}

	private World lookupWorld(Location l) {
		return l != null && l.getWorld() != null ? l.getWorld() : null;
	}

	@Override
	public boolean isWithin(@NonNull Location loc) {
		return false;
	}

	@Override
	public SerializedMap serialize() {
		return new SerializedMap();
	}

	/*@Override
	public boolean isComplete() {
		return false;
	}*/

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{" + (this.primary != null ? SerializeUtil.serializeLocation(this.primary) : "null") + " - " + (this.secondary != null ? SerializeUtil.serializeLocation(this.secondary) : "null") + "}";
	}
}