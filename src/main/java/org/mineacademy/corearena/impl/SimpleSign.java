package org.mineacademy.corearena.impl;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.exception.IllegalSignException;
import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.model.ArenaSign;
import org.mineacademy.fo.SerializeUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.model.CompChatColor;

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
		return this.sign.getLocation();
	}

	protected abstract String[] getFormatting();

	protected abstract String replaceVariables(String line);

	@Override
	public void updateState() {
		for (int i = 0; i < this.getSign().getLines().length; i++)
			if (this.getFormatting().length > i) {
				String line = this.getFormatting()[i];

				if (this.arenaName != null) {
					line = this.getArena().getMessenger().replaceVariables(line);
					line = this.replaceVariables(line);
				}

				this.getSign().setLine(i, CompChatColor.translateColorCodes(line));
			}

		final boolean success = this.getSign().update(true, false);
		Valid.checkBoolean(success, "Arena " + this.getType() + " sign at " + SerializeUtil.serializeLocation(this.getLocation()) + " could not be updated");
	}

	@Override
	public void removeSign() {
		this.sign.getBlock().setType(Material.AIR);
		this.getArena().getData().removeSign(this);
	}

	@Override
	public Arena getArena() {
		Valid.checkNotNull(this.arenaName, "Arena name not set!");

		final Arena arena = CoreArenaPlugin.getArenaManager().findArena(this.arenaName);
		Valid.checkNotNull(arena, "Arena " + this.arenaName + " could not be found!");

		return arena;
	}

	@Override
	public SerializedMap serialize() {
		final SerializedMap map = new SerializedMap();

		map.put("location", this.sign.getLocation());

		return map;
	}

	protected static final Sign deserializeSign(SerializedMap map) {
		final Location loc = map.get("location", Location.class);
		final BlockState state = loc != null ? loc.getBlock().getState() : null;

		// Validate
		if (state == null || !(state instanceof Sign))
			throw new IllegalSignException("The sign at " + (loc == null ? "unknown" : SerializeUtil.serializeLocation(loc)) + " is no longer a valid sign! Removing..");

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
