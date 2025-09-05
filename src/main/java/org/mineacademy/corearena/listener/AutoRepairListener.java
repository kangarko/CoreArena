package org.mineacademy.corearena.listener;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.fo.remain.CompMaterial;

public final class AutoRepairListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBowShoot(EntityShootBowEvent event) {
		if (event.getEntityType() == EntityType.PLAYER) {
			final Player player = (Player) event.getEntity();
			final Arena arena = CoreArenaPlugin.getArenaManager().findArena(player);

			if (arena != null) {
				final ItemStack bow = event.getBow();

				if (this.canRepairItem(bow, arena))
					bow.setDurability((short) 0);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		final Player player = event.getPlayer();
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(player);

		if (arena != null)
			this.repairHandAndOffHand(player, arena);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onDamage(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
			final Player victimPlayer = (Player) event.getEntity();
			final Arena victimArena = CoreArenaPlugin.getArenaManager().findArena(victimPlayer);

			if (victimArena != null) {
				this.repairHandAndOffHand(victimPlayer, victimArena);
				this.repairArmor(victimPlayer, victimArena);
			}
		}

		if (event.getDamager() instanceof Player) {
			final Player damagerPlayer = (Player) event.getDamager();
			final Arena damagerArena = CoreArenaPlugin.getArenaManager().findArena(damagerPlayer);

			if (damagerArena != null) {
				this.repairHandAndOffHand(damagerPlayer, damagerArena);
				this.repairArmor(damagerPlayer, damagerArena);
			}
		}
	}

	private void repairHandAndOffHand(Player player, Arena arena) {
		final ItemStack hand = player.getItemInHand();

		if (this.canRepairItem(hand, arena))
			hand.setDurability((short) 0);

		try {
			final ItemStack offhand = player.getEquipment().getItemInOffHand();

			if (this.canRepairItem(offhand, arena))
				offhand.setDurability((short) 0);

		} catch (final Throwable t) {
		}
	}

	private void repairArmor(Player player, Arena arena) {
		for (final ItemStack armor : player.getEquipment().getArmorContents())
			if (this.canRepairItem(armor, arena))
				armor.setDurability((short) 0);
	}

	private boolean canRepairItem(ItemStack item, Arena arena) {
		if (item == null || CompMaterial.isAir(item))
			return false;

		return item.getDurability() > 0 && !arena.getSettings().getRepairBlacklist().isAllowed(item.getType());
	}
}
