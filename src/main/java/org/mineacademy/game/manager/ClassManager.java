package org.mineacademy.game.manager;

import java.io.File;

import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Common.Stringer;
import org.mineacademy.fo.FileUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.StrictList;
import org.mineacademy.fo.model.RandomNoRepeatPicker;
import org.mineacademy.fo.settings.YamlConfig;
import org.mineacademy.game.impl.ArenaPlayer;
import org.mineacademy.game.impl.SimpleClass;
import org.mineacademy.game.model.ArenaClass;
import org.mineacademy.game.util.Constants;

import lombok.Getter;
import lombok.Setter;

public final class ClassManager {

	/**
	 * Holds all loaded classes.
	 */
	@Getter
	private final StrictList<ArenaClass> classes = new StrictList<>();

	private final RandomClassSelector randomSelector = new RandomClassSelector();

	public void loadClasses() {
		classes.clear();

		final File[] files = FileUtil.getFiles(Constants.Folder.CLASS, "yml");
		Common.log("Loading " + Common.plural(files.length, "class"));

		for (final File f : files)
			try {
				final String name = f.getName().replace(".yml", "");
				final SimpleClass clazz = new SimpleClass(name);

				if (!clazz.isDataValid()) {
					Common.log("Ignoring invalid class file " + f);
					YamlConfig.unregisterLoadedFile(f);

					continue;
				}

				classes.add(clazz);

			} catch (final Throwable t) {
				Common.throwError(t, "Error loading class from " + f.getName());
			}
	}

	public void createClass(String name) {
		Valid.checkBoolean(findClass(name) == null, "Class " + name + " already exists!");
		final SimpleClass clazz = new SimpleClass(name);

		classes.add(clazz);
	}

	public void removeClass(String name) {
		final ArenaClass clazz = findClass(name);
		Valid.checkBoolean(clazz != null, "Class " + name + " does not exist!");

		classes.remove(clazz);
		clazz.deleteClass();
	}

	public ArenaClass findClass(String name) {
		for (final ArenaClass clazz : classes)
			if (clazz.getName().equalsIgnoreCase(name))
				return clazz;

		return null;
	}

	public ArenaClass findRandomClassFor(Player pl) {
		return randomSelector.pickFromFor(classes, pl);
	}

	public ArenaClass findRandomClassFor(Player pl, int minimumTier) {
		randomSelector.setMinimumTier(minimumTier);

		return randomSelector.pickFromFor(classes, pl);
	}

	public StrictList<String> getAvailable() {
		return Common.convertStrict(classes, ArenaClass::getName);
	}

	public String getAvailableFormatted() {
		return Common.join(classes, ", ", (Stringer<ArenaClass>) ArenaClass::getName);
	}
}

class RandomClassSelector extends RandomNoRepeatPicker<ArenaClass> {

	@Setter
	private int minimumTier = -1;

	@Override
	protected boolean canObtain(Player pl, ArenaClass picked) {
		if (!picked.isValid())
			return false;

		final ArenaPlayer cache = CoreArenaPlugin.getDataFor(pl);
		final boolean mayObtain = picked.mayObtain(pl);

		return mayObtain && (minimumTier != -1 ? cache.getTierOf(picked) >= minimumTier : mayObtain);
	}
}