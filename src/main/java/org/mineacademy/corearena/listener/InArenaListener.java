package org.mineacademy.corearena.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.data.AllData.ArenaPlayer;
import org.mineacademy.corearena.exception.CancelledException;
import org.mineacademy.corearena.exception.EventHandledException;
import org.mineacademy.corearena.exp.ExpItemHandler;
import org.mineacademy.corearena.item.ExplosiveArrow;
import org.mineacademy.corearena.item.ExplosiveBow;
import org.mineacademy.corearena.manager.SimpleArenaManager;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.model.ArenaSnapshot;
import org.mineacademy.corearena.model.ArenaSnapshotProcedural;
import org.mineacademy.corearena.model.ArenaSnapshotProcedural.DamagedStage;
import org.mineacademy.corearena.model.ExpItem;
import org.mineacademy.corearena.physics.PhysicalEngine;
import org.mineacademy.corearena.settings.Settings;
import org.mineacademy.corearena.tool.CloneSpawnerToolOff;
import org.mineacademy.corearena.tool.CloneSpawnerToolOn;
import org.mineacademy.corearena.type.ArenaState;
import org.mineacademy.corearena.type.DeathCause;
import org.mineacademy.corearena.type.LeaveCause;
import org.mineacademy.corearena.util.Constants;
import org.mineacademy.corearena.util.CoreUtil;
import org.mineacademy.corearena.util.Permissions;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.ItemUtil;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.SerializeUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.command.SimpleCommandGroup;
import org.mineacademy.fo.event.RocketExplosionEvent;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.model.CompChatColor;
import org.mineacademy.fo.model.SimpleRunnable;
import org.mineacademy.fo.model.Variables;
import org.mineacademy.fo.platform.BukkitPlugin;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompParticle;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.fo.settings.Lang;

public class InArenaListener implements Listener {

	// Serialized Location, Vector
	static final Map<String, Vector> storedExplosions = new HashMap<>();

	private final Map<UUID, Player> playerDamagers = new HashMap<>();

	public InArenaListener() {

		if (Settings.ProceduralDamage.ENABLED)
			try {
				Class.forName("org.bukkit.event.block.BlockExplodeEvent");
				Platform.registerEvents(new EventsProceduralDamage18());

			} catch (final ClassNotFoundException err) {
				Common.log("&cProcedural damage is only available for Minecraft 1.8 and later. Disabling..");
			}

		try {
			Class.forName("org.bukkit.event.entity.SpawnerSpawnEvent");
			Platform.registerEvents(new SpawnerSpawnComp());

		} catch (final ClassNotFoundException ex) {
		}

		try {
			Class.forName("org.bukkit.event.entity.EntityPickupItemEvent");
			Platform.registerEvents(new EntityPickupComp());

		} catch (final ClassNotFoundException ex) {
		}
	}

	//
	// Format chat and prevent commands
	//

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onChat(AsyncPlayerChatEvent event) {
		if (!Settings.Arena.Chat.ENABLED)
			return;

		final Player player = event.getPlayer();
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(player);
		String message = event.getMessage();

		if (arena == null)
			return;

		String format;
		boolean cancelEvent = false;

		if (!Settings.Arena.Chat.RANGED || message.startsWith("!") && !message.equals("!") && player.hasPermission(Permissions.Chat.GLOBAL)) {
			if (Valid.isNullOrEmpty(Settings.Arena.Chat.GLOBAL_FORMAT))
				return;

			message = message.startsWith("!") ? message.substring(1) : message;
			format = this.formatArenaChat(arena, player, Settings.Arena.Chat.GLOBAL_FORMAT);

		} else {
			if (Valid.isNullOrEmpty(Settings.Arena.Chat.FORMAT))
				return;

			format = this.formatArenaChat(arena, player, Settings.Arena.Chat.FORMAT);

			cancelEvent = true;
		}

		event.setFormat(CompChatColor.translateColorCodes(format));
		event.setMessage(message);

		if (cancelEvent) {
			event.setCancelled(true);

			for (final Player recipient : arena.getPlayers())
				recipient.sendMessage(String.format(event.getFormat(), player.getName(), message));
		}
	}

	private final String formatArenaChat(Arena arena, Player player, String message) {
		return Variables.builder().audience(player).placeholderArray(
				"arena", arena.getName(),
				"operatorColor", player.hasPermission(Permissions.Chat.RED_COLOR) ? "&c" : "&7",
				"message", Constants.Symbols.CHAT_EVENT_MESSAGE).replaceLegacy(message);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onCommand(PlayerCommandPreprocessEvent event) {
		final Player player = event.getPlayer();
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(player);
		final String command = event.getMessage();

		if (!CoreArenaPlugin.DEBUG_EDITING_MODE && arena != null) {
			final SimpleCommandGroup mainCommand = BukkitPlugin.getInstance().getDefaultCommandGroup();

			// Has bypass perm
			if (player.hasPermission(Permissions.Bypass.ARENA_COMMANDS))
				return;

			// Enable plugin commands
			if (command.startsWith(mainCommand.getLabel()) || Valid.isInListStartsWith(command, mainCommand.getAliases()))
				return;

			if (!Valid.isInListStartsWith(command, Settings.Arena.ALLOWED_COMMANDS)) {
				event.setCancelled(true);

				Common.tell(player, Lang.component("command-disallowed-while-playing"));
			}
		}
	}

	//
	// Handle quitting players or joining inside the arena
	//

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onEntityTeleported(EntityTeleportEvent e) {
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(e.getFrom());
		final Entity en = e.getEntity();

		if (arena != null && en instanceof Tameable && ((Tameable) en).isTamed())
			e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onPlayerTeleported(PlayerTeleportEvent event) {
		final Player player = event.getPlayer();
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(player);

		if (arena != null) {
			final Arena newArena = CoreArenaPlugin.getArenaManager().findArena(event.getTo());
			final ArenaPlayer data = CoreArenaPlugin.getDataFor(player);

			if (newArena == null || !newArena.equals(arena))
				Platform.runTask(1, () -> {
					if (data.hasArenaCache() && !data.getArenaCache().pendingRemoval && !data.getArenaCache().pendingJoining)
						if (arena.getState() == ArenaState.RUNNING)
							arena.kickPlayer(player, LeaveCause.ESCAPED);
				});
		}
	}

	//
	// Forward events to arena and or cancel
	//
	@EventHandler(priority = EventPriority.NORMAL)
	public final void onPlayerDeath(PlayerDeathEvent event) {
		final Player player = event.getEntity();
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(player);

		if (arena != null) {
			final Player killer = this.playerDamagers.remove(player.getUniqueId());

			if (arena.getSettings().allowOwnEquipment() && Settings.Arena.KEEP_OWN_EQUIPMENT)
				event.setKeepInventory(true);

			if (Settings.Arena.HIDE_DEATH_MESSAGES)
				event.setDeathMessage(null);

			// Always keep level because we store Nuggets in our cache anyways
			event.setKeepLevel(true);

			try {
				if (killer != null && arena.isJoined(killer) && !killer.getName().equals(player.getName()))
					arena.onPlayerDeath(player, killer);
				else
					arena.onPlayerDeath(player, DeathCause.DIED);

			} catch (final EventHandledException ex) {
				// exit gracefully
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public final void onRespawn(PlayerRespawnEvent e) {
		final Player player = e.getPlayer();
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(player);

		if (arena != null)
			try {
				arena.onPlayerRespawn(e);
			} catch (final EventHandledException ex) {
			}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public final void onPlayerInteractLobby(PlayerInteractEvent event) {
		final Player player = event.getPlayer();

		// Fix bug in older Spigot versions where the event is called while browsing GUI
		if (player.hasMetadata(Menu.TAG_MENU_CURRENT))
			return;

		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(player);

		if (arena != null && arena.getState() == ArenaState.LOBBY)
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public final void onPlayerInteractTwo(PlayerInteractEvent event) {
		final boolean valid = event.isCancelled() && event.getAction().toString().contains("_AIR");

		if (!valid)
			return;

		final Player player = event.getPlayer();

		// Fix bug in older Spigot versions where the event is called while browsing GUI
		if (player.hasMetadata(Menu.TAG_MENU_CURRENT))
			return;

		final SimpleArenaManager manager = CoreArenaPlugin.getArenaManager();
		final Arena arena = manager.findArena(player);

		if (arena != null && arena.getState() == ArenaState.RUNNING)
			try {
				arena.onPlayerClickAir(player, player.getItemInHand());

			} catch (final CancelledException ex) {
				event.setCancelled(true);
			}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.hasBlock())
			return;

		final Player player = event.getPlayer();

		// Fix bug in older Spigot versions where the event is called while browsing GUI
		if (player.hasMetadata(Menu.TAG_MENU_CURRENT))
			return;

		if (ItemUtil.isSimilar(player.getItemInHand(), CloneSpawnerToolOn.getInstance().getItem()) || ItemUtil.isSimilar(player.getItemInHand(), CloneSpawnerToolOff.getInstance().getItem()))
			return;

		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(event.getClickedBlock().getLocation());

		if (arena == null)
			return;

		if (Remain.isInteractEventPrimaryHand(event)) {
			if (arena.getState() == ArenaState.RUNNING && arena.isJoined(player)) {
				if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
					try {
						arena.onPlayerClick(player, event.getClickedBlock(), player.getItemInHand());

					} catch (final CancelledException ex) {
						event.setCancelled(true);
					}

				return;
			}

			if (CoreArenaPlugin.getSetupManager().isArenaEdited(arena)) {
				try {
					arena.getSetup().onSetupClick(player, event.getAction(), event.getClickedBlock());

				} catch (final CancelledException ex) {
					event.setCancelled(true);
				}

				return;
			}

			if (arena.getState() == ArenaState.STOPPED)
				this.notifyCannotEdit(player);
		}

		if (arena.getState() == ArenaState.RUNNING && event.getAction() == Action.PHYSICAL) {
			final Block block = event.getClickedBlock();

			if (block.getType() == CompMaterial.FARMLAND.getMaterial()) {
				event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
				event.setCancelled(true);
			}
		}

		if (arena.getState() != ArenaState.STOPPED)
			return;

		if (!event.hasBlock())
			event.setCancelled(true);

		final Block block = event.getClickedBlock();
		if (block.getType() == Material.TRIPWIRE)
			return;

		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onPlayerEat(PlayerItemConsumeEvent event) {
		final Player player = event.getPlayer();
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(player);

		if (arena != null && arena.getState() != ArenaState.RUNNING)
			event.setCancelled(true);
	}

	@EventHandler
	public final void onHealthRegen(EntityRegainHealthEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;

		final Player player = (Player) event.getEntity();
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(player);

		if (arena != null && arena.getState() == ArenaState.RUNNING)
			try {
				arena.onHealthRegen(event, player);

			} catch (final CancelledException ex) {
				event.setCancelled(true);
			}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onItemPickup(PlayerPickupItemEvent e) {
		final Item item = e.getItem();

		if (item == null || item.getItemStack() == null)
			return;

		final Player player = e.getPlayer();
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(player);

		if (arena != null)
			if (arena.getState() == ArenaState.RUNNING) {
				if (ExpItemHandler.isExpItem(item)) {
					final ExpItem tag = ExpItemHandler.getExpItem(item);

					try {
						arena.onPlayerPickupTag(e, tag);
					} catch (final EventHandledException ex) {
					}
				}

			} else {
				if (CoreArenaPlugin.getSetupManager().isArenaEdited(arena))
					return;

				e.setCancelled(true);
				item.remove();
			}
	}

	//
	// Cancel events in arena
	//

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onItemSpawn(ItemSpawnEvent e) {
		if (e.getEntity() == null || e.getEntity().getItemStack() == null || e.getEntity().getItemStack().getType() == Material.AIR)
			return;

		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(e.getLocation());

		if (arena != null) {
			if (arena.getState() == ArenaState.RUNNING || CoreArenaPlugin.getSetupManager().isArenaEdited(arena))
				return;

			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onDamage(EntityDamageEvent event) {
		final Entity entity = event.getEntity();

		if (entity instanceof Player) {
			final Player player = (Player) entity;
			final Arena arena = CoreArenaPlugin.getArenaManager().findArena(player);

			if (arena != null)
				this.playerDamagers.put(player.getUniqueId(), null);

		} else if (entity instanceof Item && ExpItemHandler.isExpItem(entity))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public final void onPlayerDamage(EntityDamageByEntityEvent event) {
		final Entity victim = event.getEntity();
		final Entity damager = event.getDamager();
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(victim.getLocation());

		if (arena != null)
			if (arena.getState() != ArenaState.STOPPED) {
				final double finalDmg = Remain.getFinalDamage(event);

				if (arena.getState() == ArenaState.RUNNING) {

					Player plDamager = null;

					if (damager instanceof Projectile) {
						final ProjectileSource source = ((Projectile) damager).getShooter();

						if (source instanceof Player)
							plDamager = (Player) source;

					} else if (damager instanceof Player)
						plDamager = (Player) damager;

					try {
						// is the damager in the game?
						if (plDamager != null && !arena.isJoined(plDamager))
							event.setCancelled(true);
						else if (victim instanceof Player) {
							final Player pvictim = (Player) victim;

							try {
								if (plDamager != null)
									arena.onPlayerPvP(event, plDamager, pvictim, finalDmg);
								else
									arena.onPlayerDamage(event, pvictim, damager, finalDmg);
							} catch (final EventHandledException ex) {
							}

							if (!event.isCancelled())
								this.playerDamagers.put(pvictim.getUniqueId(), plDamager);

						} else if (victim instanceof LivingEntity && plDamager != null)
							arena.onPlayerPvE(plDamager, (LivingEntity) victim, finalDmg);

					} catch (final EventHandledException ex) {
					}
				} else
					event.setCancelled(true);

			} else {
				if (CoreArenaPlugin.getSetupManager().isArenaEdited(arena))
					return;

				event.setCancelled(true);

				if (damager instanceof Player)
					this.notifyCannotEdit((Player) damager);
			}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPotionSplashEvent(PotionSplashEvent event) {
		final ProjectileSource projectileSource = event.getEntity().getShooter();

		if (!(projectileSource instanceof Player))
			return;

		for (final Entity entity : event.getAffectedEntities()) {
			if (!(entity instanceof Player))
				continue;

			final Player player = (Player) entity;
			final Arena arena = CoreArenaPlugin.getArenaManager().findArena(player);

			if (arena != null)
				entity.setMetadata("KaGame_potionKiller", new FixedMetadataValue(CoreArenaPlugin.getInstance(), player));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPotionDamage(EntityDamageEvent event) {
		final DamageCause cause = event.getCause();
		final Entity entity = event.getEntity();

		if (!(entity instanceof Player) || !entity.hasMetadata("KaGame_potionKiller"))
			return;

		if (cause == DamageCause.POISON || cause == DamageCause.MAGIC || cause == DamageCause.WITHER)
			this.playerDamagers.put(entity.getUniqueId(), (Player) entity.getMetadata("KaGame_potionKiller").get(0).value());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public final void onPlayerDamageByBlock(EntityDamageByBlockEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;

		final Player damaged = (Player) event.getEntity();
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(damaged.getLocation());

		if (arena != null)
			if (arena.getState() != ArenaState.STOPPED) {
				final double finalDmg = Remain.getFinalDamage(event);

				if (arena.getState() == ArenaState.RUNNING)
					try {
						this.playerDamagers.put(damaged.getUniqueId(), null);
						arena.onPlayerBlockDamage(event, damaged, finalDmg);

					} catch (final EventHandledException ex) {
					}
				else
					event.setCancelled(true);

			} else {
				if (CoreArenaPlugin.getSetupManager().isArenaEdited(arena))
					return;

				event.setCancelled(true);
				this.notifyCannotEdit(damaged);
			}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity() == null)
			return;

		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(event.getEntity().getLocation());

		if (arena != null)
			if (arena.getState() == ArenaState.RUNNING && !CoreArenaPlugin.getSetupManager().isArenaEdited(arena))
				try {
					arena.onEntityDeath(event);
				} catch (final EventHandledException ex) {
				}
			else {
				event.getDrops().clear();
				event.setDroppedExp(0);
			}
	}

	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		if (!(event.getEntity() instanceof FallingBlock)) {
			final Entity entity = event.getEntity();
			final Arena arena = CoreArenaPlugin.getArenaManager().findArena(entity.getLocation());

			if (arena != null && (arena.getState() != ArenaState.RUNNING || !arena.getSettings().hasProceduralDamage()))
				event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityFormBlock(EntityBlockFormEvent event) {
		if (!(event.getEntity() instanceof FallingBlock)) {
			final Entity entity = event.getEntity();
			final Arena arena = CoreArenaPlugin.getArenaManager().findArena(entity.getLocation());

			if (arena != null && (arena.getState() != ArenaState.RUNNING || !arena.getSettings().hasProceduralDamage()))
				event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onEntityTarget(EntityTargetEvent event) {
		if (event.getEntity() == null)
			return;

		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(event.getEntity().getLocation());

		if (arena != null)
			if (arena.getState() == ArenaState.RUNNING && !CoreArenaPlugin.getSetupManager().isArenaEdited(arena))
				try {
					arena.onEntityTarget(event);
				} catch (final EventHandledException ex) {
				}
			else
				event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public final void onProjectileHit(ProjectileHitEvent e) {
		if (e.getEntity() == null)
			return;

		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(e.getEntity().getLocation());

		if (arena != null)
			if (arena.getState() == ArenaState.RUNNING && !CoreArenaPlugin.getSetupManager().isArenaEdited(arena))
				try {
					arena.onProjectileHit(e);
				} catch (final EventHandledException ex) {
				}
			else
				e.getEntity().remove();
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public final void onProjectileLaunch(ProjectileLaunchEvent e) {
		if (e.getEntity() == null)
			return;

		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(e.getEntity().getLocation());

		if (arena != null)
			if (arena.getState() == ArenaState.RUNNING && !CoreArenaPlugin.getSetupManager().isArenaEdited(arena))
				try {
					arena.onProjectileLaunch(e);
				} catch (final EventHandledException ex) {
				}
			else
				e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onBlockSpread(BlockSpreadEvent event) {
		if (event.getSource() != null)
			this.cancelIfInStoppedArena(event.getSource().getLocation(), event);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onEntityInteract(EntityInteractEvent e) {
		if (e.getEntity() != null)
			this.cancelIfNotEditedAndNotifyIfNotPlaying(e.getEntity().getLocation(), null, e);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onCreatureSpawn(CreatureSpawnEvent event) {
		if (event.getLocation() == null)
			return;

		final LivingEntity en = event.getEntity();
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(event.getLocation());
		final List<String> banned = Arrays.asList("NATURAL", "CHUNK_GEN", "NETHER_PORTAL", "VILLAGE_DEFENSE", "VILLAGE_INVASION");

		if (arena == null)
			return;
		if (banned.contains(event.getSpawnReason().toString()))
			event.setCancelled(true);
		else if (arena.getSetup().isEdited() && event.getSpawnReason() == SpawnReason.SPAWNER_EGG)
			this.modifyToNotCombust(arena, en);
		else if (arena.getState() != ArenaState.RUNNING) {
			//Armor stands should be allowed to be spawned
			if (event.getEntity().getType().toString().equals("ARMOR_STAND"))
				return;
			event.setCancelled(true);
		} else {
			arena.onEntitySpawn(event);

			if (!en.isDead())
				this.modifyToNotCombust(arena, en);
		}
	}

	private void modifyToNotCombust(Arena arena, LivingEntity entity) {
		if (!(entity instanceof Monster))
			return;

		// If monsters should not burn under sunlight, add an invisible button to their
		// head
		if (!arena.getSettings().allowMonstersBurn()) {
			final EntityEquipment equipment = entity.getEquipment();

			if (equipment.getHelmet() == null || equipment.getHelmet() != null && equipment.getHelmet().getType() == Material.AIR) {
				equipment.setHelmet(new ItemStack(Material.STONE_BUTTON));

				equipment.setHelmetDropChance(0F);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public final void onExplosionPrime(ExplosionPrimeEvent e) {
		if (e.getEntity() == null)
			return;

		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(e.getEntity().getLocation());

		if (arena != null)
			if (arena.getState() != ArenaState.RUNNING) {

				// Enable explosions in edit mode
				if (!arena.getSetup().isEdited())
					e.setCancelled(true);

			} else if (e.getEntity() instanceof TNTPrimed)
				e.setRadius(Settings.ProceduralDamage.Explosions.POWER_TNT);
			else if (e.getEntity() instanceof Creeper)
				e.setRadius(Settings.ProceduralDamage.Explosions.POWER_CREEPER);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public final void onEntityExplode(EntityExplodeEvent e) {
		if (e.getEntity() != null) {
			final Arena arena = CoreArenaPlugin.getArenaManager().findArena(e.getLocation());

			if (arena != null)
				if (arena.getState() == ArenaState.RUNNING) {
					boolean glass = false;
					final ArenaSnapshot snap = arena.getSnapshot();

					if (snap instanceof ArenaSnapshotProcedural && snap.isSaved(DamagedStage.INITIAL) && snap.isSaved(DamagedStage.DAMAGED)) {
						final List<Block> toRemove = new ArrayList<>();

						for (final Block oldBlock : e.blockList()) {
							final Arena otherArena = CoreArenaPlugin.getArenaManager().findArena(oldBlock.getLocation());

							if (otherArena == null || !arena.getName().equals(otherArena.getName())) {
								toRemove.add(oldBlock);

								continue;
							}

							final double radius = Settings.ProceduralDamage.Explosions.DAMAGE_RADIUS;

							for (final Entity en : Remain.getNearbyEntities(oldBlock.getLocation(), radius))
								if (en instanceof Monster) {
									CoreArenaPlugin.getArenaManager().findArena(oldBlock.getLocation());

									final LivingEntity l = (LivingEntity) en;
									final double newHP = MathUtil.range(Remain.getHealth(l) - Settings.ProceduralDamage.Explosions.DAMAGE_DAMAGE, 0, Remain.getMaxHealth(l));

									l.setHealth(newHP);
								}

							final BlockState oldBlockState = oldBlock.getState();

							if (oldBlock.getType().toString().contains("GLASS"))
								glass = true;

							final Block damagedBlock = ((ArenaSnapshotProcedural) snap).restoreBlock(oldBlock, DamagedStage.DAMAGED);

							if (damagedBlock == null || damagedBlock.getType() != oldBlockState.getType()) {
								PhysicalEngine.pushAway(arena, oldBlockState, Vector.getRandom());
								PhysicalEngine.applyGravitation(arena, oldBlock, Settings.ProceduralDamage.Explosions.GRAVITATION_RADIUS_CHECK, true, 4);
							}
						}

						e.blockList().removeAll(toRemove);
					}

					if (glass)
						CompSound.BLOCK_GLASS_BREAK.play(e.getEntity().getLocation(), 1F, 0.1F);

					e.blockList().clear();

					/*e.setYield(0);

					boolean glass = false;
					final ArenaSnapshot snap = arena.getSnapshot();

					if (snap instanceof ArenaSnapshotProcedural && snap.isSaved(DamagedStage.INITIAL) && snap.isSaved(DamagedStage.DAMAGED))
						for (final Block block : e.blockList()) {
							final Material oldMaterial = block.getState().getType();

							if (block.getType().toString().contains("GLASS"))
								glass = true;

							final Block damaged = ((ArenaSnapshotProcedural) snap).restoreBlock(block, DamagedStage.DAMAGED);

							if (damaged != null && damaged.getType() != oldMaterial)
								BlockPhysics.applyGravitation(arena, block, Settings.ProceduralDamage.Explosions.GRAVITATION_RADIUS_CHECK + 1, true, 4);
						}

					if (glass)
						CompSound.BLOCK_GLASS_BREAK.play(e.getLocation(), 1F, 0.1F);

					e.blockList().clear();*/

				} else
					e.setCancelled(true);
		}
	}

	@EventHandler
	public final void onRocketExplosion(RocketExplosionEvent e) {
		this.cancelIfInStoppedArena(e.getProjectile().getLocation(), e);

		if (!e.isCancelled())
			storedExplosions.put(SerializeUtil.serializeLocation(e.getProjectile().getLocation()), e.getProjectile().getVelocity());
	}

	private final void cancelIfInStoppedArena(Location loc, Cancellable e) {
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(loc);

		if (arena != null && arena.getState() != ArenaState.RUNNING)
			e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onInvClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player))
			return;

		final Arena arena = CoreArenaPlugin.getArenaManager().findArena((Player) e.getWhoClicked());

		if (arena != null && arena.getState() == ArenaState.LOBBY) {
			final Menu menu = Menu.getMenu((Player) e.getWhoClicked());

			if (menu == null)
				e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onItemDrop(PlayerDropItemEvent e) {
		if (e.getItemDrop() == null || e.getItemDrop().getLocation() == null)
			return;

		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(e.getItemDrop().getLocation());

		if (arena != null) {
			if (arena.getState() == ArenaState.RUNNING || CoreArenaPlugin.getSetupManager().isArenaEdited(arena))
				return;

			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onToggleFly(PlayerToggleFlightEvent e) {
		if (CoreArenaPlugin.DEBUG_EDITING_MODE)
			return;

		final boolean blocked = this.cancelIfPlaying(e.getPlayer(), e);

		if (blocked)
			e.getPlayer().setAllowFlight(false);
	}

	private final boolean cancelIfPlaying(Player player, Cancellable event) {
		if (CoreArenaPlugin.getArenaManager().isPlaying(player)) {
			event.setCancelled(true);

			return true;
		}

		return false;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onHangingBreak(HangingBreakByEntityEvent event) {
		if (event.getRemover() != null && event.getRemover() instanceof Player)
			this.cancelIfNotEditedAndNotifyIfNotPlaying(event.getEntity().getLocation(), (Player) event.getRemover(), event);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onBlockPlace(BlockPlaceEvent event) {
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(event.getBlock().getLocation());
		final Player player = event.getPlayer();

		if (arena != null && !CoreArenaPlugin.getSetupManager().isArenaEdited(arena))
			if (player != null && arena.getState() == ArenaState.STOPPED)
				this.notifyCannotEdit(player);
			else
				try {
					arena.onPlayerBlockPlace(event);

				} catch (final CancelledException ex) {
					event.setCancelled(true);
				}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onBlockBreak(BlockBreakEvent e) {
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(e.getBlock().getLocation());
		final Player pl = e.getPlayer();

		if (arena != null && !CoreArenaPlugin.getSetupManager().isArenaEdited(arena))
			if (pl != null && arena.getState() == ArenaState.STOPPED)
				this.notifyCannotEdit(pl);
			else
				arena.onPlayerBlockBreak(e);
	}

	private final void cancelIfNotEditedAndNotifyIfNotPlaying(Location loc, Player pl, Cancellable e) {
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(loc);

		if (arena != null && !CoreArenaPlugin.getSetupManager().isArenaEdited(arena)) {
			if (pl != null && arena.getState() == ArenaState.STOPPED)
				this.notifyCannotEdit(pl);

			e.setCancelled(true);
		}
	}

	// ------------------------------------------------------------------------
	// Utility
	// ------------------------------------------------------------------------

	private void notifyCannotEdit(Player player) {
		if (CoreUtil.checkPerm(player, Permissions.Commands.EDIT, false))
			Common.tell(player, Lang.component("arena-setup-cannot-edit"));
	}
}

class EventsProceduralDamage18 implements Listener {

	private final HashSet<String> undamagedPlayers = new HashSet<>();

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		final Projectile projectile = event.getEntity();

		if (projectile != null && projectile.getShooter() instanceof Player) {
			final Arena arena = CoreArenaPlugin.getArenaManager().findArena(projectile.getLocation());
			final Player player;

			if (arena == null)
				return;

			if (arena.getState() != ArenaState.RUNNING || !arena.isJoined(player = (Player) projectile.getShooter())) {
				event.setCancelled(true);

				return;
			}

			if (!ItemUtil.isSimilar(player.getItemInHand(), ExplosiveBow.getItem()))
				return;

			final PlayerInventory inv = player.getInventory();
			boolean found = false;
			ItemStack toTake = null;

			for (final ItemStack is : inv.getContents())
				if (is != null && ItemUtil.isSimilar(is, ExplosiveArrow.getItem())) {
					toTake = is;
					found = true;

					break;
				}

			if (found) {
				final ItemStack toTakeFinal = toTake;

				projectile.setMetadata("CoreBow", new FixedMetadataValue(CoreArenaPlugin.getInstance(), "true"));

				CompSound.ENTITY_FIREWORK_ROCKET_BLAST.play(player, 1, (float) Math.random());

				// Set amount
				Platform.runTask(() -> {
					Valid.checkNotNull(toTakeFinal);

					Remain.takeItemOnePiece(player, toTakeFinal);
				});

				// Effects
				new SimpleRunnable() {
					final Projectile arrow = event.getEntity();

					@Override
					public void run() {
						if (this.arrow.isDead() || !this.arrow.isValid() || this.arrow.isOnGround())
							this.cancel();
						else
							CompParticle.SPELL_WITCH.spawn(this.arrow.getLocation());
					}
				}.runTaskTimer(CoreArenaPlugin.getInstance(), 0, 1);

			} else {
				event.setCancelled(true);

				CompSound.ENTITY_ITEM_FRAME_BREAK.play(player, 1, (float) Math.random());
				Common.tell(player, Lang.component("arena-game-explosive-arrow-no-ammo"));
			}
		}
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageByBlockEvent event) {
		if (event.getEntityType() == EntityType.PLAYER) {
			final String damagedEntityName = ((Player) event.getEntity()).getName();

			if (event.getCause() == DamageCause.BLOCK_EXPLOSION && this.undamagedPlayers.contains(damagedEntityName)) {
				event.setDamage(0);

				this.undamagedPlayers.remove(damagedEntityName);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onProjectileHit(ProjectileHitEvent event) {
		final Projectile projectile = event.getEntity();

		if (projectile != null && projectile.getShooter() instanceof Player) {
			final Arena arena = CoreArenaPlugin.getArenaManager().findArena(projectile.getLocation());

			if (arena != null && projectile.hasMetadata("CoreBow")) {
				final Player player = (Player) projectile.getShooter();

				if (arena.getPlayers().contains(player)) {
					final String location = this.hashLocation(projectile.getLocation());

					InArenaListener.storedExplosions.put(location, projectile.getVelocity());

					player.getWorld().playEffect(projectile.getLocation(), Effect.SMOKE, 0);

					if (!arena.getSettings().explosiveArrowPlayerDamage()) { // Prevent damage of players
						final double radius = Settings.Items.ExplosiveBow.DAMAGE + Settings.ProceduralDamage.Explosions.DAMAGE_RADIUS;

						for (final Entity nearbyEntity : Remain.getNearbyEntities(projectile.getLocation(), radius))
							if (nearbyEntity instanceof Player)
								this.undamagedPlayers.add(((Player) nearbyEntity).getName());
					}

					player.getWorld().createExplosion(projectile.getLocation(), Settings.Items.ExplosiveBow.DAMAGE);

					projectile.remove();
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onBlockExplode(BlockExplodeEvent event) {
		if (event.getBlock() == null)
			return;

		final String location = this.hashLocation(event.getBlock().getLocation());
		final Vector velocity = InArenaListener.storedExplosions.remove(location);

		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(event.getBlock().getLocation());

		if (velocity == null) {
			if (arena != null && (arena.getState() != ArenaState.RUNNING || !arena.getSettings().hasProceduralDamage()))
				event.setCancelled(true);

			return;
		}

		if (arena != null && arena.getState() == ArenaState.RUNNING) {
			boolean glass = false;

			final ArenaSnapshot clipboard = arena.getSnapshot();

			if (clipboard instanceof ArenaSnapshotProcedural && clipboard.isSaved(DamagedStage.INITIAL) && clipboard.isSaved(DamagedStage.DAMAGED)) {
				final List<Block> toRemove = new ArrayList<>();

				for (final Block oldBlock : event.blockList()) {
					final Arena otherArena = CoreArenaPlugin.getArenaManager().findArena(oldBlock.getLocation());

					if (otherArena == null || !arena.getName().equals(otherArena.getName())) {
						toRemove.add(oldBlock);

						continue;
					}

					final double radius = Settings.ProceduralDamage.Explosions.DAMAGE_RADIUS;

					for (final Entity nearbyEntity : Remain.getNearbyEntities(oldBlock.getLocation(), radius))
						if (nearbyEntity instanceof Monster) {
							CoreArenaPlugin.getArenaManager().findArena(oldBlock.getLocation());

							final LivingEntity livingNearbyEntity = (LivingEntity) nearbyEntity;
							final double newHealth = MathUtil.range(Remain.getHealth(livingNearbyEntity) - Settings.ProceduralDamage.Explosions.DAMAGE_DAMAGE, 0, Remain.getMaxHealth(livingNearbyEntity));

							livingNearbyEntity.setHealth(newHealth);
						}

					final BlockState oldBlockState = oldBlock.getState();

					if (oldBlock.getType().toString().contains("GLASS"))
						glass = true;

					final Block damagedBlock = ((ArenaSnapshotProcedural) clipboard).restoreBlock(oldBlock, DamagedStage.DAMAGED);

					if (damagedBlock == null || damagedBlock.getType() != oldBlockState.getType()) {
						PhysicalEngine.pushAway(arena, oldBlockState, velocity);
						PhysicalEngine.applyGravitation(arena, oldBlock, Settings.ProceduralDamage.Explosions.GRAVITATION_RADIUS_CHECK, true, 4);
					}
				}

				event.blockList().removeAll(toRemove);

			} else
				event.setCancelled(true);

			if (glass)
				CompSound.BLOCK_GLASS_BREAK.play(event.getBlock().getLocation(), 1F, 0.1F);

			event.blockList().clear();
			this.undamagedPlayers.clear();
		}
	}

	private String hashLocation(Location loc) {
		return loc.getWorld().getName() + " " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ();
	}
}

class SpawnerSpawnComp implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onSpawnerSpawn(SpawnerSpawnEvent e) {
		if (e.getEntity() == null)
			return;

		final Entity en = e.getEntity();
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(en.getLocation());

		if (arena != null && arena.getState() != ArenaState.RUNNING)
			e.setCancelled(true);
	}
}

class EntityPickupComp implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onItemPickupEntity(EntityPickupItemEvent event) {
		if (event.getEntityType() != EntityType.PLAYER) {
			if (event.getItem() == null || event.getItem().getItemStack() == null)
				return;

			if (ExpItemHandler.isExpItem(event.getItem()))
				event.setCancelled(true);
		}
	}
}