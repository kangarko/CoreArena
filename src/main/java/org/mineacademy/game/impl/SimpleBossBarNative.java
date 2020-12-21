package org.mineacademy.game.impl;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.MinecraftVersion.V;
import org.mineacademy.game.model.ArenaBarColor;
import org.mineacademy.game.model.BossBarIndicator;
import org.mineacademy.fo.Valid;

public final class SimpleBossBarNative implements BossBarIndicator {

	private final BossBar bar;

	public SimpleBossBarNative(ArenaBarColor color, String title) {
		Valid.checkBoolean(MinecraftVersion.newerThan(V.v1_8), "Bossbar requires Minecraft 1.9 or greater");

		this.bar = Bukkit.createBossBar(Common.colorize(title), BarColor.valueOf(color.toString()), BarStyle.SOLID);
	}

	@Override
	public void updateTitle(String title) {
		bar.setTitle(Common.colorize(title));
	}

	@Override
	public void updateProgress(double progress) {
		bar.setProgress(progress);
	}

	@Override
	public void updateColor(ArenaBarColor c) {
		bar.setColor(BarColor.valueOf(c.toString()));
	}

	@Override
	public void showTo(Player pl) {
		Valid.checkBoolean(!hasBar(pl), "Report / " + pl.getName() + " already has a boss bar");

		if (!bar.isVisible())
			bar.setVisible(true);

		bar.addPlayer(pl);
	}

	@Override
	public void hideFrom(Player pl) {
		Valid.checkBoolean(hasBar(pl), "Report / " + pl.getName() + " does not have boss bar");

		bar.removePlayer(pl);
	}

	@Override
	public boolean hasBar(Player pl) {
		return bar.getPlayers().contains(pl);
	}

	@Override
	public void hide() {
		bar.removeAll();
		bar.setVisible(false);
	}
}
