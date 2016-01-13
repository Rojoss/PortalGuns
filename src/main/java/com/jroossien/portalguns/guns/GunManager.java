package com.jroossien.portalguns.guns;

import com.jroossien.portalguns.PortalGuns;
import com.jroossien.portalguns.config.GunCfg;
import com.jroossien.portalguns.util.item.EItem;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
            UUID uid = UUID.fromString(entry.getKey());
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
        return guns.containsKey(uid);
    }

    public GunData getGun(UUID uid) {
        if (hasGun(uid)) {
            return guns.get(uid);
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

    public boolean createGun(GunType type, UUID owner) {
        UUID uid = UUID.randomUUID();
        while (guns.containsKey(uid)) {
            uid = UUID.randomUUID();
        }

        GunData data = new GunData(uid, type, owner);
        if (!data.isValid()) {
            return false;
        }

        guns.put(uid, data);
        cfg.guns.put(uid.toString(), data.getData());

        //TODO: Don't save for every gun creation.
        cfg.save();
        return true;
    }

    public EItem getGunItem() {
        return new EItem(Material.BREWING_STAND_ITEM).setName("&6&lPortal &9&lGun").makeGlowing(true);
    }
}
