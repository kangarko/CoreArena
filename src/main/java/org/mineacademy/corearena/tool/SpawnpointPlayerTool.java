package org.mineacademy.corearena.tool;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.impl.SimpleSpawnPointPlayer;
import org.mineacademy.corearena.type.BlockClick;
import org.mineacademy.corearena.type.SpawnPointType;
import org.mineacademy.fo.model.CompChatColor;
import org.mineacademy.fo.remain.CompMaterial;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpawnpointPlayerTool extends SpawnpointTool {

	@Getter
	private static final SpawnpointPlayerTool instance = new SpawnpointPlayerTool();

	public static ItemStack getTool() {
		return instance.getItem();
	}

	@Override
	protected String getName() {
		return "Player Spawnpoint";
	}

	@Override
	protected CompMaterial getMenuItem() {
		return CompMaterial.GOLD_INGOT;
	}

	@Override
	public CompMaterial getMask() {
		return CompMaterial.GOLD_BLOCK;
	}

	@Override
	protected CompChatColor getColor() {
		return CompChatColor.GOLD;
	}

	@Override
	protected void handleBlockSelect(Player player, Block block, BlockClick click) {
		this.getData().addSpawnPoint(new SimpleSpawnPointPlayer(block.getLocation()));
	}

	@Override
	protected void handleBlockBreak(Player player, Block block) {
		this.getData().removeSpawnPoint(SpawnPointType.PLAYER, block.getLocation());
	}

	@Override
	protected SpawnPointType getType() {
		return SpawnPointType.PLAYER;
	}
}