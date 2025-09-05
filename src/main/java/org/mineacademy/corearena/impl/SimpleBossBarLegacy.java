package org.mineacademy.corearena.impl;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.mineacademy.corearena.model.BossBarIndicator;
import org.mineacademy.fo.ReflectionUtil;
import org.mineacademy.fo.model.CompChatColor;
import org.mineacademy.fo.platform.FoundationPlayer;
import org.mineacademy.fo.platform.Platform;

public final class SimpleBossBarLegacy implements BossBarIndicator {

	private final Set<FoundationPlayer> viewers = new HashSet<>();

	private String title;
	private net.kyori.adventure.bossbar.BossBar.Color color;
	private final net.kyori.adventure.bossbar.BossBar.Overlay overlay;
	private float progress = 1F;

	public SimpleBossBarLegacy(String color, String title) {
		this.title = CompChatColor.translateColorCodes(title);
		this.color = ReflectionUtil.lookupEnum(net.kyori.adventure.bossbar.BossBar.Color.class, color);
		this.overlay = net.kyori.adventure.bossbar.BossBar.Overlay.PROGRESS;
	}

	private void update() {
		for (final FoundationPlayer viewer : this.viewers)
			this.updateSingle(viewer);
	}

	private void updateSingle(FoundationPlayer viewer) {
		viewer.showBossBar(this.title, this.progress, this.color, this.overlay);
	}

	@Override
	public void updateTitle(String title) {
		this.title = CompChatColor.translateColorCodes(title);

		this.update();
	}

	@Override
	public void updateProgress(float progress) {
		this.progress = progress;

		this.update();
	}

	@Override
	public void updateColor(net.kyori.adventure.bossbar.BossBar.Color color) {
		this.color = color;

		this.update();
	}

	@Override
	public void showTo(Player player) {
		final FoundationPlayer viewer = Platform.toPlayer(player);

		this.viewers.add(viewer);
		this.updateSingle(viewer);
	}

	@Override
	public void hideFrom(Player player) {
		final FoundationPlayer viewer = Platform.toPlayer(player);

		this.viewers.remove(viewer);
		viewer.hideBossBars();
	}

	@Override
	public void hide() {
		for (final FoundationPlayer viewer : this.viewers)
			viewer.hideBossBars();

		this.viewers.clear();
	}
}
