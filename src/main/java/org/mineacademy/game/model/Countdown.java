package org.mineacademy.game.model;

import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.mineacademy.fo.Common;

import com.google.common.collect.Sets;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a simple countdown you can use to end your arenas
 * or lobbies.
 */
public abstract class Countdown implements Runnable {

	/**
	 * This countdown ticks each one seconds - 20 ticks.
	 */
	private static final int PERIOD_TICKS = 20;

	/**
	 * The duration of the countdown
	 */
	@Getter
	private final int duration;

	/**
	 * The plugin that owns this countdown.
	 */
	private final Plugin plugin;

	/**
	 * Moments when the countdown ticks. By default, it ticks
	 * when there are 720, 360, 180, 60, 30, 20, 10, 8, 6, 5, 4, 3, 2, 1 and 1/2 of the duration seconds left.
	 *
	 * You can modify it by {@link #setMoments(Set)} or {@link #getMoments()} and then editing it.
	 */
	@Getter
	@Setter
	private Set<Integer> moments = Sets.newHashSet(getDuration() / 2, 720, 360, 180, 60, 30, 20, 10, 8, 6, 5, 4, 3, 2, 1);

	/**
	 * Seconds since the beginning
	 */
	@Getter
	private int secondsSinceStart = 0;

	/**
	 * The internal task id
	 */
	private int taskId = -1;

	/**
	 * Make a new countdown for a plugin with a certain duration
	 *
	 * @param plugin the plugin
	 * @param duration the duration, in seconds
	 */
	protected Countdown(Plugin plugin, int duration) {
		this.plugin = plugin;
		this.duration = duration;
	}

	@Override
	public final void run() {
		secondsSinceStart++;

		if (secondsSinceStart < duration)
			try {
				onTick();

				if (moments.contains(getTimeLeft()))
					onTickIn();
				else
					onTickOut();

			} catch (final Throwable t) {
				Common.error(t,
						"Error in countdown!",
						"Seconds since start: " + secondsSinceStart,
						"Counting till: " + duration + " seconds");

			}
		else {
			cancel();

			onEnd();
		}
	}

	/**
	 * Called each time this countdown loop ticks.
	 */
	protected abstract void onTick();

	/**
	 * Called only in {@link #moments}
	 */
	protected void onTickIn() {
	}

	/**
	 * Called only out of {@link #moments}
	 */
	protected void onTickOut() {
	}

	/**
	 * Called at the end of this countdown
	 */
	protected abstract void onEnd();

	/**
	 * Starts this countdown. Please make sure it {@link #isRunning()}
	 */
	public synchronized final void launch() {
		Validate.isTrue(!isRunning(), "Task " + this + " already scheduled!");

		final BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, this, 0, PERIOD_TICKS);
		taskId = task.getTaskId();
	}

	/**
	 * Cancels this countdown. You can start it again later.
	 */
	public synchronized final void cancel() {
		Bukkit.getScheduler().cancelTask(getTaskId());

		taskId = -1;
		secondsSinceStart = 0;
	}

	/**
	 * Get time left
	 *
	 * @return the time left, in seconds
	 */
	public int getTimeLeft() {
		return duration - secondsSinceStart;
	}

	/**
	 * Return if the countdown is running
	 *
	 * @return if the countdown is running
	 */
	public final boolean isRunning() {
		return taskId != -1;
	}

	// Get internal task it and verify if it is running
	private final int getTaskId() {
		Validate.isTrue(isRunning(), "Task " + this + " not scheduled yet");

		return taskId;
	}

	@Override
	public final String toString() {
		return getClass().getSimpleName() + "{" + duration + ", id=" + taskId + "}";
	}
}
