package org.mineacademy.game.hook;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.boss.api.Boss;
import org.mineacademy.boss.api.BossAPI;
import org.mineacademy.boss.api.BossSpawnReason;
import org.mineacademy.boss.api.SpawnedBoss;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.debug.Debugger;
import org.mineacademy.fo.remain.CompMaterial;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.EggManager;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;

public class CoreHookManager {

	private static MythicMobsHook mythicMobs;

	private static BossHook boss;

	private CoreHookManager() {
	}

	public static void loadCoreDependencies() {
		if (Common.doesPluginExist("MythicMobs"))
			mythicMobs = new MythicMobsHook();

		if (Common.doesPluginExist("Boss"))
			boss = new BossHook();
	}

	public static boolean isMythicMobsLoaded() {
		return mythicMobs != null;
	}

	public static boolean isBossLoaded() {
		return boss != null;
	}

	// ------------------ delegate methods, reason it's here = prevent errors when class loads but plugin is missing

	public static boolean tryMythicalSpawn(ItemStack i, Location location) {
		return isMythicMobsLoaded() ? mythicMobs.tryMythicalSpawn(i, location) : false;
	}

	public static LivingEntity tryBOSSSpawn(ItemStack i, Location location) {
		return isBossLoaded() ? boss.tryBossSpawn(i, location) : null;
	}

	public static String getBossName(Entity entity) {
		String name = null;

		if (isBossLoaded())
			name = boss.getBossName(entity);

		if (isMythicMobsLoaded() && name == null)
			name = mythicMobs.getBossName(entity);

		return name;
	}
}

class MythicMobsHook {

	final boolean tryMythicalSpawn(ItemStack i, Location location) {
		final boolean valid = i != null && CompMaterial.isMonsterEgg(i.getType()) && i.hasItemMeta() && i.getItemMeta().hasLore();

		if (valid && i.getItemMeta().getLore().get(0).equals(ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "A Mythical Egg that can")) {
			final MythicMob mm = EggManager.getMythicMobFromEgg(i.getItemMeta().getLore().get(2));

			if (mm != null) {
				MythicMobs.inst().getMobManager().spawnMob(mm.getInternalName(), location.clone().add(0, 1, 0));
				return true;
			}
		}

		return false;
	}

	final String getBossName(Entity entity) {
		try {
			final Optional<ActiveMob> opt = MythicMobs.inst().getMobManager().getActiveMob(entity.getUniqueId());
			final ActiveMob mob = opt != null && opt.isPresent() ? opt.get() : null;

			return mob != null ? mob.getEntity().getName() : null;
		} catch (final NoSuchElementException ex) {
			return null;
		}
	}
}

class BossHook {

	final LivingEntity tryBossSpawn(ItemStack i, Location location) {
		Debugger.debug("spawning", "Trying to spawn boss at " + Common.shortLocation(location));

		if (i == null || !CompMaterial.isMonsterEgg(i.getType())) {
			Debugger.debug("spawning", "Spawn for boss at " + Common.shortLocation(location) + " failed. ");

			return null;
		}

		final Boss boss = BossAPI.getBoss(i);
		final SpawnedBoss spawned = boss != null ? boss.spawn(location, BossSpawnReason.CUSTOM) : null;

		Debugger.debug("spawning", "Spawning boss at " + Common.shortLocation(location));

		return spawned != null ? spawned.getEntity() : null;
	}

	final String getBossName(Entity entity) {
		final Boss boss = BossAPI.getBoss(entity);

		return boss != null ? boss.getName() : null;
	}
}