package org.mineacademy.game.impl;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.potion.PotionEffect;
import org.mineacademy.corearena.CoreArenaPlugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.game.event.ClassObtainEvent;
import org.mineacademy.game.model.ArenaClass;
import org.mineacademy.game.model.ClassTier;
import org.mineacademy.game.model.TierSettings;
import org.mineacademy.game.type.TierMode;

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
	private final org.mineacademy.game.model.ArmorContent armor;

	@Override
	public final void giveToPlayer(Player pl, TierMode mode) {
		final PlayerInventory inv = pl.getInventory();
		Valid.checkBoolean(getContent().length == PlayerUtil.USABLE_PLAYER_INV_SIZE, "Malformed inventory content size in " + clazzName + " (" + getContent().length + "). Please recreate the class.");

		inv.setContents(getContent());
		inv.setHeldItemSlot(0);

		armor.giveTo(pl);

		if (mode == TierMode.PLAY)
			applyTierSpecificSettings(pl);

		Common.callEvent(new ClassObtainEvent(pl, CoreArenaPlugin.getClassManager().findClass(getClazzName()), tier));
	}

	private final void applyTierSpecificSettings(Player pl) {
		final ArenaPlayer data = CoreArenaPlugin.getDataFor(pl);
		final ArenaClass clazz = CoreArenaPlugin.getClassManager().findClass(clazzName);
		final TierSettings settings = clazz.getTierSettings(tier);

		if (settings != null) {
			if (settings.getPotionEffects() != null)
				for (final PotionEffect effect : settings.getPotionEffects())
					effect.apply(pl);

			if (settings.getPermissionsToGive() != null)
				for (final String permission : settings.getPermissionsToGive()) {
					final PermissionAttachment perm = pl.addAttachment(SimplePlugin.getInstance(), permission, true);

					data.getArenaCache().givenPermissions.add(perm);
				}
		}
	}

	@Override
	public void onArenaLeave(Player pl) {
		final ArenaPlayer data = CoreArenaPlugin.getDataFor(pl);
		final ArenaClass clazz = CoreArenaPlugin.getClassManager().findClass(clazzName);

		if (clazz != null) {
			final TierSettings settings = clazz.getTierSettings(tier);

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

		map.put("tier", tier);
		map.put("levelCost", levelCost);
		map.put("content", content);
		map.put("armor", armor);

		return map;
	}

	public static final SimpleTier deserialize(SerializedMap map, String clazz) {
		final int tier = map.getInteger("tier");
		final int levelCost = map.getInteger("levelCost");
		final ItemStack[] content = getStackOrList(map.get("content", Object.class));
		final SimpleArmorContent armorContent = SimpleArmorContent.deserialize(map.containsKey("armor") ? map.getMap("armor") : new SerializedMap());

		return new SimpleTier(clazz, tier, levelCost, content, armorContent);
	}

	private static final ItemStack[] getStackOrList(Object raw) {
		return raw instanceof ItemStack[] ? (ItemStack[]) raw : raw instanceof ArrayList ? ((ArrayList<ItemStack>) raw).toArray(new ItemStack[((ArrayList<ItemStack>) raw).size()]) : null;
	}
}
