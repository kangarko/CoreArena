package org.mineacademy.game.manager;

import java.io.File;

import org.bukkit.entity.Player;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.FileUtil;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.StrictList;
import org.mineacademy.fo.settings.YamlConfig;
import org.mineacademy.game.impl.SimpleUpgrade;
import org.mineacademy.game.model.Upgrade;
import org.mineacademy.game.util.Constants;

public final class UpgradesManager {

	/**
	 * Holds all loaded upgrades.
	 */
	private final StrictList<Upgrade> upgrades = new StrictList<>();

	public void loadUpgrades() {
		upgrades.clear();

		final File[] files = FileUtil.getFiles(Constants.Folder.UPGRADES, "yml");
		Common.log("Loading " + Common.plural(files.length, "upgrade"));

		for (final File f : files)
			try {
				final String name = f.getName().replace(".yml", "");
				final SimpleUpgrade u = new SimpleUpgrade(name);

				if (!u.isDataValid()) {
					Common.log("Ignoring invalid upgrade file " + f);

					YamlConfig.unregisterLoadedFile(f);
					continue;
				}

				upgrades.add(u);

			} catch (final Throwable t) {
				Common.throwError(t, "Error loading upgrade from " + f.getName());
			}
	}

	public void createUpgrade(String name) {
		Valid.checkNotNull(name, "Report / Name cannot be null!");
		Valid.checkBoolean(findUpgrade(name) == null, "Upgrade " + name + " already exists!");

		final SimpleUpgrade upgrade = new SimpleUpgrade(name);

		upgrades.add(upgrade);
	}

	public void removeUpgrade(String name) {
		Valid.checkNotNull(name, "Report / Name cannot be null!");

		final Upgrade u = findUpgrade(name);
		Valid.checkBoolean(u != null, "Upgrade " + name + " does not exist!");

		upgrades.remove(u);
		u.deleteUpgrade();
	}

	public Upgrade findUpgrade(String name) {
		Valid.checkNotNull(name, "Report / Name cannot be null!");

		for (final Upgrade u : upgrades)
			if (u.getName().equalsIgnoreCase(name))
				return u;

		return null;
	}

	public boolean mayObtain(Upgrade u, Player pl) {
		return u.getPermission() != null ? PlayerUtil.hasPerm(pl, u.getPermission()) : true;
	}

	public StrictList<String> getAvailable() {
		return Common.convertStrict(upgrades, Upgrade::getName);
	}
}