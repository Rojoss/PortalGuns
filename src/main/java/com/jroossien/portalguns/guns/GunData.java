package com.jroossien.portalguns.guns;

import com.jroossien.portalguns.PortalType;
import com.jroossien.portalguns.util.Parse;
import com.jroossien.portalguns.util.Str;
import com.jroossien.portalguns.util.Util;
import org.bukkit.Color;

import java.util.*;

public class GunData {

    private UUID uid;
    private GunType type;
    private UUID owner;
    private UUID primaryPortal;
    private UUID secondaryPortal;
    private Color primaryColor;
    private Color secondaryColor;
    private List<UUID> shares = new ArrayList<UUID>();

    public GunData(UUID uid, Map<String, String> data) {
        this.uid = uid;
        if (data.containsKey("type")) {
            type = GunType.valueOf(data.get("type"));
        }
        if (data.containsKey("owner")) {
            owner = UUID.fromString(data.get("owner"));
        }
        if (data.containsKey("primaryPortal")) {
            primaryPortal = UUID.fromString(data.get("primaryPortal"));
        }
        if (data.containsKey("secondaryPortal")) {
            secondaryPortal = UUID.fromString(data.get("secondaryPortal"));
        }
        if (data.containsKey("primaryColor")) {
            primaryColor = Parse.Color(data.get("primaryColor"));
        }
        if (data.containsKey("secondaryColor")) {
            secondaryColor = Parse.Color(data.get("secondaryColor"));
        }
        if (data.containsKey("shares")) {
            String[] split = data.get("shares").split(",");
            for (String share : split) {
                UUID uuid = UUID.fromString(share);
                if (uuid != null) {
                    shares.add(uuid);
                }
            }
        }
    }

    public GunData(UUID uid, GunType type) {
        this.uid = uid;
        this.type = type;
    }

    public GunData(UUID uid, GunType type, UUID owner) {
        this.uid = uid;
        this.type = type;
        this.owner = owner;
    }


    public boolean isValid() {
        return uid != null && type != null && (type == GunType.GLOBAL || owner != null);
    }


    public UUID getUid() {
        return uid;
    }

    public GunType getType() {
        return type;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public UUID getPortal(PortalType type) {
        if (type == PortalType.PRIMARY) {
            return getPrimaryPortal();
        }
        return getSecondaryPortal();
    }

    public void setPortal(PortalType type, UUID uid) {
        if (type == PortalType.PRIMARY) {
            setPrimaryPortal(uid);
        } else {
            setSecondaryPortal(uid);
        }
    }

    public Color getColor(PortalType type) {
        if (type == PortalType.PRIMARY) {
            return getPrimaryColor();
        }
        return getSecondaryColor();
    }

    public void setColor(PortalType type, Color color) {
        if (type == PortalType.PRIMARY) {
            setPrimaryColor(color);
        } else {
            setSecondaryColor(color);
        }
    }

    public UUID getPrimaryPortal() {
        return primaryPortal;
    }

    public void setPrimaryPortal(UUID uid) {
        this.primaryPortal = uid;
    }

    public UUID getSecondaryPortal() {
        return secondaryPortal;
    }

    public void setSecondaryPortal(UUID uid) {
        this.secondaryPortal = uid;
    }

    public Color getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(Color primaryColor) {
        this.primaryColor = primaryColor;
    }

    public Color getSecondaryColor() {
        return secondaryColor;
    }

    public void setSecondaryColor(Color secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    public void addShare(UUID player) {
        if (!shares.contains(player)) {
            shares.add(player);
        }
    }

    public void removeShare(UUID player) {
        if (shares.contains(player)) {
            shares.remove(player);
        }
    }

    public List<UUID> getShares() {
        return shares;
    }


    public Map<String, String> getData() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("type", type.toString());
        if (owner != null) {
            data.put("owner", owner.toString());
        }
        if (primaryPortal != null) {
            data.put("primaryPortal", primaryPortal.toString());
        }
        if (secondaryPortal != null) {
            data.put("secondaryPortal", secondaryPortal.toString());
        }
        if (primaryColor != null) {
            data.put("primaryColor", primaryColor.toString());
        }
        if (secondaryColor != null) {
            data.put("secondaryColor", secondaryColor.toString());
        }
        if (shares != null && !shares.isEmpty()) {
            List<String> shareList = new ArrayList<String>();
            for (UUID share : shares) {
                shareList.add(share.toString());
            }
            data.put("shares", Str.implode(shareList, ","));
        }
        return data;
    }
}
