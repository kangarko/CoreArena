package org.mineacademy.corearena.impl;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.data.AllData.ArenaPlayer.InArenaCache;
import org.mineacademy.corearena.exception.IllegalSignException;
import org.mineacademy.corearena.menu.IndividualUpgradeMenu;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.model.ArenaSign;
import org.mineacademy.corearena.model.Upgrade;
import org.mineacademy.corearena.settings.Settings;
import org.mineacademy.corearena.type.ArenaState;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.remain.CompParticle;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.fo.settings.Lang;

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
		if (this.getArena().getState() != ArenaState.RUNNING)
			return;

		if (this.getArena().getPhase().getCurrent() < this.upgrade.getUnlockPhase()) {
			Messenger.error(player, Lang.component("upgrade-locked", "phase", String.valueOf(this.upgrade.getUnlockPhase())));

			return;
		}

		final InArenaCache cache = CoreArenaPlugin.getDataFor(player).getArenaCache();

		if (cache.getLevel() < this.levelCost) {
			Messenger.error(player, Lang.component("upgrade-lack-levels", "levels", Lang.numberFormat("case-level", this.levelCost - cache.getLevel())));

			return;
		}

		this.upgrade.giveToPlayer(player);
		cache.takeAndShowLevels(player, this.levelCost);

		{ // Decorate
			final Location loc = player.getLocation().clone().toVector().add(player.getEyeLocation().getDirection().normalize().multiply(1.1).setY(1.5)).toLocation(player.getWorld());

			for (int i = -1; i < 1; i++)
				for (int j = -1; j < 1; j++)
					CompParticle.VILLAGER_HAPPY.spawn(loc.clone().add(i * Math.random(), 0, 0.5 + j * Math.random()));
		}

		this.getArena().getMessenger().playSound(player, CompSound.ENTITY_PLAYER_LEVELUP, 0.1F);
		Lang.component("upgrade-successful-purchase", "upgrade", this.upgrade.getName(), "levels", Lang.numberFormat("case-level", this.levelCost));
	}

	@Override
	public void onSignSetupClick(Player player) {
		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(this.getLocation());

		if (arena == null) {
			Messenger.error(player, Lang.component("sign-removed-outside-sign"));

			this.removeSign();

		} else
			new IndividualUpgradeMenu(this.upgrade, false).displayTo(player);
	}

	@Override
	public void onSignOutGameClick(Player player) {
	}

	@Override
	protected final String replaceVariables(String line) {
		return line.replace("{upgrade}", this.upgrade.getName()).replace("{price}", Lang.numberFormat("case-level", this.levelCost));
	}

	@Override
	protected final String[] getFormatting() {
		return Common.toArray(Settings.Signs.UPGRADES_SIGN_FORMAT);
	}

	@Override
	public SerializedMap serialize() {
		final SerializedMap map = super.serialize();

		map.put("upgrade", this.upgrade.getName());
		map.put("levelCost", this.levelCost);

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
