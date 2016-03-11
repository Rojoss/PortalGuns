package com.jroossien.portalguns.config;

import java.util.HashMap;
import java.util.Map;

public class GunCfg extends EasyConfig {

    public Map<String, Map<String, String>> guns = new HashMap<String, Map<String, String>>();

    public GunCfg(String fileName) {
        this.setFile(fileName);
        load();
    }

    @Override
    public void save() {
        super.save();
    }
}
