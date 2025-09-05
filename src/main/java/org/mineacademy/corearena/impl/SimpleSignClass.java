package org.mineacademy.corearena.impl;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.data.AllData.ArenaPlayer;
import org.mineacademy.corearena.exception.IllegalSignException;
import org.mineacademy.corearena.menu.IndividualClassMenu;
import org.mineacademy.corearena.model.ArenaClass;
import org.mineacademy.corearena.model.ArenaSign;
import org.mineacademy.corearena.settings.Settings;
import org.mineacademy.corearena.type.ArenaState;
import org.mineacademy.corearena.type.TierMode;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.fo.settings.Lang;

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
		if (this.getArena().getState() == ArenaState.LOBBY)
			this.giveClass(player);
	}

	@Override
	public void onSignSetupClick(Player player) {
		new IndividualClassMenu(this.clazz, false).displayTo(player);
	}

	@Override
	public void onSignOutGameClick(Player player) {
	}

	private final void giveClass(Player player) {
		final ArenaPlayer data = CoreArenaPlugin.getDataFor(player);
		final int tier = data.getTierOf(this.clazz);

		if (!this.clazz.mayObtain(player)) {
			Messenger.error(player, Lang.component("class-no-permission", "class", this.clazz.getName()));

			CompSound.BLOCK_NOTE_BLOCK_BASS.play(player, 1F, (float) Math.random());
			return;
		}

		if (tier < this.getArena().getSettings().getMinimumTier()) {
			Messenger.error(player, Lang.component("arena-lobby-tier-too-low", "classTier", "" + tier, "arenaTier", "" + this.getArena().getSettings().getMinimumTier()));

			CompSound.BLOCK_NOTE_BLOCK_BASS.play(player, 1F, (float) Math.random());
			return;
		}

		// Do not re-give if has, save resources
		if (data.getClassCache().assignedClass != null && data.getClassCache().assignedClass.equals(this.clazz))
			return;

		this.clazz.giveToPlayer(player, TierMode.PREVIEW, true);
	}

	@Override
	protected final String replaceVariables(String line) {
		return line.replace("{class}", this.clazz.getName());
	}

	@Override
	protected final String[] getFormatting() {
		return Common.toArray(Settings.Signs.CLASS_SIGN_FORMAT);
	}

	@Override
	public SerializedMap serialize() {
		final SerializedMap map = super.serialize();

		map.put("class", this.clazz.getName());

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
