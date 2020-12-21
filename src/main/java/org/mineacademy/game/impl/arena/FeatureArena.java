package org.mineacademy.game.impl.arena;

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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.HealthBarUtil;
import org.mineacademy.fo.RandomUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.StrictList;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.game.exception.CancelledException;
import org.mineacademy.game.exception.EventHandledException;
import org.mineacademy.game.exp.ExpItemHandler;
import org.mineacademy.game.impl.ArenaPlayer;
import org.mineacademy.game.impl.ArenaPlayer.ClassCache;
import org.mineacademy.game.impl.ArenaPlayer.InArenaCache;
import org.mineacademy.game.impl.SimpleSettings;
import org.mineacademy.game.impl.SimpleSetup;
import org.mineacademy.game.manager.SpawnPointManager;
import org.mineacademy.game.menu.MenuInArenaClasses;
import org.mineacademy.game.model.ArenaClass;
import org.mineacademy.game.model.ExpItem;
import org.mineacademy.game.model.Setup;
import org.mineacademy.game.model.SpawnPoint;
import org.mineacademy.game.settings.Localization;
import org.mineacademy.game.settings.Settings;
import org.mineacademy.game.type.DeathCause;
import org.mineacademy.game.type.LeaveCause;
import org.mineacademy.game.type.SpawnPointType;
import org.mineacademy.game.type.TierMode;

import lombok.Getter;

public abstract class FeatureArena extends SimpleArena {

	@Getter
	private final Setup setup;

	private final SpawnPointManager spawnPoints;

	protected FeatureArena(String name) {
		super(new SimpleSettings(name));

		setup = new SimpleSetup(this);
		spawnPoints = new SpawnPointManager(getData());
	}

	@Override
	protected final void handlePlayerPostJoin(Player pl) {
		final ArenaPlayer data = CoreArenaPlugin.getDataFor(pl);
		final InArenaCache arenaCache = data.getArenaCache();

		{ // Set lifes
			arenaCache.lifesLeft = getSettings().getLifes();
			Valid.checkBoolean(arenaCache.lifesLeft != -1, "Please configure 'Lifes' in your " + getName() + " arena");
		}

		if (!getSettings().allowOwnEquipment() && data.getClassCache().assignedClass == null && getSettings().openClassMenu())
			new MenuInArenaClasses(this, pl).displayTo(pl);
	}

	@Override
	protected final void handlePrePlayerLeave(Player pl, LeaveCause cause) {
		{ // Classes
			final Menu menu = Menu.getMenu(pl);

			if (menu != null && menu instanceof MenuInArenaClasses)
				pl.closeInventory();
		}
	}

	@Override
	public final void onPlayerPvP(EntityDamageByEntityEvent e, Player damager, Player victim, double damage) throws EventHandledException {
		final int pvpPhase = getSettings().getPvpPhase();

		if (pvpPhase != -1 && getPhase().getCurrent() >= pvpPhase)
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

		if (type == Material.TNT && getSettings().igniteTnts()) {
			final TNTPrimed tnt = loc.getWorld().spawn(loc.add(0.5, 0, 0.5), TNTPrimed.class);
			tnt.setFuseTicks(40);

			Remain.takeHandItem(e.getPlayer());
			returnCancelled();
		}

		if (!getSettings().getPlaceList().isAllowed(type))
			returnCancelled();
	}

	@Override
	public final void onPlayerBlockBreak(BlockBreakEvent e) {
		final Material type = e.getBlock().getType();

		if (!getSettings().getBreakingList().isAllowed(type))
			e.setCancelled(true);
	}

	@Override
	public final void onPlayerClickAir(Player pl, ItemStack hand) throws CancelledException {
		if (hand == null)
			return;

		if (hand.getType() == CompMaterial.FIRE_CHARGE.getMaterial() && getSettings().launchFireballs()) {
			pl.launchProjectile(RandomUtil.chance(75) ? Fireball.class : SmallFireball.class);

			Remain.takeHandItem(pl);
			returnCancelled();
		}
	}

	@Override
	public final void onPlayerClick(Player pl, Block block, ItemStack hand) throws CancelledException {
		if (hand == null)
			return;

		final Material type = hand.getType();
		final Location loc = block.getLocation();

		if (type == Material.BONE && getSettings().spawnWolves()) {
			final Wolf wolf = loc.getWorld().spawn(loc.add(0.5, 1, 0.5), Wolf.class);

			wolf.setOwner(pl);
			wolf.setTamed(true);

			final Monster target = getMobTarget(wolf.getLocation(), 12);

			if (target != null)
				wolf.setTarget(target);

			Remain.takeHandItem(pl);

			return;
		}
	}

	private final Monster getMobTarget(Location loc, int radius) {
		for (final Entity en : Remain.getNearbyEntities(loc, radius))
			if (en instanceof Monster && getData().getRegion().isWithin(en.getLocation()))
				return (Monster) en;

		return null;
	}

	@Override
	public void onPlayerDeath(Player pl, Player killer) throws EventHandledException {
		super.onPlayerDeath(pl, killer);

		final InArenaCache cache = CoreArenaPlugin.getDataFor(pl).getArenaCache();

		if (--cache.lifesLeft < 1) {

			getMessenger().broadcastExcept(pl, pl, Localization.Arena.Game.KICK_KILL_BROADCAST.replace("{killer}", killer.getName()));
			getMessenger().tell(pl, Localization.Arena.Game.KICK_KILL_TO_VICTIM.replace("{killer}", killer.getName()));

			respawnAndKick(cache, pl, true);

		} else {
			getMessenger().tell(pl, Localization.Arena.Game.KILL_TO_VICTIM.replace("{lifes}", Localization.Cases.LIFE.formatWithCount(cache.lifesLeft)));
			getMessenger().broadcastExcept(pl, pl, Localization.Arena.Game.KILL_BROADCAST.replace("{player}", pl.getName()).replace("{killer}", killer.getName()).replace("{lifes_left}", getSettings().getLifes() - cache.lifesLeft + "").replace("{lifes_max}", getSettings().getLifes() + ""));

			respawnAndKick(cache, pl, false);
		}
	}

	@Override
	public void onPlayerDeath(Player pl, DeathCause cause) throws EventHandledException {
		super.onPlayerDeath(pl, cause);

		final InArenaCache cache = CoreArenaPlugin.getDataFor(pl).getArenaCache();

		if (--cache.lifesLeft < 1) {
			getMessenger().broadcastExcept(pl, pl, Localization.Arena.Game.KICK_DEATH_BROADCAST);
			getMessenger().tell(pl, Localization.Arena.Game.KICK_DEATH_TO_VICTIM);

			respawnAndKick(cache, pl, true);

		} else {
			getMessenger().tell(pl, Localization.Arena.Game.DEATH_TO_VICTIM.replace("{lifes_left}", Localization.Cases.LIFE.formatWithCount(cache.lifesLeft)));
			getMessenger().broadcastExcept(pl, pl, Localization.Arena.Game.DEATH_BROADCAST.replace("{player}", pl.getName()).replace("{deaths}", "" + (getSettings().getLifes() - cache.lifesLeft)).replace("{lifes_max}", getSettings().getLifes() + ""));

			respawnAndKick(cache, pl, false);
		}
	}

	private final void respawnAndKick(InArenaCache cache, Player pl, boolean kick) throws EventHandledException {
		if (kick)
			cache.pendingRemoval = true;

		Remain.respawn(pl);
		returnHandled();
	}

	@Override
	public void onPlayerRespawn(PlayerRespawnEvent e) throws EventHandledException {
		super.onPlayerRespawn(e);

		final Player pl = e.getPlayer();
		final ArenaPlayer data = CoreArenaPlugin.getDataFor(pl);
		final InArenaCache cache = data.getArenaCache();

		{ // Handle spawn points
			if (getSettings().isRespawningRandom())
				assignRandomSpawnPoint(data, pl);

			Valid.checkNotNull(cache.spawnPoint, "Spawnpoint in " + getName() + " for " + pl.getName() + " not assigned!");
			e.setRespawnLocation(cache.spawnPoint.getLocation());
		}

		// Handle classes
		if (!getSettings().allowOwnEquipment()) {
			final ClassCache plClass = data.getClassCache();
			Valid.checkNotNull(plClass.assignedClass, "Unable to respawn " + pl.getName() + " as he is missing class!");

			pl.getInventory().clear();

			Common.runLater(10, () -> {
				plClass.assignedClass.giveToPlayer(pl, TierMode.PLAY);
			});
		}
	}

	@Override
	public final void onEntityDeath(EntityDeathEvent e) throws EventHandledException {
		super.onEntityDeath(e);
		super.checkRunning();

		final Entity en = e.getEntity();
		final int exp = getSettings().getExpFor(en, getPhase().getCurrent());

		spawnExperienceItem(en.getLocation(), exp);

		e.setDroppedExp(0);

		if (!getSettings().allowNaturalDrops() || e.getEntityType() == EntityType.PLAYER && !getSettings().allowOwnEquipment())
			e.getDrops().clear();
	}

	private final void spawnExperienceItem(Location location, int exp) {
		ExpItemHandler.spawn(location, exp);
	}

	@Override
	public final void onPlayerPickupTag(PlayerPickupItemEvent e, ExpItem tag) throws EventHandledException {
		super.checkRunning();

		final Player pl = e.getPlayer();
		final Item item = e.getItem();
		final InArenaCache cache = CoreArenaPlugin.getDataFor(e.getPlayer()).getArenaCache();

		// Give exp to player
		{
			cache.giveAndShowExp(pl, tag.getExpGiven());

			CompSound.ORB_PICKUP.play(pl, 1, 1);
			Common.tell(pl, Localization.Experience.PICKUP.replace("{level}", Localization.Cases.LEVEL.formatWithCount(cache.getLevel())).replace("{exp}", Localization.Cases.EXP.formatWithCount(cache.getExp())));
		}

		// Take care of the item
		{
			e.setCancelled(true);
			item.remove();
		}
	}

	@Override
	protected void handleArenaPostStart() {
		final StrictList<Player> toKick = new StrictList<>();

		for (final Player pl : getPlayers()) {
			final ArenaPlayer data = CoreArenaPlugin.getDataFor(pl);
			final ClassCache cache = data.getClassCache();

			{ // Handle classes
				if (cache.assignedClass != null)
					Valid.checkBoolean(data.getTierOf(cache.assignedClass) >= getSettings().getMinimumTier(), "Found too low class tier for " + pl.getName() + " in " + getName());

				if (!getSettings().allowOwnEquipment()) {
					if (cache.assignedClass == null) {
						final ArenaClass random = Settings.Arena.GIVE_RANDOM_CLASS_IF_NOT_SELECTED ? getClassManager().findRandomClassFor(pl, getSettings().getMinimumTier()) : null;

						if (random == null) {
							getMessenger().tell(pl, Localization.Arena.Lobby.KICK_NO_CLASS);

							toKick.add(pl);
							continue;

						}

						getMessenger().tell(pl, Localization.Arena.Game.CLASS_AUTO_ASSIGNED.replace("{class}", random.getName()));

						cache.assignedClass = random;
					}

					cache.assignedClass.giveToPlayer(pl, TierMode.PLAY);
				}
			}

			{ // Handle spawn points
				final Location spawn = assignRandomSpawnPoint(data, pl);

				pl.teleport(spawn.add(0, 1.2, 0));
			}
		}

		for (final Player pl : toKick)
			kickPlayer(pl, LeaveCause.NOT_READY);
	}

	private final Location assignRandomSpawnPoint(ArenaPlayer data, Player player) {
		final SpawnPoint point = spawnPoints.getRandomSpawnPoint(player, SpawnPointType.PLAYER);

		data.getArenaCache().spawnPoint = point;
		return point.getLocation().clone();
	}
}
