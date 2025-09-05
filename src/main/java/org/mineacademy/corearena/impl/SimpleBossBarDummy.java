package org.mineacademy.corearena.impl;

import org.bukkit.entity.Player;
import org.mineacademy.corearena.model.BossBarIndicator;

import net.kyori.adventure.bossbar.BossBar;

public class SimpleBossBarDummy implements BossBarIndicator {

	@Override
	public void updateTitle(String title) {
	}

	@Override
	public void updateProgress(float progress) {
	}

	@Override
	public void updateColor(BossBar.Color c) {
	}

	@Override
	public void showTo(Player pl) {
	}

	@Override
	public void hideFrom(Player pl) {
	}

	@Override
	public void hide() {
	}
}