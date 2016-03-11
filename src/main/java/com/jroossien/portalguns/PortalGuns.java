package com.jroossien.portalguns;

import com.jroossien.portalguns.commands.Commands;
import com.jroossien.portalguns.config.*;
import com.jroossien.portalguns.config.messages.MessageCfg;
import com.jroossien.portalguns.guns.GunManager;
import com.jroossien.portalguns.listeners.CraftListener;
import com.jroossien.portalguns.listeners.MainListener;
import com.jroossien.portalguns.listeners.PortalListener;
import com.jroossien.portalguns.menu.ControlPanel;
import com.jroossien.portalguns.menu.Menu;
import com.jroossien.portalguns.portals.PortalData;
import com.jroossien.portalguns.portals.PortalManager;
import com.jroossien.portalguns.util.item.ItemParser;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class PortalGuns extends JavaPlugin {

    private static PortalGuns instance;
    private Vault vault;
    private Economy economy;

    private PluginCfg cfg;
    private MessageCfg msgCfg;
    private SoundsCfg soundsCfg;
    private PortalCfg portalCfg;
    private RecipesCfg recipesCfg;
    private GunCfg gunCfg;

    private PortalManager pm;
    private GunManager gm;

    private ControlPanel controlPanel;

    private CraftListener craftListener;

    private Commands cmds;

    private final Logger log = Logger.getLogger("PortalGuns");

    @Override
    public void onDisable() {
        instance = null;
        log("disabled");
    }

    @Override
    public void onEnable() {
        instance = this;
        log.setParent(this.getLogger());

        Plugin vaultPlugin = getServer().getPluginManager().getPlugin("Vault");
        if (vaultPlugin != null) {
            vault = (Vault)vaultPlugin;
            RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (economyProvider != null) {
                economy = economyProvider.getProvider();
            }
        }
        if (economy == null) {
            log("Failed to load Economy from Vault. The plugin will still work fine but some features might not work!");
        }

        cfg = new PluginCfg("plugins/PortalGuns/PortalGuns.yml");
        msgCfg = new MessageCfg("plugins/PortalGuns/Messages.yml");
        soundsCfg = new SoundsCfg("plugins/PortalGuns/Sounds.yml");
        recipesCfg = new RecipesCfg("plugins/PortalGuns/Recipes.yml");
        portalCfg = new PortalCfg("plugins/PortalGuns/data/Portals.yml");
        gunCfg = new GunCfg("plugins/PortalGuns/data/Guns.yml");

        pm = new PortalManager(this);
        gm = new GunManager(this);

        controlPanel = new ControlPanel(this);

        cmds = new Commands(this);

        registerListeners();
        registerRecipes();

        log("loaded successfully");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return cmds.onCommand(sender, cmd, label, args);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new Menu.Events(), this);
        getServer().getPluginManager().registerEvents(new MainListener(this), this);
        craftListener = new CraftListener(this);
        getServer().getPluginManager().registerEvents(craftListener, this);
        getServer().getPluginManager().registerEvents(new PortalListener(this), this);
    }

    private void registerRecipes() {
        if (!registerRecipe(craftListener.getGunItem(false), recipesCfg.personalGun__items, recipesCfg.personalGun__row1, recipesCfg.personalGun__row2, recipesCfg.personalGun__row3)) {
            warn("Failed to register the personal gun recipe! Check your Recipes.yml config for errors!");
        }
        if (!registerRecipe(craftListener.getGunItem(true), recipesCfg.globalGun__items, recipesCfg.globalGun__row1, recipesCfg.globalGun__row2, recipesCfg.globalGun__row3)) {
            warn("Failed to register the global gun recipe! Check your Recipes.yml config for errors!");
        }
    }

    private boolean registerRecipe(ItemStack result, Map<String, Map<String, String>> ingredients, String... shape) {
        ShapedRecipe recipe = new ShapedRecipe(result);
        recipe.shape(shape);
        for (Map<String, String> ingredient : ingredients.values()) {
            if (!ingredient.containsKey("material")) {
                return false;
            }
            MaterialData matData = ItemParser.getItem(ingredient.get("material"));
            if (matData == null || matData.getItemType() == null || matData.getItemType() == Material.AIR) {
                return false;
            }
            if (!ingredient.containsKey("char")) {
                return false;
            }
            String character = ingredient.get("char");
            if (character == null || character.isEmpty()) {
                return false;
            }
            recipe.setIngredient(ingredient.get("char").toCharArray()[0], matData.getItemType());
        }
        getServer().addRecipe(recipe);
        return true;
    }

    public void log(Object msg) {
        log.info("[PortalGuns " + getDescription().getVersion() + "] " + msg.toString());
    }

    public void warn(Object msg) {
        log.warning("[PortalGuns " + getDescription().getVersion() + "] " + msg.toString());
    }

    public static PortalGuns inst() {
        return instance;
    }

    public Vault getVault() {
        return vault;
    }

    public Economy getEco() {
        return economy;
    }


    public PluginCfg getCfg() {
        return cfg;
    }

    public MessageCfg getMsgCfg() {
        return msgCfg;
    }

    public SoundsCfg getSounds() {
        return soundsCfg;
    }

    public RecipesCfg getRecipes() {
        return recipesCfg;
    }

    public PortalCfg getPortalCfg() {
        return portalCfg;
    }

    public GunCfg getGunCfg() {
        return gunCfg;
    }


    public PortalManager getPM() {
        return pm;
    }

    public GunManager getGM() {
        return gm;
    }

    public ControlPanel getControlPanel() {
        return controlPanel;
    }

}
