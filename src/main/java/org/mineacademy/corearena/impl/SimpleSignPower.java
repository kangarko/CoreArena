package org.mineacademy.corearena.impl;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.mineacademy.corearena.model.ArenaSign;
import org.mineacademy.corearena.util.Constants;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.model.SimpleRunnable;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.remain.CompMaterial;

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
		if (this.power == PowerType.LOBBY)
			this.power();
	}

	public void onArenaStart() {
		if (this.power == PowerType.START)
			this.power();
	}

	public void onNextPhase() {
		if (this.power == PowerType.PHASE)
			this.power();
	}

	public void onArenaEnd() {
		if (this.power == PowerType.END)
			this.power();
	}

	private void power() {
		this.getLocation().getBlock().setType(Material.REDSTONE_BLOCK);

		Platform.runTask(5, new SimpleRunnable() {

			@Override
			public void run() {
				SimpleSignPower.this.getLocation().getBlock().setType(CompMaterial.OAK_WALL_SIGN.getMaterial());
				Valid.checkBoolean(SimpleSignPower.this.getLocation().getBlock().getState() instanceof Sign, "Power sign failed to reset. Ended up with " + SimpleSignPower.this.getLocation().getBlock().getType());

				SimpleSignPower.this.setSign((Sign) SimpleSignPower.this.getLocation().getBlock().getState());

				SimpleSignPower.this.updateState();
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
		return line.replace("{type}", this.power.getKey());
	}

	@Override
	protected String[] getFormatting() {
		return Constants.Symbols.POWER_SIGN_FORMAT;
	}

	@Override
	public SerializedMap serialize() {
		final SerializedMap map = super.serialize();

		map.put("type", this.power.getKey());

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
