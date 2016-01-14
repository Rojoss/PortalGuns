package com.jroossien.portalguns.config;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class RecipesCfg extends EasyConfig {

    public String personalGun__row1 = " #$";
    public String personalGun__row2 = "#/#";
    public String personalGun__row3 = "*# ";
    public String personalGun__indexItem = "STICK";
    public Map<String, Map<String, String>> personalGun__items = new HashMap<String, Map<String, String>>();

    public String globalGun__row1 = " #$";
    public String globalGun__row2 = "#/#";
    public String globalGun__row3 = "*# ";
    public String globalGun__indexItem = "STICK";
    public Map<String, Map<String, String>> globalGun__items = new HashMap<String, Map<String, String>>();

    public RecipesCfg(String fileName) {
        this.setFile(fileName);
        load();

        int changes = 0;
        if (personalGun__items.isEmpty()) {
            personalGun__items.put("1", getIngredient(Material.NETHER_STAR, "$"));
            personalGun__items.put("2", getIngredient(Material.IRON_INGOT, "#"));
            personalGun__items.put("3", getIngredient(Material.BLAZE_ROD, "/"));
            personalGun__items.put("indexItem", getIngredient(Material.STICK, "*"));
            changes++;
        }

        if (globalGun__items.isEmpty()) {
            globalGun__items.put("1", getIngredient(Material.NETHER_STAR, "$"));
            globalGun__items.put("2", getIngredient(Material.GOLD_INGOT, "#"));
            globalGun__items.put("3", getIngredient(Material.BLAZE_ROD, "/"));
            globalGun__items.put("indexItem", getIngredient(Material.STICK, "*"));
            changes++;
        }

        if (changes > 0) {
            save();
        }
    }

    private Map<String, String> getIngredient(Material material, String character) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("char", character);
        data.put("material", material.toString());
        return data;
    }
}
