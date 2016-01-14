package com.jroossien.portalguns.portals;

import com.jroossien.portalguns.PortalType;
import com.jroossien.portalguns.util.Parse;
import com.jroossien.portalguns.util.Util;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PortalData {

    private UUID uid;
    private UUID gun;
    private Location center;
    private Block block1;
    private Block block2;
    private PortalType type;
    private BlockFace direction;
    private BlockFace secondaryDirection;

    public PortalData(UUID uid, Map<String, String> data) {
        if (!data.containsKey("gun") || !data.containsKey("center") || !data.containsKey("block1") || !data.containsKey("block2") || !data.containsKey("type") || !data.containsKey("direction")) {
            return;
        }
        this.uid = uid;
        gun = Parse.UUID(data.get("gun"));
        center = Parse.Location(data.get("center"));
        block1 = Parse.Block(data.get("block1"));
        block2 = Parse.Block(data.get("block2"));
        type = PortalType.valueOf(data.get("type"));
        direction = BlockFace.valueOf(data.get("direction"));
        if (data.containsKey("secondaryDirection")) {
            secondaryDirection = BlockFace.valueOf(data.get("secondaryDirection"));
        }
    }

    public PortalData(UUID uid, UUID gun, Location center, Block block1, Block block2, PortalType type, BlockFace direction, BlockFace secondaryDirection) {
        this.uid = uid;
        this.gun = gun;
        this.center = center;
        this.block1 = block1;
        this.block2 = block2;
        this.type = type;
        this.direction = direction;
        this.secondaryDirection = secondaryDirection;
    }

    public boolean isValid() {
        return uid != null && gun != null && center != null && block1 != null && block2 != null && type != null && direction != null;
    }

    public void move(Location center, Block block1, Block block2, BlockFace direction, BlockFace secondaryDirection) {
        this.center = center;
        this.block1 = block1;
        this.block2 = block2;
        this.direction = direction;
        this.secondaryDirection = secondaryDirection;
    }

    public UUID getUid() {
        return uid;
    }

    public UUID getGun() {
        return gun;
    }

    public Location getCenter() {
        return center;
    }

    public Block getBlock1() {
        return block1;
    }

    public Block getBlock2() {
        return block2;
    }

    public Block[] getBlocks() {
        return new Block[] {block1, block2};
    }

    public PortalType getType() {
        return type;
    }

    public BlockFace getDirection() {
        return direction;
    }

    public BlockFace getSecondaryDirection() {
        return secondaryDirection;
    }

    public Map<String, String> getData() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("gun", gun.toString());
        data.put("center", Parse.Location(center));
        data.put("block1", Parse.Block(block1));
        data.put("block2", Parse.Block(block2));
        data.put("type", type.toString());
        data.put("direction", direction.toString());
        if (secondaryDirection != null) {
            data.put("secondaryDirection", secondaryDirection.toString());
        }
        return data;
    }
}
