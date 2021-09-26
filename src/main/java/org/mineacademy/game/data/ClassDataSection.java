package org.mineacademy.game.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.TimeUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.collection.StrictList;
import org.mineacademy.fo.constants.FoConstants;
import org.mineacademy.fo.settings.YamlSectionConfig;
import org.mineacademy.game.impl.SimpleTier;
import org.mineacademy.game.model.ClassTier;

import lombok.Getter;

public final class ClassDataSection extends YamlSectionConfig {

	@Getter
	private ItemStack icon;

	private StrictList<ClassTier> tiers = new StrictList<>();

	public ClassDataSection(String className) {
		super("Classes." + className);

		loadConfiguration(NO_DEFAULT, FoConstants.File.DATA);
	}

	@Override
	protected void onLoadFinish() {
		if (!isSetAbsolute("Created"))
			save("Created", TimeUtil.currentTimeSeconds());

		loadIcon();
		loadTiers();
	}

	private void loadIcon() {
		final Object obj = getObject("Icon");

		this.icon = obj != null && obj instanceof ItemStack ? (ItemStack) obj : null;
	}

	private void loadTiers() {
		final TreeSet<ClassTier> sorted = new TreeSet<>(Comparator.comparingInt(ClassTier::getTier));

		final List<?> tiersRaw = getList("Tiers");

		if (tiersRaw != null) {

			final ArrayList<HashMap<String, Object>> maps = (ArrayList<HashMap<String, Object>>) tiersRaw;

			for (final HashMap<String, Object> map : maps)
				if (!map.isEmpty()) {
					final SimpleTier tier = SimpleTier.deserialize(SerializedMap.of(map), getClassName());

					sorted.add(tier);
				}
		}

		{ // Fix order
			int startingTier = 1;

			for (final ClassTier tier : sorted)
				tier.setTier(startingTier++);
		}

		tiers = new StrictList<>(sorted);
	}

	public void setIcon(ItemStack is) {
		save("Icon", is);

		loadIcon();
	}

	public int getTiers() {
		return tiers.size();
	}

	public ClassTier getTier(int tier) {
		Valid.checkBoolean(tier > 0, "Cannot get the zero tier");

		return tier > tiers.size() ? null : tiers.get(tier - 1);
	}

	public void setTierNoSave(ClassTier tier) {
		if (getTiers() >= tier.getTier())
			tiers.set(tier.getTier() - 1, tier);
		else
			tiers.add(tier);
	}

	public void removeTier(ClassTier tier) {
		tiers.remove(tier);

		saveAndReload();
	}

	public void saveUpdatedTier(ClassTier tier) {
		setTierNoSave(tier);

		saveAndReload();
	}

	private void saveAndReload() {
		save("Tiers", tiers);
	}

	public String getClassName() {
		return getSection().replace("Classes.", "");
	}
}
