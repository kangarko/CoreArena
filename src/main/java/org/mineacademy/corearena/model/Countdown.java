package org.mineacademy.corearena.model;

import java.util.Set;

import org.bukkit.plugin.Plugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.model.Task;
import org.mineacademy.fo.platform.Platform;

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
	 * Moments when the countdown ticks. By default, it ticks
	 * when there are 720, 360, 180, 60, 30, 20, 10, 8, 6, 5, 4, 3, 2, 1 and 1/2 of the duration seconds left.
	 *
	 * You can modify it by {@link #setMoments(Set)} or {@link #getMoments()} and then editing it.
	 */
	@Getter
	@Setter
	private Set<Integer> moments = Sets.newHashSet(this.getDuration() / 2, 720, 360, 180, 60, 30, 20, 10, 8, 6, 5, 4, 3, 2, 1);

	/**
	 * Seconds since the beginning
	 */
	@Getter
	private int secondsSinceStart = 0;

	/**
	 * The internal task
	 */
	private Task task = null;

	/**
	 * Make a new countdown for a plugin with a certain duration
	 *
	 * @param plugin the plugin
	 * @param duration the duration, in seconds
	 */
	protected Countdown(@Deprecated Plugin plugin, int duration) {
		this.duration = duration;
	}

	@Override
	public final void run() {
		this.secondsSinceStart++;

		if (this.secondsSinceStart < this.duration)
			try {
				this.onTick();

				if (this.moments.contains(this.getTimeLeft()))
					this.onTickIn();
				else
					this.onTickOut();

			} catch (final Throwable t) {
				Common.error(t,
						"Error in countdown!",
						"Seconds since start: " + this.secondsSinceStart,
						"Counting till: " + this.duration + " seconds");

			}
		else {
			this.cancel();

			this.onEnd();
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
	public final void launch() {
		Valid.checkBoolean(!this.isRunning(), "Task " + this + " already scheduled!");

		this.task = Platform.runTaskTimer(0, PERIOD_TICKS, this);
	}

	/**
	 * Cancels this countdown. You can start it again later.
	 */
	public final void cancel() {
		this.task.cancel();

		this.task = null;
		this.secondsSinceStart = 0;
	}

	/**
	 * Get time left
	 *
	 * @return the time left, in seconds
	 */
	public int getTimeLeft() {
		return this.duration - this.secondsSinceStart;
	}

	/**
	 * Return if the countdown is running
	 *
	 * @return if the countdown is running
	 */
	public final boolean isRunning() {
		return this.task != null;
	}

	@Override
	public final String toString() {
		return this.getClass().getSimpleName() + "{" + this.duration + ", task=" + (isRunning() ? this.task.getTaskId() : "not running") + "}";
	}
}
