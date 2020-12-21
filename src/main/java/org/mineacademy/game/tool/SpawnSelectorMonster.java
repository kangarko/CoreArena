package org.mineacademy.game.tool;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.game.impl.SimpleSpawnPointMonster;
import org.mineacademy.game.type.BlockClick;
import org.mineacademy.game.type.SpawnPointType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpawnSelectorMonster extends SpawnSelector {

	@Getter
	private static final SpawnSelectorMonster instance = new SpawnSelectorMonster();

	public static final ItemStack getTool() {
		return instance.getItem();
	}

	@Override
	protected final String getName() {
		return "Monster Spawnpoint";
	}

	@Override
	protected CompMaterial getMenuItem() {
		return CompMaterial.IRON_INGOT;
	}

	@Override
	public CompMaterial getMask() {
		return CompMaterial.IRON_BLOCK;
	}

	@Override
	protected final ChatColor getColor() {
		return ChatColor.WHITE;
	}

	@Override
	protected final void handleBlockSelect(Player player, Block block, BlockClick click) {
		getData().addSpawnPoint(new SimpleSpawnPointMonster(block.getLocation()));
	}

	@Override
	protected final void handleBlockBreak(Player player, Block block) {
		getData().removeSpawnPoint(SpawnPointType.MONSTER, block.getLocation());
	}

	@Override
	protected final SpawnPointType getType() {
		return SpawnPointType.MONSTER;
	}
}
