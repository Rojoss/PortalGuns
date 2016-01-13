package com.jroossien.portalguns.portals;

import com.jroossien.portalguns.PortalGuns;
import com.jroossien.portalguns.config.PortalCfg;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PortalManager {

    private PortalGuns pg;
    private PortalCfg cfg;
    private Map<UUID, PortalData> portals = new HashMap<UUID, PortalData>();

    public PortalManager(PortalGuns pg) {
        this.pg = pg;
        this.cfg = pg.getPortalCfg();
        loadPortals();
    }

    public void loadPortals() {
        portals.clear();
        int removals = 0;
        Map<String, Map<String, String>> cfgPortals = new HashMap<String, Map<String, String>>(cfg.portals);
        for (Map.Entry<String, Map<String, String>> entry : cfgPortals.entrySet()) {
            UUID uid = UUID.fromString(entry.getKey());
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
        return portals.containsKey(uid);
    }

    public PortalData getPortal(UUID uid) {
        if (hasPortal(uid)) {
            return portals.get(uid);
        }
        return null;
    }

    public void deletePortal(UUID uid) {
        if (cfg.portals.containsKey(uid.toString())) {
            cfg.portals.remove(uid.toString());
            //TODO: Don't save for every deleted portal
            cfg.save();
        }
        if (portals.containsKey(uid)) {
            portals.remove(uid);
        }
    }

    public boolean createPortal(UUID gunUid, Location location, PortalIndex index, PortalDirection direction) {
        UUID uid = UUID.randomUUID();
        while (portals.containsKey(uid)) {
            uid = UUID.randomUUID();
        }

        PortalData data = new PortalData(uid, gunUid, location, index, direction);
        if (!data.isValid()) {
            return false;
        }

        portals.put(uid, data);
        cfg.portals.put(uid.toString(), data.getData());

        //TODO: Don't save for every portal creation.
        cfg.save();
        return true;
    }
}
