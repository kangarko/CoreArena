package org.mineacademy.game.impl;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.game.event.ArenaPreJoinEvent;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.model.ArenaSign;
import org.mineacademy.game.settings.Settings;
import org.mineacademy.game.type.JoinCause;

import lombok.Getter;

@Getter
public final class SimpleSignJoin extends SimpleSign {

	private final SignType type = SignType.JOIN;

	public SimpleSignJoin(String arena, Sign sign) {
		super(sign, arena);
	}

	@Override
	public void onSignInGameClick(Player player) {
	}

	@Override
	public void onSignSetupClick(Player player) {
	}

	@Override
	public void onSignOutGameClick(Player player) {
		if (!Common.callEvent(new ArenaPreJoinEvent(getArena(), JoinCause.SIGN, player)))
			return;

		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(player);

		if (arena != null && !arena.equals(getArena())) {
			Common.logFramed(false, "&cPlayer " + player.getName() + " accessed sign from " + getArena().getName() + " while being in " + arena.getName() + "!");
			return;
		}

		getArena().joinPlayer(player, JoinCause.SIGN);
	}

	@Override
	protected String replaceVariables(String line) {
		return line;
	}

	@Override
	protected String[] getFormatting() {
		return Settings.Signs.JOIN_SIGN_FORMAT;
	}

	public static ArenaSign deserialize(String arenaName, SerializedMap map) {
		final Sign sign = deserializeSign(map);

		return new SimpleSignJoin(arenaName, sign);
	}
}
