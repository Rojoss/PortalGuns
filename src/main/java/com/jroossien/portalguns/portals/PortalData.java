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

    //Stored data
    private UUID uid;
    private UUID gun;
    private Boolean enabled;
    private Location center;
    private Block block1;
    private Block block2;
    private BlockFace direction;
    private BlockFace secondaryDirection;
    private PortalType type;
    private Short durability;
    private Boolean persistent;

    //Internal data
    private Long cooldown;

    public PortalData(UUID uid, Map<String, String> data) {
        if (!data.containsKey("gun") || !data.containsKey("center") || !data.containsKey("block1") || !data.containsKey("block2") || !data.containsKey("type") || !data.containsKey("direction")) {
            return;
        }
        this.uid = uid;
        if (data.containsKey("gun")) {
            gun = Parse.UUID(data.get("gun"));
        }
        if (data.containsKey("enabled")) {
            enabled = Parse.Bool(data.get("enabled"));
        }
        if (data.containsKey("center")) {
            center = Parse.Location(data.get("center"));
        }
        if (data.containsKey("block1")) {
            block1 = Parse.Block(data.get("block1"));
        }
        if (data.containsKey("block2")) {
            block2 = Parse.Block(data.get("block2"));
        }
        if (data.containsKey("direction")) {
            direction = BlockFace.valueOf(data.get("direction"));
        }
        if (data.containsKey("secondaryDirection")) {
            secondaryDirection = BlockFace.valueOf(data.get("secondaryDirection"));
        }
        if (data.containsKey("type")) {
            type = PortalType.valueOf(data.get("type"));
        }
        if (data.containsKey("durability")) {
            durability = Parse.Short(data.get("durability"));
        }
        if (data.containsKey("persistent")) {
            persistent = Parse.Bool(data.get("persistent"));
        }
    }

    public PortalData(UUID uid, UUID gun, Location center, Block block1, Block block2, BlockFace direction, BlockFace secondaryDirection, PortalType type, Boolean persistent) {
        this.uid = uid;
        this.gun = gun;
        this.enabled = true;
        this.center = center;
        this.block1 = block1;
        this.block2 = block2;
        this.direction = direction;
        this.secondaryDirection = secondaryDirection;
        this.type = type;
        this.persistent = persistent;
    }

    public boolean isValid() {
        return uid != null && gun != null && enabled != null && center != null && block1 != null && block2 != null && direction != null && type != null && persistent != null;
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

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    public BlockFace getDirection() {
        return direction;
    }

    public BlockFace getSecondaryDirection() {
        return secondaryDirection;
    }

    public PortalType getType() {
        return type;
    }

    public Short getDurability() {
        return durability;
    }

    public void setDurability(short durability) {
        this.durability = durability;
    }

    public void decreaseDurability() {
        durability--;
    }

    public Boolean isPersistent() {
        return persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }


    public Long getCooldown() {
        return cooldown;
    }

    public void setCooldown(long cooldown) {
        this.cooldown = cooldown;
    }

    public boolean onCooldown() {
        if (cooldown == null) {
            return false;
        }
        return cooldown > System.currentTimeMillis();
    }

    public long getCooldownTime() {
        if (!onCooldown()) {
            return 0l;
        }
        return cooldown - System.currentTimeMillis();
    }


    public Map<String, String> getData() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("gun", gun.toString());
        data.put("enabled", enabled.toString());
        data.put("center", Parse.Location(center));
        data.put("block1", Parse.Block(block1));
        data.put("block2", Parse.Block(block2));
        data.put("type", type.toString());
        data.put("direction", direction.toString());
        if (secondaryDirection != null) {
            data.put("secondaryDirection", secondaryDirection.toString());
        }
        if (durability != null) {
            data.put("durability", durability.toString());
        }
        if (persistent != null) {
            data.put("persistent", persistent.toString());
        }
        return data;
    }
}
