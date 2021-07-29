package org.mineacademy.game.impl;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.game.model.ArenaSign;
import org.mineacademy.game.util.Constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public final class SimpleSignPower extends SimpleSign {

	private final SignType type = SignType.POWER;

	private final PowerType power;

	public SimpleSignPower(PowerType power, String arena, Sign sign) {
		super(sign, arena);

		this.power = power;
	}

	public void onLobbyStart() {
		if (power == PowerType.LOBBY)
			power();
	}

	public void onArenaStart() {
		if (power == PowerType.START)
			power();
	}

	public void onNextPhase() {
		if (power == PowerType.PHASE)
			power();
	}

	public void onArenaEnd() {
		if (power == PowerType.END)
			power();
	}

	private void power() {
		getLocation().getBlock().setType(Material.REDSTONE_BLOCK);

		Common.runLater(5, new BukkitRunnable() {

			@Override
			public void run() {
				getLocation().getBlock().setType(CompMaterial.OAK_WALL_SIGN.getMaterial());
				Valid.checkBoolean(getLocation().getBlock().getState() instanceof Sign, "Power sign failed to reset. Ended up with " + getLocation().getBlock().getType());

				setSign((Sign) getLocation().getBlock().getState());

				updateState();
			}
		});
	}

	@Override
	public void onSignInGameClick(Player player) {
	}

	@Override
	public void onSignSetupClick(Player player) {
	}

	@Override
	public void onSignOutGameClick(Player player) {
	}

	@Override
	protected String replaceVariables(String line) {
		return line.replace("{type}", power.getKey());
	}

	@Override
	protected String[] getFormatting() {
		return Constants.Symbols.POWER_SIGN_FORMAT;
	}

	@Override
	public SerializedMap serialize() {
		final SerializedMap map = super.serialize();

		map.put("type", power.getKey());

		return map;
	}

	public static ArenaSign deserialize(String arenaName, SerializedMap map) {
		final Sign sign = deserializeSign(map);
		final PowerType power = PowerType.fromLine(map.getString("type"));

		return new SimpleSignPower(power, arenaName, sign);
	}

	@RequiredArgsConstructor
	public enum PowerType {
		LOBBY("Lobby"),
		START("Start"),
		PHASE("Phase"),
		END("End");

		@Getter
		private final String key;

		public static PowerType fromLine(String line) {
			for (final PowerType sign : values())
				if (sign.key.equalsIgnoreCase(line))
					return sign;

			return null;
		}
	}
}
