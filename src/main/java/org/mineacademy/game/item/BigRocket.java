package org.mineacademy.game.item;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.mineacademy.fo.RandomUtil;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.tool.Rocket;
import org.mineacademy.fo.model.SimpleEnchant;
import org.mineacademy.fo.remain.CompItemFlag;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.CompParticle;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.game.util.CoreUtil;

import lombok.Getter;

public class BigRocket extends Rocket {

	@Getter
	private static final BigRocket instance = new BigRocket();

	private BigRocket() {
		super(EnderPearl.class, 1F, 6F);
	}

	@Override
	public ItemStack getItem() {
		return ItemCreator.of(CompMaterial.ENDER_PEARL)
				.name("&3Rocket")
				.enchant(new SimpleEnchant(Enchantment.DURABILITY, 1))
				.flag(CompItemFlag.HIDE_ENCHANTS)
				.lores(Arrays.asList(
						"",
						"&7Click to launch",
						"&7a rocket!"))
				.build().make();
	}

	@Override
	public boolean canLaunch(Player pl, Location loc) {
		return CoreUtil.isWithinArena(pl, loc);
	}

	@Override
	public void onLaunch(Projectile proj, Player shooter) {
		CompSound.FIREWORK_LARGE_BLAST2.play(shooter, 1F, 1F);
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
		CompSound.FIREWORK_BLAST.play(shooter, 1F, 0.1F);
	}
}
