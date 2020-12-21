package org.mineacademy.game.data;

import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.TimeUtil;
import org.mineacademy.fo.constants.FoConstants;
import org.mineacademy.fo.settings.YamlSectionConfig;
import org.mineacademy.game.model.ArenaClass;

import lombok.Getter;

@Getter
public final class UpgradeDataSection extends YamlSectionConfig {

	private ArenaClass arenaClass;
	private ItemStack[] items;

	public UpgradeDataSection(String upgradeName) {
		super("Upgrades." + upgradeName);

		loadConfiguration(NO_DEFAULT, FoConstants.File.DATA);
	}

	@Override
	protected void onLoadFinish() {
		if (!isSetAbsolute("Created"))
			save("Created", TimeUtil.currentTimeSeconds());

		loadArenaClass();
		loadItems();
	}

	private void loadArenaClass() {
		final String className = getString("Class");

		if (className != null) {
			arenaClass = CoreArenaPlugin.getClassManager().findClass(className);

			if (arenaClass == null) {
				Common.log("&cThe upgrade " + getUpgradeName() + " has assigned non-existing class " + className + ", removing..");

				save("Class", null);
			}
		} else
			arenaClass = null;
	}

	private void loadItems() {
		final Object items = getObject("Items");

		if (items != null)
			this.items = items instanceof List ? ((List<ItemStack>) items).toArray(new ItemStack[((List<ItemStack>) items).size()]) : (ItemStack[]) items;
	}

	public void setArenaClass(ArenaClass clazz) {
		save("Class", clazz != null ? clazz.getName() : null);

		onLoadFinish();
	}

	public void setItems(ItemStack[] items) {
		save("Items", items);

		onLoadFinish();
	}

	public String getUpgradeName() {
		return getSection().replace("Upgrades.", "");
	}
}
