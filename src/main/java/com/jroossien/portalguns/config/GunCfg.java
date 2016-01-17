package com.jroossien.portalguns.config;

import com.jroossien.portalguns.PortalGuns;

import java.util.HashMap;
import java.util.Map;

public class GunCfg extends EasyConfig {

    private Long lastSave = System.currentTimeMillis();
    public Map<String, Map<String, String>> guns = new HashMap<String, Map<String, String>>();

    public GunCfg(String fileName) {
        this.setFile(fileName);
        load();
    }

    @Override
    public void save() {
        if (System.currentTimeMillis() < lastSave + PortalGuns.inst().getCfg().saveDelay) {
            return;
        }
        lastSave = System.currentTimeMillis();
        super.save();
    }

    public void forceSave() {
        super.save();
    }
}
