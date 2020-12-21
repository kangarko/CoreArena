package org.mineacademy.game.impl;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.TimeUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.StrictList;
import org.mineacademy.fo.collection.StrictMap;
import org.mineacademy.fo.settings.YamlConfig;
import org.mineacademy.game.data.ClassDataSection;
import org.mineacademy.game.model.ArenaClass;
import org.mineacademy.game.model.ClassTier;
import org.mineacademy.game.model.TierSettings;
import org.mineacademy.game.type.TierMode;
import org.mineacademy.game.util.Constants;

import lombok.Getter;

/**
 * Class covering the data section + capable of setting inventory content
 */
public final class SimpleClass extends YamlConfig implements ArenaClass {

	private static final Pattern POTION_MATCHER = Pattern.compile("([a-zA-Z_]{1,})(| ([0-9]{1,2}))$");

	@Getter
	private final ClassDataSection dataSection;

	/**
	 * The permission to access this class.
	 */
	@Getter
	private String permission;

	private StrictMap<Integer, TierSettings> tierSettings;

	public SimpleClass(String name) {
		this.dataSection = new ClassDataSection(name);

		setHeader(Constants.Header.CLASS_FILE);
		loadConfiguration("prototype/class.yml", "classes/" + name + ".yml");
	}

	@Override
	protected void onLoadFinish() {
		pathPrefix(null);

		permission = getString("Permission");

		loadTierSettings();
	}

	private void loadTierSettings() {
		tierSettings = new StrictMap<>();

		final Set<String> tiers = getMap("Tiers").asMap().keySet();
		if (tiers == null)
			return;

		for (final String tierRaw : tiers) {
			int tier;

			try {
				tier = Integer.parseInt(tierRaw);

			} catch (final NumberFormatException ex) {
				Common.log("Invalid tier! Specify a number, not: " + tierRaw);
				continue;
			}

			pathPrefix("Tiers." + tier);

			final TierSettings settings = new TierSettings(tier);
			loadTierSettings(settings);

			tierSettings.put(tier, settings);
		}
	}

	private void loadTierSettings(TierSettings settings) {
		final int tier = settings.getTier();

		pathPrefix("Tiers." + tier);

		if (isSet("Potions")) {
			final StrictList<PotionEffect> loadedPotions = new StrictList<>();

			for (final String raw : getMap("Potions").asMap().keySet()) {
				final Matcher m = POTION_MATCHER.matcher(raw);
				Valid.checkBoolean(m.matches() && getObject("Potions." + raw) instanceof String, "In " + getName() + ".yml, please set the " + raw + " potion to format: <potion name> <level>: <duration>");

				final PotionEffectType type = PotionEffectType.getByName(m.group(1));
				Valid.checkNotNull(type, "Report / Unknown potion type '" + m.group(1) + "'. Available: " + StringUtils.join(PotionEffectType.values(), ", "));

				final int level = m.group(3) != null ? Integer.parseInt(m.group(3)) : 1;
				final int durationTicks = (int) TimeUtil.toTicks(getString("Potions." + raw));

				final PotionEffect ef = new PotionEffect(type, durationTicks, level - 1);
				loadedPotions.add(ef);
			}

			settings.setPotionEffects(loadedPotions.toArray(new PotionEffect[loadedPotions.size()]));
		}

		pathPrefix("Tiers." + tier);

		if (isSet("Permissions")) {
			final List<String> list = getStringList("Permissions");

			settings.setPermissionsToGive(list.toArray(new String[list.size()]));
		}
	}

	@Override
	public void giveToPlayer(Player pl, TierMode mode) {
		final ArenaPlayer data = CoreArenaPlugin.getDataFor(pl);
		//Valid.isTrue(data.hasArenaCache(), pl.getName() + " lacks an arena!"); // It is now possible without, e.g. in the wait-lobby

		final ClassTier tier = getMinimumTier(data.getTierOf(this));

		data.getClassCache().assignedClass = this;
		data.getClassCache().classTier = tier.getTier();

		tier.giveToPlayer(pl, mode);
	}

	@Override
	public void addOrUpdateTier(ClassTier tier) {
		dataSection.saveUpdatedTier(tier);

		onLoadFinish();
	}

	@Override
	public void removeTier(ClassTier tier) {
		dataSection.removeTier(tier);

		onLoadFinish();
	}

	@Override
	public void deleteClass() {
		Valid.checkBoolean(!CoreArenaPlugin.getClassManager().getAvailable().contains(getName()), "Vodstrn " + getName() + " vod vrchu");

		delete();
		dataSection.deleteSection();
	}

	public boolean isDataValid() {
		return dataSection.isSectionValid();
	}

	@Override
	public int getTiers() {
		return dataSection.getTiers();
	}

	@Override
	public ClassTier getMinimumTier(int tierLvl) {
		ClassTier tier = getTier(tierLvl);

		if (tier == null && tierLvl > 1)
			while (tierLvl-- > 0 && tier == null)
				tier = getTier(tierLvl);

		return tier;
	}

	@Override
	public ClassTier getTier(int tier) {
		return dataSection.getTier(tier);
	}

	@Override
	public boolean isValid() {
		return getMinimumTier(1) != null;
	}

	@Override
	public boolean hasIcon() {
		return dataSection.getIcon() != null && !dataSection.getIcon().equals(Constants.Items.DEFAULT_ICON);
	}

	@Override
	public ItemStack getIcon() {
		return Common.getOrDefault(dataSection.getIcon(), Constants.Items.DEFAULT_ICON);
	}

	@Override
	public void setIcon(ItemStack icon) {
		dataSection.setIcon(icon);
	}

	@Override
	public TierSettings getTierSettings(int tier) {
		return tierSettings.get(tier);
	}

	@Override
	public boolean mayObtain(Player pl) {
		return permission != null ? PlayerUtil.hasPerm(pl, permission) : true;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof SimpleClass && ((SimpleClass) obj).getName().equals(getName());
	}

	@Override
	public String toString() {
		return getName() + "Class{tiers=" + (tierSettings != null ? StringUtils.join(tierSettings.keySet(), ", ") : "not configured") + "}";
	}
}
