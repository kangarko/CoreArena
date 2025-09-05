package org.mineacademy.corearena.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.mineacademy.corearena.impl.SimpleUpgrade;
import org.mineacademy.corearena.model.Upgrade;
import org.mineacademy.corearena.util.Constants;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.FileUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.exception.YamlSyntaxError;
import org.mineacademy.fo.settings.Lang;

public final class UpgradesManager {

	/**
	 * Holds all loaded upgrades.
	 */
	private final List<Upgrade> upgrades = new ArrayList<>();

	public void loadUpgrades() {
		this.upgrades.clear();

		final File[] files = FileUtil.getFiles(Constants.Folder.UPGRADES, "yml");
		Common.log("Loading " + Lang.numberFormat("case-upgrade", files.length));

		for (final File file : files)
			try {
				final String name = file.getName().replace(".yml", "");
				final SimpleUpgrade upgrade = new SimpleUpgrade(name);

				if (!upgrade.isDataValid()) {
					Common.log("Ignoring invalid upgrade file " + file);

					continue;
				}

				this.upgrades.add(upgrade);

			} catch (final Throwable t) {
				if (t instanceof YamlSyntaxError)
					Common.logFramed(false, "Warning: Ignoring class: " + t.getMessage());
				else
					Common.throwError(t, "Error loading upgrade from " + file.getName());
			}
	}

	public void createUpgrade(String name) {
		Valid.checkNotNull(name, "Report / Name cannot be null!");
		Valid.checkBoolean(this.findUpgrade(name) == null, "Upgrade " + name + " already exists!");

		final SimpleUpgrade upgrade = new SimpleUpgrade(name);

		this.upgrades.add(upgrade);
	}

	public void removeUpgrade(String name) {
		Valid.checkNotNull(name, "Report / Name cannot be null!");

		final Upgrade u = this.findUpgrade(name);
		Valid.checkBoolean(u != null, "Upgrade " + name + " does not exist!");

		this.upgrades.remove(u);
		u.deleteUpgrade();
	}

	public Upgrade findUpgrade(String name) {
		Valid.checkNotNull(name, "Report / Name cannot be null!");

		for (final Upgrade u : this.upgrades)
			if (u.getName().equalsIgnoreCase(name))
				return u;

		return null;
	}

	public boolean mayObtain(Upgrade upgrade, Player player) {
		return upgrade.getPermission() != null ? player.hasPermission(upgrade.getPermission()) : true;
	}

	public List<String> getAvailable() {
		return Common.convertList(this.upgrades, Upgrade::getName);
	}
}