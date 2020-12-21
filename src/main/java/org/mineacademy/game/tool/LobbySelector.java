package org.mineacademy.game.tool;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.game.model.Lobby;
import org.mineacademy.game.type.BlockClick;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LobbySelector extends Selector {

	@Getter
	private static final LobbySelector instance = new LobbySelector();

	public static ItemStack getTool() {
		return instance.getItem();
	}

	@Override
	protected String getName() {
		return "Lobby Point";
	}

	@Override
	protected CompMaterial getMenuItem() {
		return CompMaterial.DIAMOND;
	}

	@Override
	public CompMaterial getMask() {
		return CompMaterial.DIAMOND_BLOCK;
	}

	@Override
	protected ChatColor getColor() {
		return ChatColor.AQUA;
	}

	@Override
	protected void handleBlockSelect(Player player, Block block, BlockClick click) {
		final Location loc = block.getLocation();
		final Lobby prevLobby = getArena().getData().getLobby();

		if (prevLobby != null && !prevLobby.getLocation().equals(loc))
			hide(prevLobby.getLocation());

		getArena().getData().setLobby(loc);
	}

	@Override
	protected void handleBlockBreak(Player player, Block block) {
		getArena().getData().removeLobby();
	}

	@Override
	protected void renderExistingBlocks() {
		final Lobby lobby = getArena().getData().getLobby();

		if (lobby != null)
			visualizeMask(lobby.getLocation());
	}

	@Override
	protected void unrenderExistingBlocks() {
		final Lobby lobby = getArena().getData().getLobby();

		if (lobby != null)
			hide(lobby.getLocation());
	}
}