package com.jroossien.portalguns.config;

import com.jroossien.portalguns.util.Parse;
import com.jroossien.portalguns.util.item.ItemParser;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import java.util.Arrays;
import java.util.List;

public class PluginCfg extends EasyConfig {

    private Color primary;
    private Color secondary;
    private MaterialData gunMatData;

    public int portalgun__maxUseDistance = 32;
    public String portalgun__primaryColor = "0,80,255";
    public String portalgun__secondaryColor = "255,200,0";
    public String portalgun__item = "BREWING_STAND_ITEM:0";

    public int portal__fixDelay = 2000;

    public List<String> blockedPortalMaterials = Arrays.asList("LAVA", "STATIONARY_LAVA", "WATER", "STATIONARY_WATER", "STANDING_BANNER", "BREWING_STAND", "BED_BLOCK", "SIGN_POST", "WALL_SIGN", "IRON_FENCE",
            "FENCE", "FENCE_GATE", "BIRCH_FENCE", "BIRCH_FENCE_GATE", "SPRUCE_FENCE", "SPRUCE_FENCE_GATE", "JUNGLE_FENCE", "JUNGLE_FENCE_GATE", "ACACIA_FENCE", "ACACIA_FENCE_GATE",
            "DARK_OAK_FENCE", "DARK_OAK_FENCE_GATE", "NETHER_FENCE", "FENCE_GATE", "COBBLE_WALL", "WOODEN_DOOR", "BIRCH_DOOR", "SPRUCE_DOOR", "JUNGLE_DOOR", "ACACIA_DOOR", "DARK_OAK_DOOR",
            "TRAP_DOOR", "IRON_TRAP_DOOR", "WEB", "WOOD_PLATE", "STONE_PLATE", "GOLD_PLATE", "IRON_PLATE", "ANVIL", "STEP", "WOOD_STEP", "STONE_SLAB2", "THIN_GLASS", "STAINED_GLASS_PANE");

    public PluginCfg(String fileName) {
        this.setFile(fileName);
        load();
    }

    @Override
    public void load() {
        primary = Parse.Color(portalgun__primaryColor);
        secondary = Parse.Color(portalgun__secondaryColor);

        gunMatData = ItemParser.getItem(portalgun__item);
        if (gunMatData == null || gunMatData.getItemType() == null) {
            gunMatData = new MaterialData(Material.BREWING_STAND_ITEM);
        }

        super.load();
    }

    public Color getColor(PortalType type) {
        if (type == PortalType.PRIMARY) {
            return primary;
        } else {
            return secondary;
        }
    }

    public MaterialData getGunMatData() {
        return gunMatData;
    }
}
