package org.mineacademy.game.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;

/**
 * Represents which blocks can be placed/destroyed during arena play
 */
public class ArenaMaterialAllower {

	/**
	 * How this allow should work?
	 */
	private final AllowMode mode;

	/**
	 * The list of blocks by their material
	 */
	private final Set<Material> materials;

	/**
	 * Create a new allower by mode, null blocks
	 *
	 * @param mode
	 */
	public ArenaMaterialAllower(AllowMode mode) {
		this(null, mode);
	}

	/**
	 * Create a new specific allower allowing only given materials
	 *
	 * @param materials
	 */
	public ArenaMaterialAllower(Collection<Material> materials) {
		this(materials, AllowMode.SPECIFIC);
	}

	/**
	 * Creates a new allower
	 *
	 * @param materials
	 * @param mode
	 */
	private ArenaMaterialAllower(Collection<Material> materials, AllowMode mode) {
		this.materials = new HashSet<>(materials);
		this.mode = mode;

		if (materials == null)
			Validate.isTrue(mode != AllowMode.SPECIFIC, "Mode cannot be specific when the list is null");
	}

	/**
	 * Is the material allowed to be placed/destroed ?
	 *
	 * @param material the material
	 * @return true if the material is allowed to be manipulated
	 */
	public boolean isAllowed(Material material) {
		if (mode == AllowMode.NONE)
			return false;

		if (mode == AllowMode.ALL)
			return true;

		return materials.contains(material);
	}

	/**
	 * Represents how this allower should function.
	 */
	public enum AllowMode {

		/**
		 * We allow all blocks to be manipulated
		 */
		ALL,

		/**
		 * We allow no blocks to be manipulated
		 */
		NONE,

		/**
		 * We only allow configured block list to be manipulated
		 */
		SPECIFIC
	}
}
