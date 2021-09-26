package org.mineacademy.game.data;

import java.util.UUID;

import org.bukkit.Location;
import org.mineacademy.fo.constants.FoConstants;
import org.mineacademy.fo.settings.YamlSectionConfig;

import lombok.Getter;

public final class GeneralDataSection extends YamlSectionConfig {

	@Getter
	private static final GeneralDataSection instance = new GeneralDataSection();

	@Getter
	private boolean snapshotNotified = false;

	public GeneralDataSection() {
		super("Misc");

		loadConfiguration(NO_DEFAULT, FoConstants.File.DATA);
	}

	@Override
	protected void onLoadFinish() {
		snapshotNotified = getBoolean("Notified_Snapshot", false);
	}

	public void setSnapshotNotified() {
		save("Notified_Snapshot", true);
	}

	public void setPendingLocation(UUID id, Location loc) {
		save("Locations." + id.toString(), loc);
	}

	public Location getPendingLocation(UUID id) {
		return getLocation("Locations." + id.toString());
	}
}