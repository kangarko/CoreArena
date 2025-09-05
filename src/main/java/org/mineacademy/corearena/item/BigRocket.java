package org.mineacademy.corearena.item;

import org.bukkit.Location;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.mineacademy.corearena.util.CoreUtil;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.RandomUtil;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.tool.Rocket;
import org.mineacademy.fo.remain.CompEnchantment;
import org.mineacademy.fo.remain.CompItemFlag;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompParticle;
import org.mineacademy.fo.remain.CompSound;

import lombok.Getter;

public final class BigRocket extends Rocket {

	@Getter
	private static final BigRocket instance = new BigRocket();

	private BigRocket() {
		super(EnderPearl.class, 1F, 6F);
	}

	@Override
	public ItemStack getItem() {
		return ItemCreator.fromMaterial(CompMaterial.ENDER_PEARL)
				.name("&3Rocket")
				.enchant(CompEnchantment.DURABILITY)
				.flags(CompItemFlag.HIDE_ENCHANTS)
				.lore(
						"",
						"&7Click to launch",
						"&7a rocket!")
				.make();
	}

	@Override
	public boolean canLaunch(Player pl, Location loc) {
		return CoreUtil.isWithinArena(pl, loc);
	}

	@Override
	public void onLaunch(Projectile proj, Player shooter) {
		CompSound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR.play(shooter, 1F, 1F);

		PlayerUtil.takeOnePiece(shooter, shooter.getItemInHand());
	}

	@Override
	protected void onFlyTick(Projectile projectile, Player shooter) {
		final double a = Double.parseDouble("0.0" + RandomUtil.nextInt(3));
		final double b = Double.parseDouble("0.0" + RandomUtil.nextInt(3));
		final double c = Double.parseDouble("0.0" + RandomUtil.nextInt(3));

		projectile.setVelocity(projectile.getVelocity().add(new Vector(a, b, c)));

		CompParticle.EXPLOSION_NORMAL.spawn(projectile.getLocation());
		CompParticle.FLAME.spawn(projectile.getLocation());
	}

	@Override
	protected boolean canExplode(Projectile projectile, Player shooter) {
		return CoreUtil.isWithinArena(shooter, projectile.getLocation());
	}

	@Override
	protected void onExplode(Projectile projectile, Player shooter) {
		CompSound.ENTITY_FIREWORK_ROCKET_BLAST.play(shooter, 1F, 0.1F);
	}
}
