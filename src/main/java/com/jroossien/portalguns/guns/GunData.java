package com.jroossien.portalguns.guns;

import com.jroossien.portalguns.PortalType;
import com.jroossien.portalguns.util.Parse;
import com.jroossien.portalguns.util.Str;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Player;

import java.util.*;

public class GunData {

    private UUID uid;
    private Short index;
    private GunType type;
    private UUID owner;
    private Short durability;
    private UUID primaryPortal;
    private UUID secondaryPortal;
    private Color primaryColor;
    private Color secondaryColor;
    private Long primaryCooldown;
    private Long secondaryCooldown;
    private List<UUID> shares = new ArrayList<UUID>();

    public GunData(UUID uid, Map<String, String> data) {
        this.uid = uid;
        if (data.containsKey("index")) {
            index = Parse.Short(data.get("index"));
        }
        if (data.containsKey("type")) {
            type = GunType.valueOf(data.get("type"));
        }
        if (data.containsKey("owner")) {
            owner = Parse.UUID(data.get("owner"));
        }
        if (data.containsKey("durability")) {
            durability = Parse.Short(data.get("durability"));
        }
        if (data.containsKey("primaryPortal")) {
            primaryPortal = Parse.UUID(data.get("primaryPortal"));
        }
        if (data.containsKey("secondaryPortal")) {
            secondaryPortal = Parse.UUID(data.get("secondaryPortal"));
        }
        if (data.containsKey("primaryColor")) {
            primaryColor = Parse.Color(data.get("primaryColor"));
        }
        if (data.containsKey("secondaryColor")) {
            secondaryColor = Parse.Color(data.get("secondaryColor"));
        }
        if (data.containsKey("primaryCooldown")) {
            primaryCooldown = Parse.Long(data.get("primaryCooldown"));
        }
        if (data.containsKey("secondaryCooldown")) {
            secondaryCooldown = Parse.Long(data.get("secondaryCooldown"));
        }
        if (data.containsKey("shares")) {
            String[] split = data.get("shares").split(",");
            for (String share : split) {
                UUID uuid = Parse.UUID(share);
                if (uuid != null) {
                    shares.add(uuid);
                }
            }
        }
    }

    public GunData(UUID uid, short index, GunType type) {
        this.uid = uid;
        this.index = index;
        this.type = type;
    }

    public GunData(UUID uid, short index, GunType type, UUID owner) {
        this.uid = uid;
        this.index = index;
        this.type = type;
        this.owner = owner;
    }


    public boolean isValid() {
        return uid != null && index != null && type != null && (type == GunType.GLOBAL || owner != null);
    }


    public UUID getUid() {
        return uid;
    }

    public Short getIndex() {
        return index;
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

    public Short getDurability() {
        return durability;
    }

    public void setDurability(short durability) {
        this.durability = durability;
    }

    public void decreaseDurability() {
        durability--;
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

    public boolean onCooldown(PortalType type) {
        if (getCooldown(type) == null) {
            return false;
        }
        return getCooldown(type) > System.currentTimeMillis();
    }

    public Long getCooldownTime(PortalType type) {
        if (getCooldown(type) == null) {
            return 0l;
        }
        return getCooldown(type) - System.currentTimeMillis();
    }

    public Long getCooldown(PortalType type) {
        if (type == PortalType.PRIMARY) {
            return getPrimaryCooldown();
        } else {
            return getSecondaryCooldown();
        }
    }

    public void setCooldown(PortalType type, Long cooldown) {
        if (type == PortalType.PRIMARY) {
            setPrimaryCooldown(cooldown);
        } else {
            setSecondaryCooldown(cooldown);
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

    public Long getPrimaryCooldown() {
        return primaryCooldown;
    }

    public void setPrimaryCooldown(Long primaryCooldown) {
        this.primaryCooldown = primaryCooldown;
    }

    public Long getSecondaryCooldown() {
        return secondaryCooldown;
    }

    public void setSecondaryCooldown(Long secondaryCooldown) {
        this.secondaryCooldown = secondaryCooldown;
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

    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<Player>();
        for (UUID uuid : shares) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                players.add(player);
            }
        }
        Player ownerPlayer = Bukkit.getPlayer(owner);
        if (ownerPlayer != null) {
            players.add(ownerPlayer);
        }
        return players;
    }


    public Map<String, String> getData() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("type", type.toString());
        if (index != null) {
            data.put("index", index.toString());
        }
        if (owner != null) {
            data.put("owner", owner.toString());
        }
        if (durability != null) {
            data.put("durability", durability.toString());
        }
        if (primaryPortal != null) {
            data.put("primaryPortal", primaryPortal.toString());
        }
        if (secondaryPortal != null) {
            data.put("secondaryPortal", secondaryPortal.toString());
        }
        if (primaryColor != null) {
            data.put("primaryColor", Parse.Color(primaryColor));
        }
        if (secondaryColor != null) {
            data.put("secondaryColor", Parse.Color(secondaryColor));
        }
        if (primaryCooldown != null) {
            data.put("primaryCooldown", primaryCooldown.toString());
        }
        if (secondaryCooldown != null) {
            data.put("secondaryCooldown", secondaryCooldown.toString());
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
