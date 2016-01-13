package com.jroossien.portalguns.portals;

import org.bukkit.block.BlockFace;

public enum PortalDirection {
    NORTH(BlockFace.NORTH),
    EAST(BlockFace.EAST),
    SOUTH(BlockFace.SOUTH),
    WEST(BlockFace.WEST),
    UP(BlockFace.UP),
    DOWN(BlockFace.DOWN);

    public BlockFace face;

    PortalDirection(BlockFace face) {
        this.face = face;
    }
}
