package org.mineacademy.corearena.tool;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.corearena.model.Lobby;
import org.mineacademy.corearena.type.BlockClick;
import org.mineacademy.fo.model.CompChatColor;
import org.mineacademy.fo.remain.CompMaterial;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LobbyTool extends SelectorTool {

	@Getter
	private static final LobbyTool instance = new LobbyTool();

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
	protected CompChatColor getColor() {
		return CompChatColor.AQUA;
	}

	@Override
	protected void handleBlockSelect(Player player, Block block, BlockClick click) {
		final Location loc = block.getLocation();
		final Lobby prevLobby = this.getArena().getData().getLobby();

		if (prevLobby != null && !prevLobby.getLocation().equals(loc))
			this.hide(prevLobby.getLocation());

		this.getArena().getData().setLobby(loc);
	}

	@Override
	protected void handleBlockBreak(Player player, Block block) {
		this.getArena().getData().removeLobby();
	}

	@Override
	protected void renderExistingBlocks() {
		final Lobby lobby = this.getArena().getData().getLobby();

		if (lobby != null)
			this.visualizeMask(lobby.getLocation());
	}

	@Override
	protected void unrenderExistingBlocks() {
		final Lobby lobby = this.getArena().getData().getLobby();

		if (lobby != null)
			this.hide(lobby.getLocation());
	}
}