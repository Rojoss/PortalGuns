package com.jroossien.portalguns;

import com.jroossien.portalguns.commands.Commands;
import com.jroossien.portalguns.config.GunCfg;
import com.jroossien.portalguns.config.PluginCfg;
import com.jroossien.portalguns.config.PortalCfg;
import com.jroossien.portalguns.config.messages.MessageCfg;
import com.jroossien.portalguns.guns.GunManager;
import com.jroossien.portalguns.listeners.MainListener;
import com.jroossien.portalguns.portals.PortalManager;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class PortalGuns extends JavaPlugin {

    private static PortalGuns instance;
    private Vault vault;
    private Economy economy;

    private PluginCfg cfg;
    private MessageCfg msgCfg;
    private PortalCfg portalCfg;
    private GunCfg gunCfg;

    private PortalManager pm;
    private GunManager gm;

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
        portalCfg = new PortalCfg("plugins/PortalGuns/data/Portals.yml");
        gunCfg = new GunCfg("plugins/PortalGuns/data/Guns.yml");

        pm = new PortalManager(this);
        gm = new GunManager(this);

        cmds = new Commands(this);

        registerListeners();

        log("loaded successfully");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return cmds.onCommand(sender, cmd, label, args);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new MainListener(this), this);
    }

    public void log(Object msg) {
        log.info("[PortalGuns " + getDescription().getVersion() + "] " + msg.toString());
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

}
