package org.mineacademy.game.model;

import java.util.List;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.mineacademy.fo.model.SimpleSound;
import org.mineacademy.game.type.NextPhaseMode;

/**
 * Those are user-alterable settings stored in the arenas/ folder.
 */
public interface ArenaSettings {

	/**
	 * Get the arena name.
	 *
	 * @return the arena name
	 */
	String getName();

	/**
	 * Return which phase activates PvP (player may
	 * kill other players, not just mobs)
	 *
	 * @return from which phase friendly fire is enabled
	 */
	int getPvpPhase();

	/**
	 * Get when we should stop the arena if there is less or equals players
	 *
	 * @return see above
	 */
	int getAutoStopPlayersLimit();

	/**
	 * Get the last phase or -1 if not set (arena ends only when time is up).
	 *
	 * @return the last phase, -1 for no end phase
	 */
	int getLastPhase();

	/**
	 * Get the phase after which arena ends automatically when all monsters are killed.
	 *
	 * @return the end phase no monsters, or -1 if feature disabled
	 */
	int getEndPhaseNoMonsters();

	/**
	 * Get the last phase or -1 if not set (arena will still continue to play, just
	 * the phase won't increase anymore after it has reached the limit).
	 *
	 * @return the max phase, -1 for no infinite increase
	 */
	int getMaxPhase();

	/**
	 * When the arena should enter the next phase?
	 *
	 * @return the next phase mode
	 */
	NextPhaseMode getNextPhaseMode();

	/**
	 * Get the warm-up pause from when we can go to the next phase
	 * till we actually launch it, in seconds
	 *
	 * @return the wait period between switching phases, in seconds
	 */
	int getNextPhaseWaitSeconds();

	/**
	 * Return how many times the player may get
	 * killed before they loose and get kicked out of the arena.
	 *
	 * @return lifes per player in the arena
	 */
	int getLifes();

	/**
	 * Get the minimum class tier required to enter
	 *
	 * @return the minimum class tier required to enter
	 */
	int getMinimumTier();

	/**
	 * Get the random distance around the mob spawner to spread mobs out.
	 *
	 * @return the random distance around the mob spawner to spread mobs out
	 */
	int getMobSpread();

	/**
	 * Return whether or not the classes are completely
	 * disabled in this arena and players may use their
	 * own equipment from their gameplay.
	 *
	 * @return if arena allows joining with own equipment
	 */
	boolean allowOwnEquipment();

	/**
	 * Return whether or not the mobs should drop
	 * their natural death items on death?
	 *
	 * @return if arena allows mobs natural drops
	 */
	boolean allowNaturalDrops();

	/**
	 * Should players be teleported to a random spawnpoint, or to the first one
	 * from when the arena started?
	 *
	 * @return whether respawning should teleport to random spawnpoints
	 */
	boolean isRespawningRandom();

	/**
	 * Get if monsters should burn on the sunlight.
	 * Default: false
	 *
	 * @return if monsters should burn
	 */
	boolean allowMonstersBurn();

	/**
	 * Should right clicking with bones spawn wolves?
	 *
	 * @return if right clicking with bones spawn wolves
	 */
	boolean spawnWolves();

	/**
	 * Should right clicking with fireballs launch them?
	 *
	 * @return if right clicking with fireballs launch them?
	 */
	boolean launchFireballs();

	/**
	 * Should placing tnt ignite it?
	 *
	 * @return if placing tnt ignites it
	 */
	boolean igniteTnts();

	/**
	 * Shall we open class selection menu right after joining the lobby?
	 */
	boolean openClassMenu();

	/**
	 * Shall explosive arrows damage players?
	 */
	boolean explosiveArrowPlayerDamage();

	/**
	 * Should we apply initial/damaged snapshots?
	 *
	 * @return
	 */
	boolean hasProceduralDamage();

	/**
	 * Return the maximum players in the arena
	 *
	 * @return the maximum players in the arena
	 */
	int getMinimumPlayers();

	/**
	 * Return the minimum players in the arena
	 *
	 * @return the minimum players in the arena
	 */
	int getMaximumPlayers();

	/**
	 * Return the maximum monsters in the arena
	 *
	 * @return the maximum monsters in the arena
	 */
	int getMobLimit();

	/**
	 * Get the lobby duration, in seconds
	 *
	 * @return the lobby duration, in seconds
	 */
	int getLobbyDurationSeconds();

	/**
	 * Get the arena duration, in seconds
	 *
	 * @return the arena duration, in seconds
	 */
	int getArenaDurationSeconds();

	/**
	 * Get the phase duration, in seconds
	 *
	 * @return the phase duration, in seconds
	 */
	int getPhaseDurationSeconds();

	/**
	 * Get an implementation of when chests should be refilled.
	 *
	 * @return the chest refill trigger
	 */
	ArenaTrigger getChestRefill();

	/**
	 * Get material allower for things that can be broken.
	 *
	 * @return a list of things that can be broken
	 */
	ArenaMaterialAllower getBreakingList();

	/**
	 * Get material allower for things that can be placed.
	 *
	 * @return a list of things that can be placed
	 */
	ArenaMaterialAllower getPlaceList();

	/**
	 * Get items that always take damage regardless of Arena.Auto_Repair in settings.yml
	 *
	 * @return items that always take damage
	 */
	ArenaMaterialAllower getRepairBlacklist();

	/**
	 * Get the commands to be run when arena starts.
	 *
	 * @return the commands.
	 */
	ArenaCommands getStartCommands();

	/**
	 * Get the commands to be run when the first player joins the lobby and starts countdown
	 * that starts the arena
	 *
	 * @return
	 */
	ArenaCommands getLobbyStartCommands();

	/**
	 * Get the commands to be run on the next phase.
	 *
	 * @return the commands.
	 */
	ArenaCommands getPhaseCommands();

	/**
	 * Get the commands to only be run when arena ends gracefully.
	 *
	 * @return the commands.
	 */
	ArenaCommands getFinishCommands();

	/**
	 * Get the commands to be run when arena ends for whatever reason.
	 *
	 * @return the commands.
	 */
	ArenaCommands getEndCommands();

	/**
	 * Get the commands to be run when a player leaves for whatever reason.
	 *
	 * @return the commands.
	 */
	ArenaCommands getPlayerLeaveCommands();

	/**
	 * Checks the phase experience formula for this arena for how much exp to award<br>
	 * First checks if there are any specific settings for the arena,
	 * and returns the global setting if none is set.
	 *
	 * @param currentPhase the phase the arena is in.
	 * @return experience.
	 */
	int getPhaseExp(int currentPhase);

	/**
	 * Checks the phase experience formula for this entity in this arena for how much exp to award<br>
	 * First checks if there are any specific settings for the arena,
	 * and returns the global setting if none is set.
	 *
	 * @param entity entity to lookup
	 * @param currentPhase the phase the arena is in.
	 * @return experience formula.
	 */
	int getExpFor(Entity entity, int currentPhase);

	/**
	 * Get the internal data section
	 *
	 * @return the internal data section
	 */
	ArenaData getDataSection();

	/**
	 * Delete the settings file.
	 *
	 * This keeps arena registered, please use method in arena manager to remove arena!
	 */
	void removeSettingsFile();

	/**
	 * Get the y-height for which players are killed.
	 * Only works if the plugin supports it (for ex. Puncher)
	 *
	 * @return the kill height
	 */
	int getKillHeight();

	Map<Integer /*wave*/, List<Object> /**/> getRewardsOnWave();

	Map<Integer /*wave*/, List<Object> /**/> getRewardsEveryWave();

	SimpleSound getPlayerJoinSound();

	SimpleSound getPlayerLeaveSound();

	SimpleSound getArenaStartSound();

	int getSpawnerActivationRadius();
}
