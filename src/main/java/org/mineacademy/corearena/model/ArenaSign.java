package org.mineacademy.corearena.model;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.mineacademy.fo.model.ConfigSerializable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents a registered arena sign
 */
public interface ArenaSign extends ConfigSerializable {

	/**
	 * Get the sign type
	 *
	 * @return the sign type
	 */
	SignType getType();

	/**
	 * Get the sign arena
	 *
	 * @return the arena associated with this sign
	 */
	Arena getArena();

	/**
	 * Get the sign location
	 *
	 * @return the sign location
	 */
	Location getLocation();

	/**
	 * Updates the sign with the latest information.
	 *
	 * Typically used to update arena join signs when players join
	 * so the sign always set how many players are joined accurately
	 */
	void updateState();

	/**
	 * Called when the sign is clicked when the player is joined in the arena (lobby or playing)
	 *
	 * @param player the player
	 */
	void onSignOutGameClick(Player player);

	/**
	 * Called when the player clicks the sign but is not joined in the arena (NOT lobby nor playing)
	 *
	 * @param player the player
	 */
	void onSignInGameClick(Player player);

	/**
	 * Called when the arena is clicked by a player editing this arena
	 *
	 * @param player the player
	 */
	void onSignSetupClick(Player player);

	/**
	 * Permanently remove the block and data stored about this sign
	 */
	void removeSign();

	/**
	 * Represents a sign type
	 */
	@RequiredArgsConstructor
	public enum SignType {

		/**
		 * The join sign
		 */
		JOIN("Join"),

		/**
		 * The leave sign
		 */
		LEAVE("Leave"),

		/**
		 * The class sign
		 */
		CLASS("Class"),

		/**
		 * The upgrade sign
		 */
		UPGRADE("Upgrade"),

		/**
		 * The power sign
		 */
		POWER("Power");

		/**
		 * The human readable representation of the sign type
		 */
		@Getter
		private final String key;

		@Override
		public String toString() {
			return this.key;
		}
	}
}
