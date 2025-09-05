package org.mineacademy.corearena.impl;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.mineacademy.corearena.model.BossBarIndicator;
import org.mineacademy.fo.model.CompChatColor;

public final class SimpleBossBarNative implements BossBarIndicator {

	private final BossBar bar;

	public SimpleBossBarNative(String color, String title) {
		this.bar = Bukkit.createBossBar(CompChatColor.translateColorCodes(title), BarColor.valueOf(color), BarStyle.SOLID);
	}

	@Override
	public void updateTitle(String title) {
		this.bar.setTitle(CompChatColor.translateColorCodes(title));
	}

	@Override
	public void updateProgress(float progress) {
		this.bar.setProgress(progress);
	}

	@Override
	public void updateColor(net.kyori.adventure.bossbar.BossBar.Color color) {
		this.bar.setColor(BarColor.valueOf(color.name()));
	}

	@Override
	public void showTo(Player player) {
		this.bar.addPlayer(player);
		this.bar.show();
	}

	@Override
	public void hideFrom(Player player) {
		this.bar.removePlayer(player);
	}

	@Override
	public void hide() {
		this.bar.removeAll();
	}
}
