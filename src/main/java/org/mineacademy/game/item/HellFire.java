package org.mineacademy.game.item;

public class HellFire /*extends Rocket */ {

	/*@Getter
	private static final HellFire instance = new HellFire();

	private final StrictMap<UUID, Long> lastUsageMap = new StrictMap<>();

	private final long getLastUseAgo(Player player) {
		if (!lastUsageMap.contains(player.getUniqueId())) {
			lastUsageMap.put(player.getUniqueId(), TimeUtilFo.currentTimeSeconds());

			return Integer.MAX_VALUE;
		}

		return TimeUtilFo.currentTimeSeconds() - lastUsageMap.get(player.getUniqueId());
	}

	private final void updateUse(Player player) {
		lastUsageMap.override(player.getUniqueId(), TimeUtilFo.currentTimeSeconds());
	}

	@Override
	public boolean canLaunch(Player pl, Location loc) {
		return CoreUtil.isWithinArena(pl, loc);
	}

	@Override
	public ItemStack getItem() {
		return ItemCreator.of(CompMaterial.LEVER)
				.name("&CCall Hell-Fire")
				.enchant(new SimpleEnchant(Enchantment.DURABILITY, 1))
				.flag(CompItemFlag.HIDE_ENCHANTS)
				.lores(Arrays.asList(
						"",
						"&fLeft click &7a block",
						"&7to call a Hell-File!"))
				.build().make();
	}

	private final void operator(Player pl, String message) {
		Common.tellNoPrefix(pl, "&8[&5Operator&8] &7" + message);
	}

	@Override
	public void onBlockClick(PlayerInteractEvent e) {
		final Player pl = e.getPlayer();

		if (GamePlugin.getArenaManager().findArena(pl).getState() != ArenaState.RUNNING) {
			e.setCancelled(true);

			return;
		}

		if (!e.hasBlock() || e.getAction() != Action.LEFT_CLICK_BLOCK) {
			operator(pl, "&7Left-click a block to send me the coordinates!");

			return;
		}

		e.setCancelled(true);

		if (!CoreUtil.isWithinArena(pl, e.getClickedBlock().getLocation()))
			return;

		if (getLastUseAgo(pl) < 10) {
			operator(pl, "&6Reloading missiles... Ready in " + Localization.Cases.SECOND.formatWithCount(10 - getLastUseAgo(pl)) + "!");
			return;
		}

		final Block up = e.getClickedBlock().getRelative(BlockFace.UP);
		if (up.getType() != Material.AIR)
			return;

		final Location initialLoc = up.getLocation().add(0.5, 0, 0.5);
		operator(pl, "&a&lCalled a hell fire to " + up.getX() + " " + up.getY() + " " + up.getZ() + "!");

		Remain.takeItemAndSetAsHand(pl, pl.getItemInHand());
		updateUse(pl);

		Common.runLater(40, () -> {

			operator(pl, "&c&lHell fire incoming!");
			CompSound.FIREWORK_LAUNCH.play(pl, 1F, 0.1F);

			final Location loc = up.getLocation();

			// Spawn in the air
			loc.setY(256);

			final int explodeY = up.getWorld().getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ());

			final LargeFireball rocket = up.getWorld().spawn(loc, LargeFireball.class);
			rocket.setDirection(new Vector(0, -0.0001, 0).normalize());
			rocket.setYield(0F);
			rocket.setIsIncendiary(false);

			final long firedSince = TimeUtilFo.currentTimeSeconds();

			// Effects + explosion
			new BukkitRunnable() {

				@Override
				public void run() {
					rocket.setVelocity(new Vector(0, -3.7, 0));

					// Invalidated
					if (rocket.isDead() || !rocket.isValid()) {
						cancel();

						return;
					}

					final Location l = rocket.getLocation().clone();

					// Hit some ground
					if (l.getY() - explodeY < 5) {
						explode(rocket, l, 5F, true);

						new SoundPlayer(l).start();

						rocket.remove();
						cancel();
						return;
					}

					// Too long ago
					if (TimeUtilFo.currentTimeSeconds() - firedSince > 20) {
						cancel();

						return;
					}

					// Just fire particle
					CompParticle.FLAME.spawn(l);
					CompParticle.BLOCK_CRACK.spawnWithData(initialLoc, CompMaterial.DIRT);
				}

			}.runTaskTimer(SimplePlugin.getInstance(), 0, 1);
		});
	}

	@RequiredArgsConstructor
	class SoundPlayer extends Thread {

		private final Location l;

		@Override
		public void run() {
			CompSound.ANVIL_LAND.play(l, 1F, 0.1F);

			double count = 1;

			final int radius = 2;

			for (double y = -5; y <= 10; y += 0.05) {
				final double x = radius * Math.cos(y) * Math.random() * (count = count + 0.05);
				final double z = radius * Math.sin(y) * Math.random() * count;

				try {
					CompParticle.BLOCK_CRACK.spawnWithData(l.clone().add(x, y, z), CompMaterial.STONE);
				} catch (final Throwable t) {
					t.printStackTrace();

					break;
				}

				Common.sleep(4);
			}

			CompSound.FIREWORK_TWINKLE2.play(l, 1F, 0.1F);
		}
	}

	@Override
	public void onLaunch(Projectile proj, Player shooter) {
	}

	@Override
	public void onHit(Projectile proj, Player shooter, Location loc) {
		if (CoreUtil.isWithinArena(shooter, loc))
			explode(proj, loc, 2F, false);
	}

	@Override
	public boolean ignoreCancelled() {
		return false;
	}*/
}
