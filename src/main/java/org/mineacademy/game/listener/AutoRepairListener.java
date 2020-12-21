package org.mineacademy.game.listener;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.game.model.Arena;

public class AutoRepairListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onBowShoot(EntityShootBowEvent e) {
		if (e.getEntityType() == EntityType.PLAYER) {
			final Player pl = (Player) e.getEntity();
			final Arena arena = CoreArenaPlugin.getArenaManager().findArena(pl);

			if (arena != null) {
				final ItemStack bow = e.getBow();

				if (!arena.getSettings().getRepairBlacklist().isAllowed(bow.getType()))
					bow.setDurability((short) 0);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onBlockBreak(BlockBreakEvent e) {
		final Player pl = e.getPlayer();
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(pl);

		if (arena != null)
			repairHandAndOffHand(arena, pl);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onDamage(EntityDamageEvent e) {
		if (e.getEntity() != null && e.getEntity() instanceof Player) {
			final Player player = (Player) e.getEntity();
			final Arena arena = CoreArenaPlugin.getArenaManager().findArena(player);

			if (arena != null) {
				repairHandAndOffHand(arena, player);
				repairArmor(arena, player);
			}
		}
	}

	private final void repairHandAndOffHand(Arena arena, Player player) {
		final ItemStack item = player.getItemInHand();

		if (arena.getSettings().getRepairBlacklist().isAllowed(item.getType()))
			return;

		item.setDurability((short) 0);

		try {
			player.getEquipment().getItemInOffHand().setDurability((short) 0);
		} catch (final Throwable t) {
		}
	}

	private final void repairArmor(Arena arena, Player player) {
		for (final ItemStack armor : player.getEquipment().getArmorContents())
			if (armor != null && !arena.getSettings().getRepairBlacklist().isAllowed(armor.getType()))
				armor.setDurability((short) 0);
	}
}
