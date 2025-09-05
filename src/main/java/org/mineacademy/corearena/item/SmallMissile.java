package org.mineacademy.corearena.item;

import org.bukkit.Location;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.util.CoreUtil;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.menu.tool.Rocket;
import org.mineacademy.fo.remain.CompItemFlag;
import org.mineacademy.fo.remain.CompMaterial;

import lombok.Getter;

public final class SmallMissile extends Rocket {

	@Getter
	private static final SmallMissile instance = new SmallMissile();

	private SmallMissile() {
		super(Fireball.class, 1F, 2F, true);
	}

	@Override
	public ItemStack getItem() {
		return ItemCreator.from(CompMaterial.SNOWBALL,
				"&bSmall Missile",
				"",
				"&7Click to launch",
				"&7a small missile!")
				.flags(CompItemFlag.HIDE_ENCHANTS)
				.make();
	}

	@Override
	public boolean canLaunch(Player pl, Location loc) {
		return CoreUtil.isWithinArena(pl, loc);
	}

	@Override
	protected boolean canExplode(Projectile projectile, Player shooter) {
		return CoreUtil.isWithinArena(shooter, projectile.getLocation());
	}
}
