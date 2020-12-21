package org.mineacademy.game.impl;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.model.ArenaSign;
import org.mineacademy.game.settings.Localization;
import org.mineacademy.game.settings.Settings;
import org.mineacademy.game.type.LeaveCause;

import lombok.Getter;

@Getter
public final class SimpleSignLeave extends SimpleSign {

	private final SignType type = SignType.LEAVE;

	public SimpleSignLeave(String arena, Sign sign) {
		super(sign, arena);
	}

	@Override
	public void onSignInGameClick(Player player) {
		final Arena arena = getArena();
		final boolean left = arena.kickPlayer(player, LeaveCause.COMMAND);

		if (left)
			arena.getMessenger().tell(player, Localization.Commands.Leave.SUCCESS);
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
		return Settings.Signs.LEAVE_SIGN_FORMAT;
	}

	public static ArenaSign deserialize(String arenaName, SerializedMap map) {
		final Sign sign = deserializeSign(map);

		return new SimpleSignLeave(arenaName, sign);
	}
}
