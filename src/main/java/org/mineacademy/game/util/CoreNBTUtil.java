package org.mineacademy.game.util;

import static org.mineacademy.game.util.Constants.NBT.KA_NBT;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.SerializeUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.remain.nbt.NBTCompound;
import org.mineacademy.fo.remain.nbt.NBTItem;
import org.mineacademy.game.impl.SimpleSpawnPointMonster;
import org.mineacademy.game.impl.arena.FeatureArena;

public class CoreNBTUtil {

	// -----------------------------------------------------------------------------
	// Read
	// -----------------------------------------------------------------------------

	public static final SimpleSpawnPointMonster readSpawner(ItemStack item) {
		if (item == null || item.getType() == Material.AIR)
			return null;

		final NBTItem nbt = new NBTItem(item);

		if (nbt.hasKey(KA_NBT)) {
			final Location location = SerializeUtil.deserializeLocation(nbt.getCompound(KA_NBT).getString("spawnerLocation"));

			if (location != null) {
				final FeatureArena arena = (FeatureArena) CoreArenaPlugin.getArenaManager().findArena(location);
				final Object maybePoint = arena.getData().findSpawnPoint(location);

				if (maybePoint instanceof SimpleSpawnPointMonster)
					return (SimpleSpawnPointMonster) maybePoint;
			}
		}

		return null;
	}

	// -----------------------------------------------------------------------------
	// Write
	// -----------------------------------------------------------------------------

	public static final ItemStack writeSpawner(SimpleSpawnPointMonster point, ItemStack item) {
		Valid.checkNotNull(point, "Spawnpoint = null");
		Valid.checkNotNull(item, "Stack = null");

		final NBTItem nbt = new NBTItem(item);
		final NBTCompound tag = nbt.addCompound(KA_NBT);

		tag.setObject("spawnerLocation", SerializeUtil.serializeLoc(point.getLocation()));

		return nbt.getItem();
	}
}