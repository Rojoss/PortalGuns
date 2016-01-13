package com.jroossien.portalguns.portals;

import com.jroossien.portalguns.util.Util;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PortalData {

    private UUID uid;
    private UUID gun;
    private Location location;
    private PortalIndex index;
    private PortalDirection direction;

    public PortalData(UUID uid, Map<String, String> data) {
        if (!data.containsKey("gun") || !data.containsKey("location") || !data.containsKey("index") || !data.containsKey("direction")) {
            return;
        }
        this.uid = uid;
        gun = UUID.fromString(data.get("gun"));
        location = Util.parseLocation(data.get("location"));
        index = PortalIndex.valueOf(data.get("index"));
        direction = PortalDirection.valueOf("direction");
    }

    public PortalData(UUID uid, UUID gun, Location location, PortalIndex index, PortalDirection direction) {
        this.uid = uid;
        this.gun = gun;
        this.location = location;
        this.index = index;
        this.direction = direction;
    }

    public boolean isValid() {
        return uid != null && gun != null && location != null && index != null && direction != null;
    }

    public UUID getUid() {
        return uid;
    }

    public void setUid(UUID uid) {
        this.uid = uid;
    }

    public UUID getGun() {
        return gun;
    }

    public void setGun(UUID gun) {
        this.gun = gun;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public PortalIndex getIndex() {
        return index;
    }

    public void setIndex(PortalIndex index) {
        this.index = index;
    }

    public PortalDirection getDirection() {
        return direction;
    }

    public void setDirection(PortalDirection direction) {
        this.direction = direction;
    }

    public Map<String, String> getData() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("gun", gun.toString());
        data.put("location", Util.parseLocation(location));
        data.put("index", index.toString());
        data.put("direction", direction.toString());
        return data;
    }
}
