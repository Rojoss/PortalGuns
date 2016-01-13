package com.jroossien.portalguns.config;

import java.util.HashMap;
import java.util.Map;

public class PortalCfg extends EasyConfig {

    public Map<String, Map<String, String>> portals = new HashMap<String, Map<String, String>>();

    public PortalCfg(String fileName) {
        this.setFile(fileName);
        load();
    }
}
