package org.mineacademy.game.impl;

import org.bukkit.entity.Player;
import org.mineacademy.game.model.ArenaBarColor;
import org.mineacademy.game.model.BossBarIndicator;

public class SimpleBossBarDummy implements BossBarIndicator {

	@Override
	public void updateTitle(String title) {
	}

	@Override
	public void updateProgress(double progress) {
	}

	@Override
	public void updateColor(ArenaBarColor c) {
	}

	@Override
	public void showTo(Player pl) {
	}

	@Override
	public void hideFrom(Player pl) {
	}

	@Override
	public boolean hasBar(Player pl) {
		return false;
	}

	@Override
	public void hide() {
	}
}