package org.mineacademy.game.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
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
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginManager;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.ItemUtil;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.StrictMap;
import org.mineacademy.fo.event.RocketExplosionEvent;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.model.Variables;
import org.mineacademy.fo.remain.CompParticle;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.game.exception.CancelledException;
import org.mineacademy.game.exception.EventHandledException;
import org.mineacademy.game.exp.ExpItemHandler;
import org.mineacademy.game.impl.ArenaPlayer;
import org.mineacademy.game.impl.SimpleGameCommandGroup;
import org.mineacademy.game.item.ExplosiveArrow;
import org.mineacademy.game.item.ExplosiveBow;
import org.mineacademy.game.manager.SimpleArenaManager;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.model.ArenaSnapshot;
import org.mineacademy.game.model.ArenaSnapshotProcedural;
import org.mineacademy.game.model.ArenaSnapshotProcedural.DamagedStage;
import org.mineacademy.game.model.ExpItem;
import org.mineacademy.game.physics.BlockPhysics;
import org.mineacademy.game.settings.Localization;
import org.mineacademy.game.settings.Settings;
import org.mineacademy.game.tool.CloneSpawnerToolOff;
import org.mineacademy.game.tool.CloneSpawnerToolOn;
import org.mineacademy.game.type.ArenaState;
import org.mineacademy.game.type.DeathCause;
import org.mineacademy.game.type.LeaveCause;
import org.mineacademy.game.util.Constants;
import org.mineacademy.game.util.CoreUtil;
import org.mineacademy.game.util.Permissions;

public class InArenaListener implements Listener {

	// Serialized Location, Vector
	static final StrictMap<String, Vector> storedExplosions = new StrictMap<>();

	private final StrictMap<UUID, Player> playerDamagers = new StrictMap<>();

	public InArenaListener() {
		final PluginManager pm = Bukkit.getPluginManager();

		// Procedural damage
		if (Settings.ProceduralDamage.ENABLED)
			try {
				Class.forName("org.bukkit.event.block.BlockExplodeEvent");
				pm.registerEvents(new EventsProceduralDamage18(), CoreArenaPlugin.getInstance());

			} catch (final ClassNotFoundException err) {
				Common.log("&cProcedural damage is only available for Minecraft 1.8 and later. Disabling..");
			}

		{ // Spawner events
			try {
				Class.forName("org.bukkit.event.entity.SpawnerSpawnEvent");
				pm.registerEvents(new SpawnerSpawnComp(), CoreArenaPlugin.getInstance());

			} catch (final ClassNotFoundException ex) {
			}
		}

		{ // Entity pickup item
			try {
				Class.forName("org.bukkit.event.entity.EntityPickupItemEvent");
				pm.registerEvents(new EntityPickupComp(), CoreArenaPlugin.getInstance());

			} catch (final ClassNotFoundException ex) {
			}
		}
	}

	//
	// Format chat and prevent commands
	//

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChat(AsyncPlayerChatEvent e) {
		if (!Settings.Arena.Chat.ENABLED)
			return;

		final Player pl = e.getPlayer();
		String msg = e.getMessage();
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(pl);

		if (arena == null)
			return;

		String format;

		if (!Settings.Arena.Chat.RANGED || msg.startsWith("!") && !msg.equals("!") && PlayerUtil.hasPerm(pl, Permissions.Chat.GLOBAL)) {
			if (Valid.isNullOrEmpty(Settings.Arena.Chat.GLOBAL_FORMAT))
				return;

			msg = msg.startsWith("!") ? msg.substring(1) : msg;
			format = formatArenaChat(arena, pl, Settings.Arena.Chat.GLOBAL_FORMAT);

		} else {
			if (Valid.isNullOrEmpty(Settings.Arena.Chat.FORMAT))
				return;

			format = formatArenaChat(arena, pl, Settings.Arena.Chat.FORMAT);

			e.getRecipients().clear();
			e.getRecipients().addAll(arena.getPlayers());
		}

		e.setFormat(Common.colorize(format));
		e.setMessage(Common.stripColors(msg));
	}

	private final String formatArenaChat(Arena arena, Player player, String message) {
		message = message
				.replace("{arena}", arena.getName())
				.replace("{operatorColor}", PlayerUtil.hasPerm(player, Permissions.Chat.RED_COLOR) ? "&c" : "&7")
				.replace("{message}", Constants.Symbols.CHAT_EVENT_MESSAGE);

		return Variables.replace(message, player);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onCommand(PlayerCommandPreprocessEvent event) {
		final Player player = event.getPlayer();
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(player);
		final String command = event.getMessage();

		if (!CoreArenaPlugin.DEBUG_EDITING_MODE && arena != null) {
			final SimpleGameCommandGroup mainCommand = CoreArenaPlugin.getInstance().getMainCommand();

			// Has bypass perm
			if (PlayerUtil.hasPerm(player, Permissions.Bypass.ARENA_COMMANDS))
				return;

			// Enable plugin commands
			if (command.startsWith(mainCommand.getLabel()) || Valid.isInListStartsWith(command, mainCommand.getAliases()))
				return;

			if (!Valid.isInListStartsWith(command, Settings.Arena.ALLOWED_COMMANDS)) {
				event.setCancelled(true);

				Common.tell(player, Localization.Commands.DISALLOWED_WHILE_PLAYING);
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
	public final void onPlayerTeleported(PlayerTeleportEvent e) {
		final Player player = e.getPlayer();
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(player);

		if (arena != null) {
			final Arena newArena = CoreArenaPlugin.getArenaManager().findArena(e.getTo());
			final ArenaPlayer data = CoreArenaPlugin.getDataFor(player);

			if (newArena == null || !newArena.equals(arena))
				Common.runLater(1, () -> {
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

	public final void onPlayerDeath(PlayerDeathEvent e) {
		final Player pl = e.getEntity();
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(pl);

		if (arena != null) {
			final Player killer = playerDamagers.removeWeak(pl.getUniqueId());

			if (arena.getSettings().allowOwnEquipment() && Settings.Arena.KEEP_OWN_EQUIPMENT)
				e.setKeepInventory(true);

			if (Settings.Arena.HIDE_DEATH_MESSAGES)
				e.setDeathMessage(null);

			// Always keep level because we store Nuggets in our cache anyways
			e.setKeepLevel(true);

			try {
				if (killer != null && arena.isJoined(killer) && !killer.getName().equals(pl.getName()))
					arena.onPlayerDeath(pl, killer);
				else
					arena.onPlayerDeath(pl, DeathCause.DIED);

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

	@EventHandler(priority = EventPriority.HIGHEST)
	public final void onPlayerInteractTwo(PlayerInteractEvent e) {
		final boolean valid = e.isCancelled() && e.getAction().toString().contains("_AIR");
		if (!valid)
			return;

		final Player player = e.getPlayer();
		final SimpleArenaManager manager = CoreArenaPlugin.getArenaManager();
		final Arena arena = manager.findArena(player);

		if (arena != null && arena.getState() == ArenaState.RUNNING)
			try {
				arena.onPlayerClickAir(player, player.getItemInHand());

			} catch (final CancelledException ex) {
				e.setCancelled(true);
			}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onPlayerInteract(PlayerInteractEvent e) {
		if (!e.hasBlock())
			return;

		final Player player = e.getPlayer();

		if (ItemUtil.isSimilar(player.getItemInHand(), CloneSpawnerToolOn.getInstance().getItem()) || ItemUtil.isSimilar(player.getItemInHand(), CloneSpawnerToolOff.getInstance().getItem()))
			return;

		final SimpleArenaManager manager = CoreArenaPlugin.getArenaManager();
		final Arena arena = manager.findArena(e.getClickedBlock().getLocation());

		if (arena == null)
			return;
		if (Remain.isInteractEventPrimaryHand(e)) {
			if (arena.getState() == ArenaState.RUNNING && arena.isJoined(player)) {

				if (e.getAction() == Action.RIGHT_CLICK_BLOCK)
					try {
						arena.onPlayerClick(player, e.getClickedBlock(), player.getItemInHand());

					} catch (final CancelledException ex) {
						e.setCancelled(true);
					}

				return;
			}

			if (CoreArenaPlugin.getSetupManager().isArenaEdited(arena)) {
				try {
					arena.getSetup().onSetupClick(player, e.getAction(), e.getClickedBlock());

				} catch (final CancelledException ex) {
					e.setCancelled(true);
				}

				return;
			}

			if (arena.getState() == ArenaState.STOPPED)
				notifyCannotEdit(player);
		}

		if (arena.getState() != ArenaState.STOPPED)
			return;

		if (!e.hasBlock())
			e.setCancelled(true);

		final Block block = e.getClickedBlock();
		if (block.getType() == Material.TRIPWIRE)
			return;

		e.setCancelled(true);
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
	public void onDamage(EntityDamageEvent e) {
		final Entity en = e.getEntity();

		if (en instanceof Player) {
			final Player pl = (Player) en;
			final Arena arena = CoreArenaPlugin.getArenaManager().findArena(pl);

			if (arena != null)
				playerDamagers.override(pl.getUniqueId(), null);

		} else if (en instanceof Item && ExpItemHandler.isExpItem(en))
			e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public final void onPlayerDamage(EntityDamageByEntityEvent e) {
		final Entity victim = e.getEntity();
		final Entity damager = e.getDamager();
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(victim.getLocation());

		if (arena != null)
			if (arena.getState() != ArenaState.STOPPED) {
				final double finalDmg = Remain.getFinalDamage(e);

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
							e.setCancelled(true);
						else if (victim instanceof Player) {
							final Player pvictim = (Player) victim;

							try {
								if (plDamager != null)
									arena.onPlayerPvP(e, plDamager, pvictim, finalDmg);
								else
									arena.onPlayerDamage(e, pvictim, damager, finalDmg);
							} catch (final EventHandledException ex) {
							}

							if (!e.isCancelled())
								playerDamagers.override(pvictim.getUniqueId(), plDamager);

						} else if (victim instanceof LivingEntity && plDamager != null)
							arena.onPlayerPvE(plDamager, (LivingEntity) victim, finalDmg);

					} catch (final EventHandledException ex) {
					}
				} else
					e.setCancelled(true);

			} else {
				if (CoreArenaPlugin.getSetupManager().isArenaEdited(arena))
					return;

				e.setCancelled(true);

				if (damager instanceof Player)
					notifyCannotEdit((Player) damager);
			}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPotionSplashEvent(PotionSplashEvent e) {
		final ProjectileSource dm = e.getEntity().getShooter();

		if (!(dm instanceof Player))
			return;

		for (final Entity en : e.getAffectedEntities()) {
			if (!(en instanceof Player))
				continue;

			final Player pl = (Player) en;
			final Arena arena = CoreArenaPlugin.getArenaManager().findArena(pl);

			if (arena != null)
				en.setMetadata("KaGame_potionKiller", new FixedMetadataValue(CoreArenaPlugin.getInstance(), pl));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPotionDamage(EntityDamageEvent e) {
		final DamageCause cause = e.getCause();
		final Entity en = e.getEntity();

		if (!(en instanceof Player) || !en.hasMetadata("KaGame_potionKiller"))
			return;

		if (cause == DamageCause.POISON || cause == DamageCause.MAGIC || cause == DamageCause.WITHER)
			playerDamagers.override(en.getUniqueId(), (Player) en.getMetadata("KaGame_potionKiller").get(0).value());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public final void onPlayerDamageByBlock(EntityDamageByBlockEvent e) {
		if (!(e.getEntity() instanceof Player))
			return;

		final Player damaged = (Player) e.getEntity();
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(damaged.getLocation());

		if (arena != null)
			if (arena.getState() != ArenaState.STOPPED) {
				final double finalDmg = Remain.getFinalDamage(e);

				if (arena.getState() == ArenaState.RUNNING)
					try {
						playerDamagers.override(damaged.getUniqueId(), null);
						arena.onPlayerBlockDamage(e, damaged, finalDmg);

					} catch (final EventHandledException ex) {
					}
				else
					e.setCancelled(true);

			} else {
				if (CoreArenaPlugin.getSetupManager().isArenaEdited(arena))
					return;

				e.setCancelled(true);
				notifyCannotEdit(damaged);
			}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onEntityDeath(EntityDeathEvent e) {
		if (e.getEntity() == null)
			return;

		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(e.getEntity().getLocation());

		if (arena != null)
			if (arena.getState() == ArenaState.RUNNING && !CoreArenaPlugin.getSetupManager().isArenaEdited(arena))
				try {
					arena.onEntityDeath(e);
				} catch (final EventHandledException ex) {
				}
			else {
				e.getDrops().clear();
				e.setDroppedExp(0);
			}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onEntityTarget(EntityTargetEvent e) {
		if (e.getEntity() == null)
			return;

		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(e.getEntity().getLocation());

		if (arena != null)
			if (arena.getState() == ArenaState.RUNNING && !CoreArenaPlugin.getSetupManager().isArenaEdited(arena))
				try {
					arena.onEntityTarget(e);
				} catch (final EventHandledException ex) {
				}
			else
				e.setCancelled(true);
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
	public final void onBlockSpread(BlockSpreadEvent e) {
		if (e.getSource() != null)
			cancelIfInStoppedArena(e.getSource().getLocation(), e);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onEntityInteract(EntityInteractEvent e) {
		if (e.getEntity() != null)
			cancelIfNotEditedAndNotifyIfNotPlaying(e.getEntity().getLocation(), null, e);
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
			modifyToNotCombust(arena, en);
		else if (arena.getState() != ArenaState.RUNNING) {
			//Armor stands should be allowed to be spawned
			if (event.getEntity().getType() == EntityType.ARMOR_STAND)
				return;
			event.setCancelled(true);
		} else {
			arena.onEntitySpawn(event);

			if (!en.isDead())
				modifyToNotCombust(arena, en);
		}
	}

	private void modifyToNotCombust(Arena arena, LivingEntity en) {
		if (!(en instanceof Monster))
			return;

		// If monsters should not burn under sunlight, add an invisible button to their
		// head
		if (!arena.getSettings().allowMonstersBurn()) {
			final EntityEquipment eq = en.getEquipment();

			if (eq.getHelmet() == null || eq.getHelmet() != null && eq.getHelmet().getType() == Material.AIR) {
				eq.setHelmet(new ItemStack(Material.STONE_BUTTON));
				eq.setHelmetDropChance(0F);
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
									final double newHP = MathUtil.range(Remain.getHealth(l) - Settings.ProceduralDamage.Explosions.DAMAGE_DAMAGE, 0, l.getMaxHealth());

									l.setHealth(newHP);
								}

							final BlockState oldBlockState = oldBlock.getState();

							if (oldBlock.getType().toString().contains("GLASS"))
								glass = true;

							final Block damagedBlock = ((ArenaSnapshotProcedural) snap).restoreBlock(oldBlock, DamagedStage.DAMAGED);

							if (damagedBlock == null || damagedBlock.getType() != oldBlockState.getType()) {
								BlockPhysics.pushAway(arena, oldBlockState, Vector.getRandom());
								BlockPhysics.applyGravitation(arena, oldBlock, Settings.ProceduralDamage.Explosions.GRAVITATION_RADIUS_CHECK, true, 4);
							}
						}

						e.blockList().removeAll(toRemove);
					}

					if (glass)
						CompSound.GLASS.play(e.getEntity().getLocation(), 1F, 0.1F);

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
						CompSound.GLASS.play(e.getLocation(), 1F, 0.1F);
					
					e.blockList().clear();*/

				} else
					e.setCancelled(true);
		}
	}

	@EventHandler
	public final void onRocketExplosion(RocketExplosionEvent e) {
		cancelIfInStoppedArena(e.getProjectile().getLocation(), e);

		if (!e.isCancelled())
			storedExplosions.override(Common.shortLocation(e.getProjectile().getLocation()), e.getProjectile().getVelocity());
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

		final boolean blocked = cancelIfPlaying(e.getPlayer(), e);

		if (blocked)
			e.getPlayer().setAllowFlight(false);
	}

	/*
	 * @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	 * public final void onGamemodeChange(PlayerGameModeChangeEvent e) { final Arena
	 * arena = CorePlugin.getArenaManager().findArena(e.getPlayer());
	 *
	 * if (arena != null) { if (CorePlugin.getSetupManager().isArenaEdited(arena))
	 * return;
	 *
	 * if (arena.getState() == ArenaState.RUNNING) { e.setCancelled(true); } } }
	 */

	private final boolean cancelIfPlaying(Player pl, Cancellable e) {
		if (CoreArenaPlugin.getArenaManager().isPlaying(pl)) {
			e.setCancelled(true);

			return true;
		}

		return false;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onHangingBreak(HangingBreakByEntityEvent e) {
		if (e.getRemover() != null && e.getRemover() instanceof Player)
			cancelIfNotEditedAndNotifyIfNotPlaying(e.getEntity().getLocation(), (Player) e.getRemover(), e);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onBlockPlace(BlockPlaceEvent e) {
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(e.getBlock().getLocation());
		final Player pl = e.getPlayer();

		if (arena != null && !CoreArenaPlugin.getSetupManager().isArenaEdited(arena))
			if (pl != null && arena.getState() == ArenaState.STOPPED)
				notifyCannotEdit(pl);
			else
				try {
					arena.onPlayerBlockPlace(e);

				} catch (final CancelledException ex) {
					e.setCancelled(true);
				}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onBlockBreak(BlockBreakEvent e) {
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(e.getBlock().getLocation());
		final Player pl = e.getPlayer();

		if (arena != null && !CoreArenaPlugin.getSetupManager().isArenaEdited(arena))
			if (pl != null && arena.getState() == ArenaState.STOPPED)
				notifyCannotEdit(pl);
			else
				arena.onPlayerBlockBreak(e);
	}

	private final void cancelIfNotEditedAndNotifyIfNotPlaying(Location loc, Player pl, Cancellable e) {
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(loc);

		if (arena != null && !CoreArenaPlugin.getSetupManager().isArenaEdited(arena)) {
			if (pl != null && arena.getState() == ArenaState.STOPPED)
				notifyCannotEdit(pl);

			e.setCancelled(true);
		}
	}

	// ------------------------------------------------------------------------
	// Utility
	// ------------------------------------------------------------------------

	private void notifyCannotEdit(Player pl) {
		if (CoreUtil.checkPerm(pl, Permissions.Commands.EDIT, false))
			Common.tell(pl, Localization.Arena.Setup.CANNOT_EDIT);
	}
}

class EventsProceduralDamage18 implements Listener {

	private final HashSet<String> undamagedPlayers = new HashSet<>();

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onProjectileLaunch(ProjectileLaunchEvent e) {
		final Projectile p = e.getEntity();

		if (p != null && p.getShooter() instanceof Player) {
			final Arena arena = CoreArenaPlugin.getArenaManager().findArena(p.getLocation());
			final Player pl;

			if (arena == null)
				return;

			if (arena.getState() != ArenaState.RUNNING || !arena.isJoined(pl = (Player) p.getShooter())) {
				e.setCancelled(true);

				return;
			}

			if (!ItemUtil.isSimilar(pl.getItemInHand(), ExplosiveBow.getItem()))
				return;

			final PlayerInventory inv = pl.getInventory();
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

				p.setMetadata("CoreBow", new FixedMetadataValue(CoreArenaPlugin.getInstance(), "true"));

				CompSound.FIREWORK_BLAST.play(pl, 1, (float) Math.random());

				// Set amount
				Common.runLater(() -> {
					Valid.checkNotNull(toTakeFinal);

					Remain.takeItemOnePiece(pl, toTakeFinal);
				});

				// Effects
				new BukkitRunnable() {
					final Projectile arrow = e.getEntity();

					@Override
					public void run() {
						if (arrow.isDead() || !arrow.isValid() || arrow.isOnGround())
							cancel();
						else
							CompParticle.SPELL_WITCH.spawn(arrow.getLocation());
					}
				}.runTaskTimer(CoreArenaPlugin.getInstance(), 0, 1);

			} else {
				e.setCancelled(true);

				CompSound.ENTITY_ITEMFRAME_BREAK.play(pl, 1, (float) Math.random());
				Common.tell(pl, Localization.Arena.Game.EXPLOSIVE_ARROW_NO_AMMO);
			}
		}
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageByBlockEvent e) {
		if (e.getEntityType() != EntityType.PLAYER)
			return;

		final String name = ((Player) e.getEntity()).getName();

		if (e.getCause() == DamageCause.BLOCK_EXPLOSION && undamagedPlayers.contains(name)) {
			e.setDamage(0);

			undamagedPlayers.remove(name);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onProjectileHit(ProjectileHitEvent e) {
		final Projectile proj = e.getEntity();

		if (proj != null && proj.getShooter() instanceof Player) {
			final Arena arena = CoreArenaPlugin.getArenaManager().findArena(proj.getLocation());

			if (arena != null && proj.hasMetadata("CoreBow")) {
				final Player player = (Player) proj.getShooter();

				if (arena.getPlayers().contains(player)) {
					final String loc = Common.shortLocation(proj.getLocation());

					InArenaListener.storedExplosions.override(loc, proj.getVelocity());

					player.getWorld().playEffect(proj.getLocation(), Effect.SMOKE, 0);

					if (!arena.getSettings().explosiveArrowPlayerDamage()) { // Prevent damage of players
						final double radius = Settings.Items.ExplosiveBow.DAMAGE + Settings.ProceduralDamage.Explosions.DAMAGE_RADIUS;

						for (final Entity en : Remain.getNearbyEntities(proj.getLocation(), radius))
							if (en instanceof Player)
								undamagedPlayers.add(((Player) en).getName());
					}

					player.getWorld().createExplosion(proj.getLocation(), Settings.Items.ExplosiveBow.DAMAGE);

					proj.remove();
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onBlockExplode(BlockExplodeEvent e) {
		if (e.getBlock() == null)
			return;

		final String serializedLocation = Common.shortLocation(e.getBlock().getLocation());
		final Vector velocity = InArenaListener.storedExplosions.removeWeak(serializedLocation);

		if (velocity == null)
			return;

		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(e.getBlock().getLocation());

		if (arena != null && arena.getState() == ArenaState.RUNNING) {
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
							final double newHP = MathUtil.range(Remain.getHealth(l) - Settings.ProceduralDamage.Explosions.DAMAGE_DAMAGE, 0, l.getMaxHealth());

							l.setHealth(newHP);
						}

					final BlockState oldBlockState = oldBlock.getState();

					if (oldBlock.getType().toString().contains("GLASS"))
						glass = true;

					final Block damagedBlock = ((ArenaSnapshotProcedural) snap).restoreBlock(oldBlock, DamagedStage.DAMAGED);

					if (damagedBlock == null || damagedBlock.getType() != oldBlockState.getType()) {
						BlockPhysics.pushAway(arena, oldBlockState, velocity);
						BlockPhysics.applyGravitation(arena, oldBlock, Settings.ProceduralDamage.Explosions.GRAVITATION_RADIUS_CHECK, true, 4);
					}
				}

				e.blockList().removeAll(toRemove);
			}

			if (glass)
				CompSound.GLASS.play(e.getBlock().getLocation(), 1F, 0.1F);

			e.blockList().clear();
			undamagedPlayers.clear();
		}
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
	public final void onItemPickupEntity(EntityPickupItemEvent e) {
		if (e.getEntityType() != EntityType.PLAYER) {
			if (e.getItem() == null || e.getItem().getItemStack() == null)
				return;

			if (ExpItemHandler.isExpItem(e.getItem()))
				e.setCancelled(true);
		}
	}
}