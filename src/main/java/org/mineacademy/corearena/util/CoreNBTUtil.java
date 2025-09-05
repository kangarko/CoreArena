package org.mineacademy.corearena.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.impl.SimpleSpawnPointMonster;
import org.mineacademy.corearena.impl.arena.FeatureArena;
import org.mineacademy.fo.SerializeUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.remain.nbt.NBTItem;

public class CoreNBTUtil {

	// -----------------------------------------------------------------------------
	// Read
	// -----------------------------------------------------------------------------

	public static final SimpleSpawnPointMonster findSpawnPoint(Location location) {
		if (location == null)
			return null;

		final FeatureArena arena = (FeatureArena) CoreArenaPlugin.getArenaManager().findArena(location);
		final Object maybePoint = arena.getData().findSpawnPoint(location);

		return maybePoint instanceof SimpleSpawnPointMonster ? (SimpleSpawnPointMonster) maybePoint : null;
	}

	public static final Location readSpawnPointLocation(ItemStack item) {
		if (item == null || item.getType() == Material.AIR)
			return null;

		final NBTItem nbt = new NBTItem(item);

		return nbt.hasKey("CloneSpawnerToolOn") ? SerializeUtil.deserializeLocation(nbt.getString("CloneSpawnerToolOn")) : null;
	}

	// -----------------------------------------------------------------------------
	// Write
	// -----------------------------------------------------------------------------

	public static final ItemStack writeSpawner(SimpleSpawnPointMonster point, ItemStack item) {
		Valid.checkNotNull(point, "Spawnpoint = null");
		Valid.checkNotNull(item, "Stack = null");

		final NBTItem nbt = new NBTItem(item);
		nbt.setString("CloneSpawnerToolOn", SerializeUtil.serializeLocation(point.getLocation()));

		return nbt.getItem();
	}
}