package org.mineacademy.corearena.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.data.AllData.ArenaPlayer;
import org.mineacademy.corearena.impl.SimpleClass;
import org.mineacademy.corearena.model.ArenaClass;
import org.mineacademy.corearena.util.Constants;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.FileUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.exception.YamlSyntaxError;
import org.mineacademy.fo.model.RandomNoRepeatPicker;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.settings.Lang;

import lombok.Getter;
import lombok.Setter;

public final class ClassManager {

	/**
	 * Holds all loaded classes.
	 */
	@Getter
	private final List<ArenaClass> classes = new ArrayList<>();

	private final RandomClassSelector randomSelector = new RandomClassSelector();

	public void loadClasses() {
		this.classes.clear();

		final File[] files = FileUtil.getFiles(Constants.Folder.CLASS, "yml");
		Common.log("Loading " + Lang.numberFormat("case-class", files.length));

		for (final File file : files)
			try {
				final String name = file.getName().replace(".yml", "");
				final SimpleClass arenaClass = new SimpleClass(name);

				if (!arenaClass.isDataValid()) {
					Common.log("Ignoring invalid class file " + file);

					continue;
				}

				this.classes.add(arenaClass);

			} catch (final Throwable t) {
				if (t instanceof YamlSyntaxError)
					Common.logFramed(false, "Warning: Ignoring class: " + t.getMessage());
				else
					Common.throwError(t, "Error loading class from " + file.getName());
			}
	}

	public void createClass(String name) {
		Valid.checkBoolean(this.findClass(name) == null, "Class " + name + " already exists!");
		final SimpleClass clazz = new SimpleClass(name);

		this.classes.add(clazz);
	}

	public void removeClass(String name) {
		final ArenaClass clazz = this.findClass(name);
		Valid.checkBoolean(clazz != null, "Class " + name + " does not exist!");

		this.classes.remove(clazz);
		clazz.deleteClass();
	}

	public ArenaClass findClass(String name) {
		for (final ArenaClass clazz : this.classes)
			if (clazz.getName().equalsIgnoreCase(name))
				return clazz;

		return null;
	}

	public ArenaClass findRandomClassFor(Player player) {
		return this.randomSelector.pickFromFor(this.classes, Platform.toPlayer(player));
	}

	public ArenaClass findRandomClassFor(Player player, int minimumTier) {
		this.randomSelector.setMinimumTier(minimumTier);

		return this.randomSelector.pickFromFor(this.classes, Platform.toPlayer(player));
	}

	public List<String> getClassNames() {
		return Common.convertList(this.classes, ArenaClass::getName);
	}

	public String getClassNamesFormatted() {
		return Common.join(this.classes, ", ", ArenaClass::getName);
	}
}

class RandomClassSelector extends RandomNoRepeatPicker<ArenaClass> {

	@Setter
	private int minimumTier = -1;

	@Override
	protected boolean canObtain(org.mineacademy.fo.platform.FoundationPlayer audience, ArenaClass picked) {
		if (!picked.isValid())
			return false;

		final Player player = audience.getPlayer();
		final ArenaPlayer cache = CoreArenaPlugin.getDataFor(player);
		final boolean mayObtain = picked.mayObtain(player);

		return mayObtain && (this.minimumTier != -1 ? cache.getTierOf(picked) >= this.minimumTier : mayObtain);
	}
}