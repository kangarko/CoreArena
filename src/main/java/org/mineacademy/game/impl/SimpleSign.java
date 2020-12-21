package org.mineacademy.game.impl;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.game.exception.IllegalSignException;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.model.ArenaSign;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class SimpleSign implements ArenaSign {

	@Getter(AccessLevel.PROTECTED)
	@Setter(AccessLevel.PROTECTED)
	private Sign sign;

	@Getter(AccessLevel.PROTECTED)
	private final String arenaName;

	@Override
	public final Location getLocation() {
		return sign.getLocation();
	}

	protected abstract String[] getFormatting();

	protected abstract String replaceVariables(String line);

	@Override
	public void updateState() {
		for (int i = 0; i < getSign().getLines().length; i++)
			if (getFormatting().length > i) {
				String line = getFormatting()[i];

				if (arenaName != null) {
					line = getArena().getMessenger().replaceVariables(line);
					line = replaceVariables(line);
				}

				getSign().setLine(i, Common.colorize(line));
			}

		final boolean success = getSign().update(true, false);
		Valid.checkBoolean(success, "Arena " + getType() + " sign at " + Common.shortLocation(getLocation()) + " could not be updated");
	}

	@Override
	public void removeSign() {
		sign.getBlock().setType(Material.AIR);
		getArena().getData().removeSign(this);
	}

	@Override
	public Arena getArena() {
		Valid.checkNotNull(arenaName, "Arena name not set!");

		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(arenaName);
		Valid.checkNotNull(arena, "Arena " + arenaName + " could not be found!");

		return arena;
	}

	@Override
	public SerializedMap serialize() {
		final SerializedMap map = new SerializedMap();

		map.put("location", sign.getLocation());

		return map;
	}

	protected static final Sign deserializeSign(SerializedMap map) {
		final Location loc = map.getLocation("location");
		final BlockState state = loc != null ? loc.getBlock().getState() : null;

		// Validate
		if (state == null || !(state instanceof Sign))
			throw new IllegalSignException("The sign at " + (loc == null ? "unknown" : Common.shortLocation(loc)) + " is no longer a valid sign! Removing..");

		return (Sign) state;
	}

	public static final ArenaSign deserialize(SerializedMap map, SignType type, String arenaName) throws IllegalSignException {
		switch (type) {
			case JOIN:
				return SimpleSignJoin.deserialize(arenaName, map);

			case LEAVE:
				return SimpleSignLeave.deserialize(arenaName, map);

			case CLASS:
				return SimpleSignClass.deserialize(arenaName, map);

			case UPGRADE:
				return SimpleSignUpgrade.deserialize(arenaName, map);

			case POWER:
				return SimpleSignPower.deserialize(arenaName, map);

			default:
				throw new FoException("Unhandled deserialization of sign " + type);
		}
	}
}
