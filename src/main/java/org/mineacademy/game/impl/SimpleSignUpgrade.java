package org.mineacademy.game.impl;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.remain.CompParticle;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.game.exception.IllegalSignException;
import org.mineacademy.game.impl.ArenaPlayer.InArenaCache;
import org.mineacademy.game.menu.IndividualUpgradeMenu;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.model.ArenaSign;
import org.mineacademy.game.model.Upgrade;
import org.mineacademy.game.settings.Localization;
import org.mineacademy.game.settings.Settings;
import org.mineacademy.game.type.ArenaState;

import lombok.Getter;

@Getter
public class SimpleSignUpgrade extends SimpleSign {

	private final SignType type = SignType.UPGRADE;

	private final Upgrade upgrade;

	private final int levelCost;

	public SimpleSignUpgrade(String arena, Upgrade upgrade, int levelCost, Sign sign) {
		super(sign, arena);

		this.upgrade = upgrade;
		if (upgrade == null)
			throw new IllegalSignException("The sign refers to a non-existing upgrade!");

		this.levelCost = levelCost;
	}

	@Override
	public void onSignInGameClick(Player player) {
		if (getArena().getState() != ArenaState.RUNNING)
			return;

		if (getArena().getPhase().getCurrent() < upgrade.getUnlockPhase()) {
			Common.tell(player, Localization.Upgrades.LOCKED.replace("{phase}", "" + upgrade.getUnlockPhase()));
			return;
		}

		final InArenaCache cache = CoreArenaPlugin.getDataFor(player).getArenaCache();

		if (cache.getLevel() < levelCost) {
			Common.tell(player, Localization.Upgrades.LACK_LEVELS.replace("{levels}", Localization.Cases.LEVEL.formatWithCount(levelCost - cache.getLevel())));
			return;
		}

		upgrade.giveToPlayer(player);
		cache.takeAndShowLevels(player, levelCost);

		{ // Decorate
			final Location loc = player.getLocation().clone().toVector().add(player.getEyeLocation().getDirection().normalize().multiply(1.1).setY(1.5)).toLocation(player.getWorld());

			for (int i = -1; i < 1; i++)
				for (int j = -1; j < 1; j++)
					CompParticle.VILLAGER_HAPPY.spawn(loc.clone().add(i * Math.random(), 0, 0.5 + j * Math.random()));
		}

		getArena().getMessenger().playSound(player, CompSound.LEVEL_UP, 0.1F);
		Common.tell(player, Localization.Upgrades.SUCCESSFUL_PURCHASE.replace("{upgrade}", upgrade.getName()).replace("{levels}", Localization.Cases.LEVEL.formatWithCount(levelCost)));
	}

	@Override
	public void onSignSetupClick(Player player) {
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(getLocation());

		if (arena == null) {
			Common.tell(player, Localization.Signs.REMOVED_OUTSIDE_SIGN);

			removeSign();

		} else
			new IndividualUpgradeMenu(upgrade, false).displayTo(player);
	}

	@Override
	public void onSignOutGameClick(Player player) {
	}

	@Override
	protected final String replaceVariables(String line) {
		return line.replace("{upgrade}", upgrade.getName()).replace("{price}", Localization.Cases.LEVEL.formatWithCount(levelCost));
	}

	@Override
	protected final String[] getFormatting() {
		return Settings.Signs.UPGRADES_SIGN_FORMAT;
	}

	@Override
	public SerializedMap serialize() {
		final SerializedMap map = super.serialize();

		map.put("upgrade", upgrade.getName());
		map.put("levelCost", levelCost);

		return map;
	}

	public static final ArenaSign deserialize(String arenaName, SerializedMap map) {
		final Sign sign = deserializeSign(map);
		final String upgradeName = map.getString("upgrade");

		final Upgrade upgrade = CoreArenaPlugin.getUpgradesManager().findUpgrade(upgradeName);
		if (upgrade == null)
			throw new IllegalSignException("The upgrade sign holds a non-existing upgrade '" + upgradeName + "'. Removing..");

		final int levelCost = map.getInteger("levelCost");

		return new SimpleSignUpgrade(arenaName, upgrade, levelCost, sign);
	}
}
