package org.mineacademy.corearena.impl;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.model.ArenaSign;
import org.mineacademy.corearena.settings.Settings;
import org.mineacademy.corearena.type.LeaveCause;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.settings.Lang;

import lombok.Getter;

@Getter
public final class SimpleSignLeave extends SimpleSign {

	private final SignType type = SignType.LEAVE;

	public SimpleSignLeave(String arena, Sign sign) {
		super(sign, arena);
	}

	@Override
	public void onSignInGameClick(Player player) {
		final Arena arena = this.getArena();
		final boolean left = arena.kickPlayer(player, LeaveCause.COMMAND);

		if (left)
			arena.getMessenger().tell(player, Lang.component("command-leave-success"));
	}

	@Override
	public void onSignSetupClick(Player player) {
	}

	@Override
	public void onSignOutGameClick(Player player) {
	}

	@Override
	protected String replaceVariables(String line) {
		return line;
	}

	@Override
	protected String[] getFormatting() {
		return Common.toArray(Settings.Signs.LEAVE_SIGN_FORMAT);
	}

	public static ArenaSign deserialize(String arenaName, SerializedMap map) {
		final Sign sign = deserializeSign(map);

		return new SimpleSignLeave(arenaName, sign);
	}
}
