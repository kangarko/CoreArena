package org.mineacademy.corearena.util;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.mineacademy.corearena.settings.Settings;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.debug.Debugger;
import org.mineacademy.fo.model.ConfigSerializable;
import org.mineacademy.fo.remain.CompAttribute;
import org.mineacademy.fo.remain.CompProperty;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.fo.settings.YamlConfig;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * You can use this simple class to save and restore players' inventories.
 */
@AutoRegister
public final class InventoryStorageUtil extends YamlConfig {

	private static final InventoryStorageUtil instance = new InventoryStorageUtil();

	private Map<UUID, StoredInventory> inventories;

	private InventoryStorageUtil() {
		this.setHeader(
				"DO NOT EDIT",
				"",
				"Stores player inventories during the time they play in the arena.");

		this.loadAndExtract(NO_DEFAULT, "inventories.yml");
	}

	@Override
	protected void onLoad() {
		this.inventories = this.getMap("Inventories", UUID.class, StoredInventory.class);
	}

	@Override
	protected void onSave() {
		this.set("Inventories", this.inventories);
	}

	/**
	 * Save player experience and level
	 *
	 * @param player the player
	 */
	public void saveExperience(Player player) {
		this.inventories.put(player.getUniqueId(), StoredInventory.fromPlayerExperience(player));

		this.save();
	}

	/**
	 * Saves player inventory in full
	 *
	 * @param player the player
	 */
	public void saveInventory(Player player) {
		this.inventories.put(player.getUniqueId(), StoredInventory.fromPlayer(player));

		this.save();
	}

	/**
	 * Restore player inventory or exp if stored
	 *
	 * @param player
	 */
	public void restoreIfStored(Player player) {
		final StoredInventory stored = this.inventories.get(player.getUniqueId());

		if (stored != null) {
			stored.restoreIfStored(player);

			this.inventories.remove(player.getUniqueId());
			this.save();
		}
	}

	public static InventoryStorageUtil getInstance() {
		synchronized (instance) {
			return instance;
		}
	}
}

/**
 * A helper class representing all data we store for players
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class StoredInventory implements ConfigSerializable {

	/**
	 * Do these data only store player's experience?
	 */
	private boolean justExperience;

	private GameMode gameMode;

	private ItemStack[] content;
	private ItemStack[] armorContent;
	private ItemStack[] extraContent;

	private double maxHealth;
	private double health;
	private boolean healthScaled;

	private int remainingAir;
	private int maximumAir;

	private float fallDistance;
	private int fireTicks;

	private int totalXp;
	private int lvl;
	private float exp;

	private int foodLevel;
	private float exhaustion;
	private float saturation;

	private float flySpeed;
	private float walkSpeed;

	private boolean glowing;
	private boolean invulnerable;
	private boolean silent;

	private Collection<PotionEffect> potionEffects;

	public void restoreIfStored(Player player) {
		if (this.justExperience)
			this.restoreExperience(player); // Only restore experience since we reserve the bar for Nuggets

		else if (Settings.Arena.STORE_INVENTORIES) {
			this.restoreExperience(player);

			this.restore(player);
		}
	}

	public void restoreExperience(Player player) {
		player.setTotalExperience(this.totalXp);
		player.setLevel(this.lvl);
		player.setExp(this.exp);
	}

	public void restore(@NonNull Player player) {
		player.setGameMode(this.gameMode);

		final PlayerInventory inventory = player.getInventory();

		inventory.setArmorContents(this.armorContent);
		inventory.setContents(this.content);

		try {
			inventory.setExtraContents(this.extraContent);
		} catch (final NoSuchMethodError err) {
		}

		Debugger.debug("inventory", "Restoring " + player.getName() + " inventory. Max health: " + this.maxHealth + " vs current: " + this.health);

		CompAttribute.MAX_HEALTH.set(player, this.maxHealth);

		try {
			player.setHealthScaled(this.healthScaled);
			player.setHealth(this.health);

		} catch (final IllegalArgumentException ex) {
			Common.error(ex,
					"Error restoring health for " + player.getName(),
					"",
					"Current health: " + player.getHealth(),
					"Current max health: " + player.getMaxHealth(),
					"Stored health: " + this.health,
					"Stored max health: " + this.maxHealth,
					"Error: {error}");
		}

		player.setRemainingAir(this.remainingAir);
		player.setMaximumAir(this.maximumAir);
		player.setFallDistance(this.fallDistance);
		player.setFireTicks(this.fireTicks);

		player.setFoodLevel(this.foodLevel);
		player.setExhaustion(this.exhaustion);
		player.setSaturation(this.saturation);

		player.setFlySpeed(this.flySpeed);
		player.setWalkSpeed(this.walkSpeed);

		CompProperty.GLOWING.apply(player, this.glowing);
		CompProperty.INVULNERABLE.apply(player, this.invulnerable);
		CompProperty.SILENT.apply(player, this.silent);

		// First clear active effects
		for (final PotionEffect effect : player.getActivePotionEffects())
			player.removePotionEffect(effect.getType());

		for (final PotionEffect effect : this.potionEffects)
			player.addPotionEffect(effect);

		if (!player.isOnline())
			player.saveData();
	}

	@Override
	public SerializedMap serialize() {
		return SerializedMap.fromArray(
				"Just_Experience", this.justExperience,
				"Gamemode", this.gameMode,
				"Content", this.content,
				"Armor_Content", this.armorContent,
				"Extra_Content", this.extraContent,
				"Max_Health", this.maxHealth,
				"Health", this.health,
				"Health_Scaled", this.healthScaled,
				"Remaining_Air", this.remainingAir,
				"Maximum_Air", this.maximumAir,
				"Fall_Distance", this.fallDistance,
				"Fire_Ticks", this.fireTicks,
				"Total_XP", this.totalXp,
				"Level", this.lvl,
				"Experience", this.exp,
				"Food_Level", this.foodLevel,
				"Exhaustion", this.exhaustion,
				"Saturation", this.saturation,
				"Fly_Speed", this.flySpeed,
				"Walk_Speed", this.walkSpeed,
				"Glowing", this.glowing,
				"Invulnerable", this.invulnerable,
				"Silent", this.silent,
				"Potion_Effects", this.potionEffects);
	}

	public static StoredInventory fromPlayerExperience(Player player) {
		final StoredInventory stored = new StoredInventory();

		stored.justExperience = true;
		stored.exp = player.getExp();
		stored.lvl = player.getLevel();
		stored.totalXp = player.getTotalExperience();

		return stored;
	}

	public static StoredInventory fromPlayer(Player player) {
		final StoredInventory stored = new StoredInventory();

		stored.justExperience = false;
		stored.gameMode = player.getGameMode();
		stored.content = player.getInventory().getContents();
		stored.armorContent = player.getInventory().getArmorContents();

		try {
			stored.extraContent = player.getInventory().getExtraContents();
		} catch (final NoSuchMethodError err) {
		}

		stored.maxHealth = Remain.getMaxHealth(player);
		stored.health = Remain.getHealth(player);
		stored.healthScaled = player.isHealthScaled();

		Debugger.debug("inventory", "Saving " + player.getName() + " inventory. Max health: " + stored.maxHealth + " vs current: " + stored.health);

		stored.remainingAir = player.getRemainingAir();
		stored.maximumAir = player.getMaximumAir();
		stored.fallDistance = player.getFallDistance();
		stored.fireTicks = player.getFireTicks();
		stored.totalXp = player.getTotalExperience();
		stored.lvl = player.getLevel();
		stored.exp = player.getExp();
		stored.foodLevel = player.getFoodLevel();
		stored.exhaustion = player.getExhaustion();
		stored.saturation = player.getSaturation();
		stored.flySpeed = player.getFlySpeed();
		stored.walkSpeed = player.getWalkSpeed();

		try {
			stored.glowing = player.isGlowing();
			stored.invulnerable = player.isInvulnerable();
			stored.silent = player.isSilent();
		} catch (final NoSuchMethodError err) {
		}

		stored.potionEffects = player.getActivePotionEffects();

		return stored;
	}

	public static StoredInventory deserialize(SerializedMap map) {
		final StoredInventory stored = new StoredInventory();

		stored.justExperience = map.getBoolean("Just_Experience");
		stored.gameMode = map.get("Gamemode", GameMode.class);
		stored.content = map.get("Content", ItemStack[].class);
		stored.armorContent = map.get("Armor_Content", ItemStack[].class);
		stored.extraContent = map.get("Extra_Content", ItemStack[].class);
		stored.maxHealth = map.getDouble("Max_Health");
		stored.health = map.getDouble("Health");
		stored.healthScaled = map.getBoolean("Health_Scaled");
		stored.remainingAir = map.getInteger("Remaining_Air");
		stored.maximumAir = map.getInteger("Maximum_Air");
		stored.fallDistance = map.getFloat("Fall_Distance");
		stored.fireTicks = map.getInteger("Fire_Ticks");
		stored.totalXp = map.getInteger("Total_XP");
		stored.lvl = map.getInteger("Level");
		stored.exp = map.getFloat("Experience");
		stored.foodLevel = map.getInteger("Food_Level");
		stored.exhaustion = map.getFloat("Exhaustion");
		stored.saturation = map.getFloat("Saturation");
		stored.flySpeed = map.getFloat("Fly_Speed");
		stored.walkSpeed = map.getFloat("Walk_Speed");
		stored.glowing = map.getBoolean("Glowing");
		stored.invulnerable = map.getBoolean("Invulnerable");
		stored.silent = map.getBoolean("Silent");
		stored.potionEffects = map.getList("Potion_Effects", PotionEffect.class);

		return stored;
	}
}