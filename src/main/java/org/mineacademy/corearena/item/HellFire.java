package org.mineacademy.corearena.item;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.model.ArenaSnapshotProcedural.DamagedStage;
import org.mineacademy.corearena.type.ArenaState;
import org.mineacademy.corearena.util.CoreUtil;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.RandomUtil;
import org.mineacademy.fo.TimeUtil;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.tool.Tool;
import org.mineacademy.fo.model.SimpleRunnable;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.platform.BukkitPlugin;
import org.mineacademy.fo.remain.CompEnchantment;
import org.mineacademy.fo.remain.CompItemFlag;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompParticle;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.fo.settings.Lang;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public final class HellFire extends Tool {

	@Getter
	private static final HellFire instance = new HellFire();

	private HellFire() {
	}

	private final Map<UUID, Long> lastUsageMap = new HashMap<>();

	private long getLastUseAgo(Player player) {
		if (!this.lastUsageMap.containsKey(player.getUniqueId())) {
			this.lastUsageMap.put(player.getUniqueId(), TimeUtil.getCurrentTimeSeconds());

			return Integer.MAX_VALUE;
		}

		return TimeUtil.getCurrentTimeSeconds() - this.lastUsageMap.get(player.getUniqueId());
	}

	private void updateUse(Player player) {
		this.lastUsageMap.put(player.getUniqueId(), TimeUtil.getCurrentTimeSeconds());
	}

	@Override
	public ItemStack getItem() {
		return ItemCreator.fromMaterial(CompMaterial.LEVER)
				.name("&CCall Hell-Fire")
				.enchant(CompEnchantment.DURABILITY, 1)
				.flags(CompItemFlag.HIDE_ENCHANTS)
				.lore("",
						"&fLeft click &7a block",
						"&7to call a Hell-File!")
				.make();
	}

	private void operator(Player player, String message) {
		Common.tellTimed(1, player, "&8[&5Operator&8] &7" + message);
	}

	@Override
	public void onBlockClick(PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(player);

		if (arena == null || CoreArenaPlugin.getArenaManager().findArena(player).getState() != ArenaState.RUNNING) {
			event.setCancelled(true);

			return;
		}

		if (!event.hasBlock() || event.getAction() != Action.LEFT_CLICK_BLOCK) {
			this.operator(player, "&7Left-click a block to send me the coordinates!");

			return;
		}

		event.setCancelled(true);

		if (!CoreUtil.isWithinArena(player, event.getClickedBlock().getLocation()))
			return;

		if (this.getLastUseAgo(player) < 10) {
			this.operator(player, "&6Reloading missiles... Ready in " + Lang.numberFormat("case-second", 10 - this.getLastUseAgo(player)) + "!");
			return;
		}

		final Block up = event.getClickedBlock().getRelative(BlockFace.UP);
		if (up.getType() != Material.AIR)
			return;

		final Location initialLoc = up.getLocation().add(0.5, 0, 0.5);
		this.operator(player, "&a&lCalled a hell fire to " + up.getX() + " " + up.getY() + " " + up.getZ() + "!");

		Remain.takeItemAndSetAsHand(player, player.getItemInHand());
		this.updateUse(player);

		Platform.runTask(40, () -> {

			this.operator(player, "&c&lHell fire incoming!");
			CompSound.ENTITY_FIREWORK_ROCKET_LAUNCH.play(player, 1F, 0.1F);

			final Location loc = up.getLocation();

			// Spawn in the air
			loc.setY(256);

			final int explodeY = up.getWorld().getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ());

			final LargeFireball rocket = up.getWorld().spawn(loc, LargeFireball.class);

			rocket.setDirection(new Vector(0D, -0.0001D, 0D).normalize());
			rocket.setYield(0F);
			rocket.setIsIncendiary(false);

			final long firedSince = TimeUtil.getCurrentTimeSeconds();

			// Effects + explosion
			new SimpleRunnable() {

				@Override
				public void run() {
					rocket.setVelocity(new Vector(0, -1F, 0));

					// If alive for over 20 seconds, remove
					if (TimeUtil.getCurrentTimeSeconds() - firedSince > 20) {
						this.cancel();

						return;
					}

					final Location location = rocket.getLocation();

					// Hit some ground
					if (location.getY() - explodeY < 3) {
						final boolean canDamage = arena.getSnapshot().isSaved(DamagedStage.INITIAL);
						location.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), 10F, canDamage, canDamage);

						new SoundPlayer(location).start();

						for (final Entity nearby : Remain.getNearbyEntities(location, 8))
							if (nearby instanceof LivingEntity && !(nearby instanceof Player))
								((LivingEntity) nearby).damage(100);

						for (int i = 0; i < 20; i++)
							CompParticle.EXPLOSION_NORMAL.spawn(location.clone().add(
									Math.random() * (RandomUtil.nextBoolean() ? -5 : 5),
									1,
									Math.random() * (RandomUtil.nextBoolean() ? -5 : 5)));

						rocket.remove();
						this.cancel();

						return;
					}

					// Invalidated
					if (rocket.isDead() || !rocket.isValid()) {
						this.cancel();

						return;
					}

					// Just fire particle
					CompParticle.FLAME.spawn(location);
					CompParticle.BLOCK_CRACK.spawn(initialLoc, CompMaterial.DIRT);
				}

			}.runTaskTimer(BukkitPlugin.getInstance(), 0, 1);
		});
	}

	@RequiredArgsConstructor
	class SoundPlayer extends Thread {

		private final Location l;

		@Override
		public void run() {
			CompSound.BLOCK_ANVIL_LAND.play(this.l, 1F, 0.1F);

			double count = 1;

			final int radius = 2;

			for (double y = -5; y <= 10; y += 0.05) {
				final double x = radius * Math.cos(y) * Math.random() * (count = count + 0.05);
				final double z = radius * Math.sin(y) * Math.random() * count;

				try {
					CompParticle.BLOCK_CRACK.spawn(this.l.clone().add(x, y, z), CompMaterial.STONE);
				} catch (final Throwable t) {
					t.printStackTrace();

					break;
				}

				Common.sleep(4);
			}

			CompSound.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR.play(this.l, 1F, 0.1F);
		}
	}

	@Override
	public boolean ignoreCancelled() {
		return false;
	}
}
