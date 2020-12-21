package org.mineacademy.game.item;

public class SmallMissile /*extends Rocket */ {

	/*@Getter
	private static final SmallMissile instance = new SmallMissile();
	
	@Override
	public ItemStack getItem() {
		return ItemCreator.of(CompMaterial.SNOWBALL)
				.name("&bSmall Missile")
				.enchant(new SimpleEnchant(Enchantment.DURABILITY, 1))
				.flag(CompItemFlag.HIDE_ENCHANTS)
				.lores(Arrays.asList(
						"",
						"&7Click to launch",
						"&7a small missile!"))
				.build().make();
	}
	
	@Override
	public boolean canLaunch(Player pl, Location loc) {
		return CoreUtil.isWithinArena(pl, loc);
	}
	
	@Override
	public void onLaunch(Projectile proj, Player shooter) {
	}
	
	@Override
	public void onHit(Projectile proj, Player shooter, Location loc) {
		if (CoreUtil.isWithinArena(shooter, loc))
			explode(proj, loc, 2F, true);
	}*/
}
