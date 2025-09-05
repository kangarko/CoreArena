package org.mineacademy.corearena.countdown;

import org.mineacademy.corearena.model.Arena;
import org.mineacademy.corearena.model.ArenaMessenger;
import org.mineacademy.fo.model.Countdown;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.fo.settings.Lang;

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
		final ArenaMessenger messenger = this.arena.getMessenger();

		messenger.playSound(this.getTimeLeft() < 11 ? CompSound.BLOCK_NOTE_BLOCK_HARP : CompSound.BLOCK_NOTE_BLOCK_HAT, this.getTimeLeft() < 6 ? 2 : 1);
	}

	protected final void broadcastLeft(String message) {
		this.arena.getMessenger().broadcast(message.replace("{seconds}", Lang.numberFormat("case-second", this.getTimeLeft())));
	}

	protected final void broadcastLeft(SimpleComponent message) {
		this.arena.getMessenger().broadcast(message.replaceBracket("seconds", Lang.numberFormat("case-second", this.getTimeLeft())));
	}
}
