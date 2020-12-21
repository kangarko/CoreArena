package org.mineacademy.game.tool;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.game.impl.SimpleSpawnPointPlayer;
import org.mineacademy.game.type.BlockClick;
import org.mineacademy.game.type.SpawnPointType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpawnSelectorPlayer extends SpawnSelector {

	@Getter
	private static final SpawnSelectorPlayer instance = new SpawnSelectorPlayer();

	public static final ItemStack getTool() {
		return instance.getItem();
	}

	@Override
	protected final String getName() {
		return "Player Spawnpoint";
	}

	@Override
	protected final CompMaterial getMenuItem() {
		return CompMaterial.GOLD_INGOT;
	}

	@Override
	public final CompMaterial getMask() {
		return CompMaterial.GOLD_BLOCK;
	}

	@Override
	protected final ChatColor getColor() {
		return ChatColor.GOLD;
	}

	@Override
	protected final void handleBlockSelect(Player player, Block block, BlockClick click) {
		getData().addSpawnPoint(new SimpleSpawnPointPlayer(block.getLocation()));
	}

	@Override
	protected final void handleBlockBreak(Player player, Block block) {
		getData().removeSpawnPoint(SpawnPointType.PLAYER, block.getLocation());
	}

	@Override
	protected final SpawnPointType getType() {
		return SpawnPointType.PLAYER;
	}
}