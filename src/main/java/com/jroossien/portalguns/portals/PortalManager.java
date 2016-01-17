package com.jroossien.portalguns.portals;

import com.jroossien.portalguns.PortalGuns;
import com.jroossien.portalguns.PortalType;
import com.jroossien.portalguns.config.PortalCfg;
import com.jroossien.portalguns.guns.GunData;
import com.jroossien.portalguns.util.Parse;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PortalManager {

    private PortalGuns pg;
    private PortalCfg cfg;
    private PortalRunnable runnable;
    private Map<UUID, PortalData> portals = new HashMap<UUID, PortalData>();

    public PortalManager(PortalGuns pg) {
        this.pg = pg;
        this.cfg = pg.getPortalCfg();
        loadPortals();

        runnable = new PortalRunnable(this);
        runnable.runTaskTimer(pg, 1, 1);
    }

    public void loadPortals() {
        portals.clear();
        int removals = 0;
        Map<String, Map<String, String>> cfgPortals = new HashMap<String, Map<String, String>>(cfg.portals);
        for (Map.Entry<String, Map<String, String>> entry : cfgPortals.entrySet()) {
            UUID uid = Parse.UUID(entry.getKey());
            PortalData data = new PortalData(uid, entry.getValue());
            if (!data.isValid()) {
                cfg.portals.remove(entry.getKey());
                removals++;
                continue;
            }
            portals.put(uid, data);
        }
        if (removals > 0) {
            pg.log("Removed " + removals + " portals because corrupted data.");
            cfg.save();
        }
    }

    public boolean hasPortal(UUID uid) {
        if (uid == null) {
            return false;
        }
        return portals.containsKey(uid);
    }

    public PortalData getPortal(UUID uid) {
        if (hasPortal(uid)) {
            return portals.get(uid);
        }
        return null;
    }

    public void deletePortal(UUID uid) {
        if (uid == null) {
            return;
        }
        PortalData data = getPortal(uid);
        if (data != null) {
            GunData gun = pg.getGM().getGun(data.getGun());
            if (gun.getPrimaryPortal().equals(data.getUid())) {
                gun.setPrimaryPortal(null);
                pg.getGM().saveGun(gun, true);
            } else if (gun.getSecondaryPortal().equals(data.getUid())) {
                gun.setSecondaryPortal(null);
                pg.getGM().saveGun(gun, true);
            }
        }
        if (cfg.portals.containsKey(uid.toString())) {
            cfg.portals.remove(uid.toString());
            //TODO: Don't save for every deleted portal
            cfg.save();
        }
        if (portals.containsKey(uid)) {
            portals.remove(uid);
        }
    }

    public PortalData createPortal(UUID gunUid, Location center, Block block1, Block block2, PortalType type, BlockFace direction, BlockFace secondaryDirection) {
        UUID uid = UUID.randomUUID();
        while (portals.containsKey(uid)) {
            uid = UUID.randomUUID();
        }

        //TODO: Don't set portals to be not persistent by default.
        PortalData data = new PortalData(uid, gunUid, center, block1, block2, direction, secondaryDirection, type, false);
        if (!data.isValid()) {
            return null;
        }

        portals.put(uid, data);
        cfg.portals.put(uid.toString(), data.getData());

        //TODO: Don't save for every portal creation.
        cfg.save();
        return data;
    }

    public void savePortal(UUID uid, boolean saveConfig) {
        savePortal(getPortal(uid), saveConfig);
    }

    public void savePortal(PortalData data, boolean saveConfig) {
        if (data == null) {
            return;
        }
        portals.put(data.getUid(), data);
        cfg.portals.put(data.getUid().toString(), data.getData());
        if (saveConfig) {
            cfg.save();
        }
    }

    public Map<UUID, PortalData> getPortals() {
        return portals;
    }
}
