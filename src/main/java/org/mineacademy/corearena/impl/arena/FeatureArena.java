package org.mineacademy.corearena.impl.arena;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Wolf;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.data.AllData.ArenaPlayer;
import org.mineacademy.corearena.data.AllData.ArenaPlayer.ClassCache;
import org.mineacademy.corearena.data.AllData.ArenaPlayer.InArenaCache;
import org.mineacademy.corearena.exception.CancelledException;
import org.mineacademy.corearena.exception.EventHandledException;
import org.mineacademy.corearena.exp.ExpItemHandler;
import org.mineacademy.corearena.impl.SimpleSettings;
import org.mineacademy.corearena.impl.SimpleSetup;
import org.mineacademy.corearena.manager.SpawnPointManager;
import org.mineacademy.corearena.menu.MenuInArenaClasses;
import org.mineacademy.corearena.model.ArenaClass;
import org.mineacademy.corearena.model.ExpItem;
import org.mineacademy.corearena.model.Setup;
import org.mineacademy.corearena.model.SpawnPoint;
import org.mineacademy.corearena.settings.Settings;
import org.mineacademy.corearena.type.DeathCause;
import org.mineacademy.corearena.type.LeaveCause;
import org.mineacademy.corearena.type.SpawnPointType;
import org.mineacademy.corearena.type.TierMode;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.HealthBarUtil;
import org.mineacademy.fo.RandomUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.fo.settings.Lang;

import lombok.Getter;

public abstract class FeatureArena extends SimpleArena {

	@Getter
	private final Setup setup;
	private final SpawnPointManager spawnPoints;

	protected FeatureArena(String name) {
		super(new SimpleSettings(name));

		this.setup = new SimpleSetup(this);
		this.spawnPoints = new SpawnPointManager(this.getData());
	}

	@Override
	protected final void handlePlayerPostJoin(Player pl) {
		final ArenaPlayer data = CoreArenaPlugin.getDataFor(pl);
		final InArenaCache arenaCache = data.getArenaCache();

		{ // Set lifes
			arenaCache.lifesLeft = this.getSettings().getLifes();
			Valid.checkBoolean(arenaCache.lifesLeft != -1, "Please configure 'Lifes' in your " + this.getName() + " arena");
		}

		if (!this.getSettings().allowOwnEquipment() && data.getClassCache().assignedClass == null && this.getSettings().openClassMenu())
			new MenuInArenaClasses(this, pl).displayTo(pl);
	}

	@Override
	protected final void handlePrePlayerLeave(Player pl, LeaveCause cause) {
		{ // Classes
			final Menu menu = Menu.getMenu(pl);

			if (menu instanceof MenuInArenaClasses)
				pl.closeInventory();
		}
	}

	@Override
	public final void onPlayerPvP(EntityDamageByEntityEvent e, Player damager, Player victim, double damage) throws EventHandledException {
		final int pvpPhase = this.getSettings().getPvpPhase();

		if (pvpPhase != -1 && this.getPhase().getCurrent() >= pvpPhase)
			HealthBarUtil.display(damager, victim, damage);

		else
			e.setCancelled(true);
	}

	@Override
	public final void onPlayerPvE(Player damager, LivingEntity victim, double damage) throws EventHandledException {
		if (Settings.Arena.SHOW_HEALTH_BAR)
			HealthBarUtil.display(damager, victim, damage);
	}

	@Override
	public final void onPlayerBlockPlace(BlockPlaceEvent e) {
		final Material type = e.getBlock().getType();
		final Location loc = e.getBlock().getLocation();

		if (type == Material.TNT && this.getSettings().igniteTnts()) {
			final TNTPrimed tnt = loc.getWorld().spawn(loc.add(0.5, 0, 0.5), TNTPrimed.class);
			tnt.setFuseTicks(40);

			Remain.takeHandItem(e.getPlayer());
			this.returnCancelled();
		}

		if (!this.getSettings().getPlaceList().isAllowed(type))
			this.returnCancelled();
	}

	@Override
	public final void onPlayerBlockBreak(BlockBreakEvent e) {
		final Material type = e.getBlock().getType();

		if (!this.getSettings().getBreakingList().isAllowed(type))
			e.setCancelled(true);
	}

	@Override
	public final void onPlayerClickAir(Player pl, ItemStack hand) throws CancelledException {
		if (hand == null)
			return;

		if (hand.getType() == CompMaterial.FIRE_CHARGE.getMaterial() && this.getSettings().launchFireballs()) {
			pl.launchProjectile(RandomUtil.chance(75) ? Fireball.class : SmallFireball.class);

			Remain.takeHandItem(pl);
			this.returnCancelled();
		}
	}

	@Override
	public final void onPlayerClick(Player pl, Block block, ItemStack hand) throws CancelledException {
		if (hand == null)
			return;

		final Material type = hand.getType();
		final Location loc = block.getLocation();

		if (type == Material.BONE && this.getSettings().spawnWolves()) {
			final Wolf wolf = loc.getWorld().spawn(loc.add(0.5, 1, 0.5), Wolf.class);

			wolf.setOwner(pl);
			wolf.setTamed(true);

			final Monster target = this.getMobTarget(wolf.getLocation(), 12);

			if (target != null)
				wolf.setTarget(target);

			Remain.takeHandItem(pl);

			return;
		}
	}

	private final Monster getMobTarget(Location loc, int radius) {
		for (final Entity en : Remain.getNearbyEntities(loc, radius))
			if (en instanceof Monster && this.getData().getRegion().isWithin(en.getLocation()))
				return (Monster) en;

		return null;
	}

	@Override
	public void onPlayerDeath(Player player, Player killer) throws EventHandledException {
		super.onPlayerDeath(player, killer);

		final InArenaCache cache = CoreArenaPlugin.getDataFor(player).getArenaCache();

		if (--cache.lifesLeft < 1) {

			this.getMessenger().broadcastExcept(player, player, Lang.component("arena-game-kick-kill-broadcast", "killer", killer.getName()));
			this.getMessenger().tell(player, Lang.component("arena-game-kick-kill-to-victim", "killer", killer.getName()));

			this.respawnAndKick(cache, player, true);

		} else {
			this.getMessenger().tell(player, Lang.component("arena-game-kill-to-victim", "lifes", Lang.numberFormat("case-life", cache.lifesLeft)));
			this.getMessenger().broadcastExcept(player, player, Lang.component("arena-game-kill-broadcast", "player", player.getName(), "killer", killer.getName(), "lifes_left", String.valueOf(this.getSettings().getLifes() - cache.lifesLeft), "lifes_max", String.valueOf(this.getSettings().getLifes())));

			this.respawnAndKick(cache, player, false);
		}
	}

	@Override
	public void onPlayerDeath(Player player, DeathCause cause) throws EventHandledException {
		super.onPlayerDeath(player, cause);

		final InArenaCache cache = CoreArenaPlugin.getDataFor(player).getArenaCache();

		if (--cache.lifesLeft < 1) {
			this.getMessenger().broadcastExcept(player, player, Lang.component("arena-game-kick-death-broadcast"));
			this.getMessenger().tell(player, Lang.component("arena-game-kick-death-to-victim"));

			this.respawnAndKick(cache, player, true);

		} else {
			this.getMessenger().tell(player, Lang.component("arena-game-death-to-victim", "lifes_left", Lang.numberFormat("case-life", cache.lifesLeft)));
			this.getMessenger().broadcastExcept(player, player, Lang.component("arena-game-death-broadcast", "player", player.getName(), "deaths", String.valueOf(this.getSettings().getLifes() - cache.lifesLeft), "lifes_max", String.valueOf(this.getSettings().getLifes())));

			this.respawnAndKick(cache, player, false);
		}
	}

	private final void respawnAndKick(InArenaCache cache, Player pl, boolean kick) throws EventHandledException {
		if (kick)
			cache.pendingRemoval = true;

		Remain.respawn(pl);
		this.returnHandled();
	}

	@Override
	public void onPlayerRespawn(PlayerRespawnEvent e) throws EventHandledException {
		super.onPlayerRespawn(e);

		final Player pl = e.getPlayer();
		final ArenaPlayer data = CoreArenaPlugin.getDataFor(pl);
		final InArenaCache cache = data.getArenaCache();

		{ // Handle spawn points
			if (this.getSettings().isRespawningRandom())
				this.assignRandomSpawnPoint(data, pl);

			Valid.checkNotNull(cache.spawnPoint, "Spawnpoint in " + this.getName() + " for " + pl.getName() + " not assigned!");
			e.setRespawnLocation(cache.spawnPoint.getLocation());
		}

		// Handle classes
		if (!this.getSettings().allowOwnEquipment()) {
			final ClassCache plClass = data.getClassCache();
			Valid.checkNotNull(plClass.assignedClass, "Unable to respawn " + pl.getName() + " as he is missing class!");

			pl.getInventory().clear();

			Platform.runTask(10, () -> {
				plClass.assignedClass.giveToPlayer(pl, TierMode.PLAY, false);
			});
		}
	}

	@Override
	public final void onEntityDeath(EntityDeathEvent event) throws EventHandledException {
		super.onEntityDeath(event);
		super.checkRunning();

		final Entity diedEntity = event.getEntity();
		final int exp = this.getSettings().getExpFor(diedEntity, this.getPhase().getCurrent());

		final EntityDamageEvent lastDamage = diedEntity.getLastDamageCause();
		boolean directDrop = false;

		if (Settings.Experience.GIVE_TO_KILLER && lastDamage != null && lastDamage instanceof EntityDamageByEntityEvent) {
			final Entity damager = ((EntityDamageByEntityEvent) lastDamage).getDamager();

			if (damager instanceof Player) {
				this.giveExp((Player) damager, exp);

				directDrop = true;
			}
		}

		if (!directDrop)
			this.spawnExperienceItem(diedEntity.getLocation(), exp);

		event.setDroppedExp(0);

		if (!this.getSettings().allowNaturalDrops() || event.getEntityType() == EntityType.PLAYER && !this.getSettings().allowOwnEquipment())
			event.getDrops().clear();
	}

	private void spawnExperienceItem(Location location, int exp) {
		ExpItemHandler.spawn(location, exp);
	}

	@Override
	public final void onPlayerPickupTag(PlayerPickupItemEvent event, ExpItem expItem) throws EventHandledException {
		super.checkRunning();

		final Player player = event.getPlayer();
		final Item item = event.getItem();

		// Give exp to player
		this.giveExp(player, expItem.getExpGiven());

		// Take care of the item
		event.setCancelled(true);
		item.remove();
	}

	private void giveExp(Player player, int expGiven) {
		final InArenaCache cache = CoreArenaPlugin.getDataFor(player).getArenaCache();

		cache.giveAndShowExp(player, expGiven);

		CompSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(player, 1, 1);
		Common.tell(player, Lang.component("experience-pickup", "level", Lang.numberFormat("case-level", cache.getLevel()), "exp", Lang.numberFormat("case-exp", cache.getExp())));
	}

	@Override
	protected void handleArenaPostStart() {
		final List<Player> toKick = new ArrayList<>();

		for (final Player player : this.getPlayers()) {
			final ArenaPlayer data = CoreArenaPlugin.getDataFor(player);
			final ClassCache cache = data.getClassCache();

			{ // Handle classes
				if (cache.assignedClass != null)
					Valid.checkBoolean(data.getTierOf(cache.assignedClass) >= this.getSettings().getMinimumTier(), "Found too low class tier for " + player.getName() + " in " + this.getName());

				if (!this.getSettings().allowOwnEquipment()) {
					if (cache.assignedClass == null) {
						final ArenaClass random = Settings.Arena.GIVE_RANDOM_CLASS_IF_NOT_SELECTED ? this.getClassManager().findRandomClassFor(player, this.getSettings().getMinimumTier()) : null;

						if (random == null) {
							this.getMessenger().tell(player, Lang.component("arena-lobby-kick-no-class"));

							toKick.add(player);
							continue;

						}

						this.getMessenger().tell(player, Lang.component("arena-game-class-auto-assigned", "class", random.getName()));

						cache.assignedClass = random;
					}

					cache.assignedClass.giveToPlayer(player, TierMode.PLAY, false);
				}
			}

			{ // Handle spawn points
				final Location spawn = this.assignRandomSpawnPoint(data, player);

				player.teleport(spawn.add(0, 1.2, 0));
			}
		}

		for (final Player pl : toKick)
			this.kickPlayer(pl, LeaveCause.NOT_READY);
	}

	private final Location assignRandomSpawnPoint(ArenaPlayer data, Player player) {
		final SpawnPoint point = this.spawnPoints.getRandomSpawnPoint(player, SpawnPointType.PLAYER);

		data.getArenaCache().spawnPoint = point;
		return point.getLocation().clone();
	}
}
