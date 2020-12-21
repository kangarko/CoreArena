package org.mineacademy.game.model;

import org.bukkit.potion.PotionEffect;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Represents the settings section of an {@link ArenaClass} individual tier
 */
@Getter
@Setter
@RequiredArgsConstructor
public final class TierSettings {

	/**
	 * The tier level
	 */
	private final int tier;

	/**
	 * Potion effects that this tier gives
	 */
	private PotionEffect[] potionEffects;

	/**
	 * Permissions that this tier gives
	 */
	private String[] permissionsToGive;
}