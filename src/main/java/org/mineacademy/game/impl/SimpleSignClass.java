package org.mineacademy.game.impl;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.game.exception.IllegalSignException;
import org.mineacademy.game.menu.IndividualClassMenu;
import org.mineacademy.game.model.ArenaClass;
import org.mineacademy.game.model.ArenaSign;
import org.mineacademy.game.settings.Localization;
import org.mineacademy.game.settings.Settings;
import org.mineacademy.game.type.ArenaState;
import org.mineacademy.game.type.TierMode;

import lombok.Getter;

@Getter
public class SimpleSignClass extends SimpleSign {

	private final SignType type = SignType.CLASS;

	private final ArenaClass clazz;

	public SimpleSignClass(String arena, ArenaClass clazz, Sign sign) {
		super(sign, arena);

		this.clazz = clazz;
		if (clazz == null)
			throw new IllegalSignException("The sign refers to a non-existing arena class!");
	}

	@Override
	public void onSignInGameClick(Player player) {
		if (getArena().getState() == ArenaState.LOBBY)
			giveClass(player);
	}

	@Override
	public void onSignSetupClick(Player player) {
		new IndividualClassMenu(clazz, false).displayTo(player);
	}

	@Override
	public void onSignOutGameClick(Player player) {
	}

	private final void giveClass(Player player) {
		final ArenaPlayer data = CoreArenaPlugin.getDataFor(player);
		final int tier = data.getTierOf(clazz);

		if (!clazz.mayObtain(player)) {
			Common.tell(player, Localization.Class.NO_PERMISSION.replace("{class}", clazz.getName()));
			CompSound.NOTE_BASS.play(player, 1F, (float) Math.random());
			return;
		}

		if (tier < getArena().getSettings().getMinimumTier()) {
			Common.tell(player, Localization.Arena.Lobby.TIER_TOO_LOW.replace("{classTier}", "" + tier).replace("{arenaTier}", "" + getArena().getSettings().getMinimumTier()));
			CompSound.NOTE_BASS.play(player, 1F, (float) Math.random());
			return;
		}

		// Do not re-give if has, save resources
		if (data.getClassCache().assignedClass != null && data.getClassCache().assignedClass.equals(clazz))
			return;

		clazz.giveToPlayer(player, TierMode.PREVIEW);

		Common.tell(player, Localization.Class.SELECTED.replace("{class}", clazz.getName()));
		CompSound.ENDERDRAGON_WINGS.play(player, 1F, 1F);
	}

	@Override
	protected final String replaceVariables(String line) {
		return line.replace("{class}", clazz.getName());
	}

	@Override
	protected final String[] getFormatting() {
		return Settings.Signs.CLASS_SIGN_FORMAT;
	}

	@Override
	public SerializedMap serialize() {
		final SerializedMap map = super.serialize();

		map.put("class", clazz.getName());

		return map;
	}

	public static final ArenaSign deserialize(String arenaName, SerializedMap map) {
		final Sign sign = deserializeSign(map);
		final String className = map.getString("class");

		final ArenaClass clazz = CoreArenaPlugin.getClassManager().findClass(className);
		if (clazz == null)
			throw new IllegalSignException("The class sign holds a non-existing class '" + className + "'. Removing..");

		return new SimpleSignClass(arenaName, clazz, sign);
	}
}
