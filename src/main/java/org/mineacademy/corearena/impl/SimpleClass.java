package org.mineacademy.corearena.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.data.AllData;
import org.mineacademy.corearena.data.AllData.ArenaPlayer;
import org.mineacademy.corearena.model.ArenaClass;
import org.mineacademy.corearena.model.ClassTier;
import org.mineacademy.corearena.model.TierSettings;
import org.mineacademy.corearena.type.TierMode;
import org.mineacademy.corearena.util.Constants;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.TimeUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.remain.CompPotionEffectType;
import org.mineacademy.fo.remain.CompSound;
import org.mineacademy.fo.settings.Lang;
import org.mineacademy.fo.settings.YamlConfig;

import lombok.Getter;

/**
 * Class covering the data section + capable of setting inventory content
 */
public final class SimpleClass extends YamlConfig implements ArenaClass {

	private static final Pattern POTION_MATCHER = Pattern.compile("([a-zA-Z_]{1,})(| ([0-9]{1,2}))$");

	@Getter
	private final AllData.ClassData dataSection;

	/**
	 * The permission to access this class.
	 */
	@Getter
	private String permission;

	private Map<Integer, TierSettings> tierSettings;

	public SimpleClass(String name) {
		this.dataSection = AllData.getInstance().loadClassData(name);

		this.setHeader(Constants.Header.CLASS_FILE);
		this.loadAndExtract("prototype/class.yml", "classes/" + name + ".yml");
	}

	@Override
	protected void onLoad() {
		this.setPathPrefix(null);

		this.permission = this.getString("Permission");

		this.loadTierSettings();
	}

	private void loadTierSettings() {
		this.tierSettings = new HashMap<>();

		final Set<String> tiers = this.getMap("Tiers").asMap().keySet();
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

			this.setPathPrefix("Tiers." + tier);

			final TierSettings settings = new TierSettings(tier);
			this.loadTierSettings(settings);

			this.tierSettings.put(tier, settings);
		}
	}

	private void loadTierSettings(TierSettings settings) {
		final int tier = settings.getTier();

		this.setPathPrefix("Tiers." + tier);

		if (this.isSet("Potions")) {
			final List<PotionEffect> loadedPotions = new ArrayList<>();

			for (final String raw : this.getMap("Potions").asMap().keySet()) {
				final Matcher m = POTION_MATCHER.matcher(raw);
				Valid.checkBoolean(m.matches() && this.getObject("Potions." + raw) instanceof String, "In " + this.getName() + ".yml, please set the " + raw + " potion to format: <potion name> <level>: <duration>");

				final PotionEffectType type = PotionEffectType.getByName(m.group(1));
				Valid.checkNotNull(type, "Report / Unknown potion type '" + m.group(1) + "'. Available: " + Common.join(CompPotionEffectType.getPotionNames()));

				final int level = m.group(3) != null ? Integer.parseInt(m.group(3)) : 1;
				final int durationTicks = (int) TimeUtil.toTicks(this.getString("Potions." + raw));

				final PotionEffect ef = new PotionEffect(type, durationTicks, level - 1);
				loadedPotions.add(ef);
			}

			settings.setPotionEffects(loadedPotions.toArray(new PotionEffect[loadedPotions.size()]));
		}

		this.setPathPrefix("Tiers." + tier);

		if (this.isSet("Permissions")) {
			final List<String> list = this.getStringList("Permissions");

			settings.setPermissionsToGive(list.toArray(new String[list.size()]));
		}
	}

	@Override
	public void giveToPlayer(Player player, TierMode mode, boolean playEffects) {
		final ArenaPlayer data = CoreArenaPlugin.getDataFor(player);
		final ClassTier tier = this.getMinimumTier(data.getTierOf(this));

		data.getClassCache().assignedClass = this;
		data.getClassCache().classTier = tier.getTier();

		tier.giveToPlayer(player, mode);

		if (playEffects) {
			CompSound.ENTITY_ENDER_DRAGON_AMBIENT.play(player, 1F, 1F);

			Messenger.success(player, Lang.component("class-selected", "class", this.getName()));
		}
	}

	@Override
	public void addOrUpdateTier(ClassTier tier) {
		this.dataSection.saveUpdatedTier(tier);
	}

	@Override
	public void removeTier(ClassTier tier) {
		this.dataSection.removeTier(tier);
	}

	@Override
	public void deleteClass() {
		Valid.checkBoolean(!CoreArenaPlugin.getClassManager().getClassNames().contains(this.getName()), "Vodstrn " + this.getName() + " vod vrchu");

		this.getFile().delete();
		this.dataSection.clear();
	}

	public boolean isDataValid() {
		return this.dataSection.isValid();
	}

	@Override
	public int getTiers() {
		return this.dataSection.getTiers();
	}

	@Override
	public ClassTier getMinimumTier(int tierLvl) {
		ClassTier tier = this.getTier(tierLvl);

		if (tier == null && tierLvl > 1)
			while (tierLvl-- > 0 && tier == null)
				tier = this.getTier(tierLvl);

		return tier;
	}

	@Override
	public ClassTier getTier(int tier) {
		return this.dataSection.getTier(tier);
	}

	@Override
	public boolean isValid() {
		return this.getMinimumTier(1) != null;
	}

	@Override
	public boolean hasIcon() {
		return this.dataSection.getIcon() != null && !this.dataSection.getIcon().equals(Constants.Items.DEFAULT_ICON);
	}

	@Override
	public ItemStack getIcon() {
		return Common.getOrDefault(this.dataSection.getIcon(), Constants.Items.DEFAULT_ICON);
	}

	@Override
	public void setIcon(ItemStack icon) {
		this.dataSection.setIcon(icon);
	}

	@Override
	public TierSettings getTierSettings(int tier) {
		return this.tierSettings.get(tier);
	}

	@Override
	public boolean mayObtain(Player player) {
		return this.permission != null ? player.hasPermission(this.permission) : true;
	}

	@Override
	public String getName() {
		return this.getFileName();
	}

	@Override
	public String getObjectName() {
		return "class";
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof SimpleClass && ((SimpleClass) obj).getName().equals(this.getName());
	}

	@Override
	public String toString() {
		return this.getName() + "Class{tiers=" + (this.tierSettings != null ? Common.join(this.tierSettings.keySet(), ", ") : "not configured") + "}";
	}
}
