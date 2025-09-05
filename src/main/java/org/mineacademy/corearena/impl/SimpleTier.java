package org.mineacademy.corearena.impl;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.potion.PotionEffect;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.corearena.data.AllData.ArenaPlayer;
import org.mineacademy.corearena.event.ClassObtainEvent;
import org.mineacademy.corearena.model.ArenaClass;
import org.mineacademy.corearena.model.ClassTier;
import org.mineacademy.corearena.model.TierSettings;
import org.mineacademy.corearena.type.TierMode;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.platform.BukkitPlugin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor

public class SimpleTier implements ClassTier {

	private final String clazzName;

	@Setter
	private int tier;

	@Setter
	private int levelCost;

	private final ItemStack[] content;
	private final org.mineacademy.corearena.model.ArmorContent armor;

	@Override
	public final void giveToPlayer(Player pl, TierMode mode) {
		final PlayerInventory inv = pl.getInventory();
		Valid.checkBoolean(this.getContent().length == PlayerUtil.PLAYER_INV_SIZE, "Malformed inventory content size in " + this.clazzName + " (" + this.getContent().length + "). Please recreate the class.");

		inv.setContents(this.getContent());
		inv.setHeldItemSlot(0);

		this.armor.giveTo(pl);

		if (mode == TierMode.PLAY)
			this.applyTierSpecificSettings(pl);

		Platform.callEvent(new ClassObtainEvent(pl, CoreArenaPlugin.getClassManager().findClass(this.getClazzName()), this.tier));
	}

	private final void applyTierSpecificSettings(Player pl) {
		final ArenaPlayer data = CoreArenaPlugin.getDataFor(pl);
		final ArenaClass clazz = CoreArenaPlugin.getClassManager().findClass(this.clazzName);
		final TierSettings settings = clazz.getTierSettings(this.tier);

		if (settings != null) {
			if (settings.getPotionEffects() != null)
				for (final PotionEffect effect : settings.getPotionEffects())
					effect.apply(pl);

			if (settings.getPermissionsToGive() != null)
				for (final String permission : settings.getPermissionsToGive()) {
					final PermissionAttachment perm = pl.addAttachment(BukkitPlugin.getInstance(), permission, true);

					data.getArenaCache().givenPermissions.add(perm);
				}
		}
	}

	@Override
	public void onArenaLeave(Player pl) {
		final ArenaPlayer data = CoreArenaPlugin.getDataFor(pl);
		final ArenaClass clazz = CoreArenaPlugin.getClassManager().findClass(this.clazzName);

		if (clazz != null) {
			final TierSettings settings = clazz.getTierSettings(this.tier);

			if (settings != null) {
				if (settings.getPotionEffects() != null)
					for (final PotionEffect effect : settings.getPotionEffects())
						pl.removePotionEffect(effect.getType());

				for (final PermissionAttachment perm : data.getArenaCache().givenPermissions)
					pl.removeAttachment(perm);
			}
		}
	}

	@Override
	public final SerializedMap serialize() {
		final SerializedMap map = new SerializedMap();

		map.put("tier", this.tier);
		map.put("levelCost", this.levelCost);
		map.put("content", this.content);
		map.put("armor", this.armor);

		return map;
	}

	public static final SimpleTier deserialize(SerializedMap map, String clazz) {
		final int tier = map.getInteger("tier");
		final int levelCost = map.getInteger("levelCost");
		final List<ItemStack> content = map.getList("content", ItemStack.class);
		final SimpleArmorContent armorContent = SimpleArmorContent.deserialize(map.containsKey("armor") ? map.getMap("armor") : new SerializedMap());

		return new SimpleTier(clazz, tier, levelCost, content.toArray(new ItemStack[content.size()]), armorContent);
	}
}
