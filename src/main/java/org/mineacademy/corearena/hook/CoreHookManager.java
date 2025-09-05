package org.mineacademy.corearena.hook;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.boss.model.Boss;
import org.mineacademy.boss.model.BossSpawnReason;
import org.mineacademy.boss.model.BossSpawnResult;
import org.mineacademy.boss.model.SpawnedBoss;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.SerializeUtil;
import org.mineacademy.fo.debug.Debugger;
import org.mineacademy.fo.model.HookManager;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.remain.CompMaterial;

import io.lumine.mythic.api.adapters.AbstractLocation;

public class CoreHookManager {

	private static MythicMobsHook mythicMobs;

	private static BossHook boss;

	private CoreHookManager() {
	}

	public static void loadCoreDependencies() {
		if (Platform.isPluginInstalled("MythicMobs"))
			mythicMobs = new MythicMobsHook();

		if (Platform.isPluginInstalled("Boss"))
			boss = new BossHook();
	}

	// ------------------ delegate methods, reason it's here = prevent errors when class loads but plugin is missing

	public static boolean tryMythicalSpawn(ItemStack item, Location location) {
		return HookManager.isMythicMobsLoaded() ? mythicMobs.tryMythicalSpawn(item, location) : false;
	}

	public static LivingEntity tryBOSSSpawn(ItemStack item, Location location) {
		return HookManager.isBossLoaded() ? boss.tryBossSpawn(item, location) : null;
	}

	public static String getBossName(Entity entity) {
		String name = null;

		if (HookManager.isBossLoaded())
			name = HookManager.getBossName(entity);

		if (HookManager.isMythicMobsLoaded() && name == null)
			name = HookManager.getMythicMobName(entity);

		return name;
	}
}

class MythicMobsHook {

	final boolean tryMythicalSpawn(ItemStack item, Location location) {

		if (item == null || !item.hasItemMeta())
			return false;

		Debugger.debug("spawning", "Trying to spawn MythicMob at " + SerializeUtil.serializeLocation(location) + " from " + item);
		final io.lumine.mythic.api.mobs.MythicMob mythicMob;

		try {
			mythicMob = getMythicMobFromEgg(item);

		} catch (NoSuchMethodError | NoClassDefFoundError err) {
			Common.warning("MythicMob integration failed, check if your MythicMobs is on the latest version and if it is, nag the authors of " + Platform.getPlugin().getName() + " to update.");
			err.printStackTrace();

			return false;
		}

		if (mythicMob != null) {
			mythicMob.spawn(new AbstractLocation(io.lumine.mythic.bukkit.utils.serialize.Position.of(location.clone().add(0, 1, 0))), 1);

			return true;
		}

		Debugger.debug("spawning", "Spawning MythicMob at " + SerializeUtil.serializeLocation(location) + " failed. ");
		return false;
	}

	public io.lumine.mythic.api.mobs.MythicMob getMythicMobFromEgg(final ItemStack eggItem) {
		final io.lumine.mythic.core.utils.jnbt.CompoundTag eggTag = io.lumine.mythic.bukkit.MythicBukkit.inst().getVolatileCodeHandler().getItemHandler().getNBTData(eggItem);

		if (eggTag.containsKey("MYTHIC_EGG")) {
			final String mmType = eggTag.getString("MYTHIC_EGG");
			return io.lumine.mythic.bukkit.MythicBukkit.inst().getMobManager().getMythicMob(mmType).orElseGet(() -> null);
		}

		return null;
	}
}

class BossHook {

	final LivingEntity tryBossSpawn(ItemStack item, Location location) {
		Debugger.debug("spawning", "Trying to spawn Boss at " + SerializeUtil.serializeLocation(location));

		if (item == null || !CompMaterial.isMonsterEgg(item.getType())) {
			Debugger.debug("spawning", "Spawn for boss at " + SerializeUtil.serializeLocation(location) + " failed. ");

			return null;
		}

		final Boss boss = Boss.findBoss(item);
		final org.mineacademy.boss.lib.model.Tuple<BossSpawnResult, SpawnedBoss> spawned = boss != null ? boss.spawn(location, BossSpawnReason.CUSTOM) : null;

		if (boss != null)
			Debugger.debug("spawning", "Spawning boss at " + SerializeUtil.serializeLocation(location));

		return spawned != null && spawned.getKey() == BossSpawnResult.SUCCESS ? spawned.getValue().getEntity() : null;
	}
}