package com.jroossien.portalguns.config;

import com.jroossien.portalguns.util.Parse;
import org.bukkit.Color;

public class PluginCfg extends EasyConfig {

    private Color primary;
    private Color secondary;

    public int portalgun__maxUseDistance = 32;
    public String portalgun__primaryColor = "0,80,255";
    public String portalgun__secondaryColor = "255,200,0";

    public PluginCfg(String fileName) {
        this.setFile(fileName);
        load();
    }

    @Override
    public void load() {
        primary = Parse.Color(portalgun__primaryColor);
        secondary = Parse.Color(portalgun__secondaryColor);
        super.load();
    }

    public Color getPrimaryColor() {
        return primary;
    }

    public Color getSecondaryColor() {
        return secondary;
    }
}
