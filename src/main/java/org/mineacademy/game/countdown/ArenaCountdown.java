package org.mineacademy.game.countdown;

import org.mineacademy.fo.model.Countdown;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.game.model.Arena;
import org.mineacademy.game.model.ArenaMessenger;
import org.mineacademy.game.settings.Localization;

import lombok.AccessLevel;
import lombok.Getter;

@Getter(AccessLevel.PROTECTED)
public abstract class ArenaCountdown extends Countdown {

	private final Arena arena;

	protected ArenaCountdown(Arena arena, int seconds) {
		super(seconds);

		this.arena = arena;
	}

	protected void playSoundTimeLeft() {
		final ArenaMessenger messenger = arena.getMessenger();

		messenger.playSound(getTimeLeft() < 11 ? CompSound.NOTE_PIANO : CompSound.NOTE_STICKS, getTimeLeft() < 6 ? 2 : 1);
	}

	protected final void broadcastLeft(String message) {
		arena.getMessenger().broadcast(message.replace("{seconds}", Localization.Cases.SECOND.formatWithCount(getTimeLeft())));
	}
}
