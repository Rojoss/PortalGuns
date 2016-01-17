package com.jroossien.portalguns.guns;

import com.jroossien.portalguns.PortalGuns;
import com.jroossien.portalguns.config.GunCfg;
import com.jroossien.portalguns.config.messages.Msg;
import com.jroossien.portalguns.config.messages.Param;
import com.jroossien.portalguns.util.ItemUtil;
import com.jroossien.portalguns.util.Parse;
import com.jroossien.portalguns.util.Str;
import com.jroossien.portalguns.util.item.EItem;

import java.util.*;

public class GunManager {

    private PortalGuns pg;
    private GunCfg cfg;
    private Map<UUID, GunData> guns = new HashMap<UUID, GunData>();

    public GunManager(PortalGuns pg) {
        this.pg = pg;
        this.cfg = pg.getGunCfg();
        loadGuns();
    }

    public void loadGuns() {
        guns.clear();
        int removals = 0;
        Map<String, Map<String, String>> cfgGuns = new HashMap<String, Map<String, String>>(cfg.guns);
        for (Map.Entry<String, Map<String, String>> entry : cfgGuns.entrySet()) {
            UUID uid = Parse.UUID(entry.getKey());
            GunData data = new GunData(uid, entry.getValue());
            if (!data.isValid()) {
                cfg.guns.remove(entry.getKey());
                removals++;
                continue;
            }
            guns.put(uid, data);
        }
        if (removals > 0) {
            pg.log("Removed " + removals + " guns because corrupted data.");
            cfg.save();
        }
    }

    public boolean hasGun(UUID uid) {
        if (uid == null) {
            return false;
        }
        return guns.containsKey(uid);
    }

    public GunData getGun(UUID uid) {
        if (hasGun(uid)) {
            return guns.get(uid);
        }
        return null;
    }

    public GunData getGun(UUID owner, short index) {
        for (GunData gun : guns.values()) {
            if (!gun.isValid()) {
                continue;
            }
            if (gun.getIndex() != index) {
                continue;
            }
            if ((gun.getOwner() == null && owner == null) || (gun.getOwner() != null && gun.getOwner().equals(owner))) {
                return gun;
            }
        }
        return null;
    }

    public List<GunData> getPlayerGuns(UUID player) {
        List<GunData> playerGuns = new ArrayList<GunData>();
        for (GunData gun : guns.values()) {
            if (!gun.isValid()) {
                continue;
            }
            if (gun.getOwner() == null || !gun.getOwner().equals(player)) {
                continue;
            }
            playerGuns.add(gun);
        }
        return playerGuns;
    }

    public List<GunData> getGlobalGuns() {
        List<GunData> globalGuns = new ArrayList<GunData>();
        for (GunData gun : guns.values()) {
            if (!gun.isValid()) {
                continue;
            }
            if (gun.getType() != GunType.GLOBAL) {
                continue;
            }
            globalGuns.add(gun);
        }
        return globalGuns;
    }

    public Short getAvailableIndex(UUID owner) {
        List<GunData> gunList;
        if (owner == null) {
            gunList = getGlobalGuns();
        } else {
            gunList = getPlayerGuns(owner);
        }
        List<Short> indexes = new ArrayList<Short>();
        for (GunData gun : gunList) {
            if (gun.getIndex() == null) {
                continue;
            }
            indexes.add(gun.getIndex());
        }
        //256 hard limit for guns (per player/global)
        for (short i = 0; i < 256; i++) {
            if (!indexes.contains(i)) {
                return i;
            }
        }
        return null;
    }

    public void deleteGun(UUID uid) {
        if (cfg.guns.containsKey(uid.toString())) {
            cfg.guns.remove(uid.toString());
            //TODO: Don't save for every deleted gun
            cfg.save();
        }
        if (guns.containsKey(uid)) {
            guns.remove(uid);
        }
    }

    public GunData createGun(GunType type, short index, UUID owner) {
        UUID uid = UUID.randomUUID();
        while (guns.containsKey(uid)) {
            uid = UUID.randomUUID();
        }

        GunData data = getGun(owner, index);
        if (data != null) {
            return data;
        }

        data = new GunData(uid, index, type, owner);
        if (!data.isValid()) {
            return null;
        }

        guns.put(uid, data);
        cfg.guns.put(uid.toString(), data.getData());

        //TODO: Don't save for every gun creation.
        cfg.save();
        return data;
    }

    public void saveGun(UUID uid, boolean saveConfig) {
        saveGun(getGun(uid), saveConfig);
    }

    public void saveGun(GunData data, boolean saveConfig) {
        if (data == null) {
            return;
        }
        guns.put(data.getUid(), data);
        cfg.guns.put(data.getUid().toString(), data.getData());
        if (saveConfig) {
            cfg.save();
        }
    }

    public EItem decreaseDurability(EItem gun) {
        String durabilityString = Str.replaceColor(gun.getLore(2));
        if (!durabilityString.startsWith(Msg.GUN_DURABILITY_PREFIX.getMsg())) {
            return gun;
        }
        durabilityString = Str.stripColor(durabilityString);
        String[] split = durabilityString.split(Str.stripColor(Msg.GUN_DURABILITY_SEPERATOR.getMsg()));
        if (split.length < 2) {
            return gun;
        }
        Integer durability = Parse.Int(split[0]);
        if (durability == null) {
            return gun;
        }
        durability--;
        if (durability <= 0) {
            return EItem.AIR;
        }
        gun.setLore(2, Msg.GUN_DURABILITY_PREFIX.getMsg() + durability + Msg.GUN_DURABILITY_SEPERATOR.getMsg() + Msg.GUN_DURABILITY_SUFFIX.getMsg() + split[1]);
        return gun;
    }

    public EItem getBlankGunItem() {
        return new EItem(pg.getCfg().getGunMatData().getItemType(), 1, (short)pg.getCfg().getGunMatData().getData()).setName(Msg.GUN_NAME.getMsg())
                .setLore(Msg.GUN_UID_PREFIX.getMsg(), Msg.GUN_OWNER.getMsg(), Msg.GUN_DESCRIPTION.getMsg()).makeGlowing(true).addAllFlags(true);
    }

    public EItem getGunItem(UUID gun) {
        GunData data = getGun(gun);
        if (data == null) {
            return null;
        }

        short durability = 0;
        if (data.getType() == GunType.GLOBAL) {
            durability = (short)pg.getCfg().portalgun__durability__global;
        } else {
            durability = (short)pg.getCfg().portalgun__durability__personal;
        }

        return new EItem(pg.getCfg().getGunMatData().getItemType(), 1, (short)pg.getCfg().getGunMatData().getData()).setName(Msg.GUN_NAME.getMsg()).setLore(
                Msg.GUN_UID_PREFIX.getMsg() + gun.toString(), Msg.GUN_OWNER.getMsg() + (data.getOwner() == null ? Msg.GLOBAL_OWNER.getMsg() : pg.getServer().getOfflinePlayer(data.getOwner()).getName()),
                (durability > 0 ? Msg.GUN_DURABILITY_PREFIX.getMsg() + durability + Msg.GUN_DURABILITY_SEPERATOR.getMsg() + Msg.GUN_DURABILITY_SUFFIX.getMsg() + durability + "\n" + Msg.GUN_DESCRIPTION.getMsg() :
                        Msg.GUN_DESCRIPTION.getMsg())).makeGlowing(true).addAllFlags(true);
    }
}
