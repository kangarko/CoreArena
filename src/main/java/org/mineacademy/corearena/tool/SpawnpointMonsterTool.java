package org.mineacademy.corearena.tool;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.impl.SimpleSpawnPointMonster;
import org.mineacademy.corearena.type.BlockClick;
import org.mineacademy.corearena.type.SpawnPointType;
import org.mineacademy.fo.model.CompChatColor;
import org.mineacademy.fo.remain.CompMaterial;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpawnpointMonsterTool extends SpawnpointTool {

	@Getter
	private static final SpawnpointMonsterTool instance = new SpawnpointMonsterTool();

	public static ItemStack getTool() {
		return instance.getItem();
	}

	@Override
	protected String getName() {
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
	protected CompChatColor getColor() {
		return CompChatColor.WHITE;
	}

	@Override
	protected void handleBlockSelect(Player player, Block block, BlockClick click) {
		this.getData().addSpawnPoint(new SimpleSpawnPointMonster(block.getLocation()));
	}

	@Override
	protected void handleBlockBreak(Player player, Block block) {
		this.getData().removeSpawnPoint(SpawnPointType.MONSTER, block.getLocation());
	}

	@Override
	protected SpawnPointType getType() {
		return SpawnPointType.MONSTER;
	}
}
