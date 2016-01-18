package com.jroossien.portalguns.listeners;

import com.jroossien.portalguns.PortalGuns;
import com.jroossien.portalguns.guns.GunData;
import com.jroossien.portalguns.portals.PortalData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;

import java.util.ArrayList;
import java.util.List;

public class MainListener implements Listener {

    private PortalGuns pg;

    public MainListener(PortalGuns pg) {
        this.pg = pg;
    }

    @EventHandler
    private void textInput(AsyncPlayerChatEvent event) {
        if (!pg.getControlPanel().hasInput(event.getPlayer())) {
            return;
        }
        pg.getControlPanel().setInputResult(event.getPlayer(), event.getMessage());
        event.setCancelled(true);
    }

    @EventHandler
    private void onDisable(PluginDisableEvent event) {
        if (!pg.getCfg().cleanup__shutdown__destroy) {
            return;
        }
        List<PortalData> portals = new ArrayList<PortalData>(pg.getPM().getPortals().values());
        for (PortalData portal : portals) {
            if (!portal.isValid()) {
                return;
            }
            if (portal.isPersistent()) {
                return;
            }
            pg.getPM().deletePortal(portal.getUid());
        }
        pg.getPortalCfg().forceSave();
        pg.getGunCfg().forceSave();
    }

    @EventHandler
    private void playerJoin(PlayerJoinEvent event) {
        List<GunData> guns = pg.getGM().getPlayerGuns(event.getPlayer().getUniqueId());
        for (GunData gun : guns) {
            PortalData primary = pg.getPM().getPortal(gun.getPrimaryPortal());
            PortalData secondary = pg.getPM().getPortal(gun.getSecondaryPortal());
            if (primary != null && !secondary.isEnabled()) {
                primary.setEnabled(true);
                pg.getPM().savePortal(primary);
            }
            if (secondary != null && !secondary.isEnabled()) {
                secondary.setEnabled(true);
                pg.getPM().savePortal(secondary);
            }
        }
    }

    @EventHandler
    private void playerLeave(PlayerQuitEvent event) {
        if (!pg.getCfg().cleanup__logout__disable && !pg.getCfg().cleanup__logout__destroy) {
            return;
        }
        List<GunData> guns = pg.getGM().getPlayerGuns(event.getPlayer().getUniqueId());
        for (GunData gun : guns) {
            if (pg.getCfg().cleanup__logout__destroy) {
                pg.getPM().deletePortal(gun.getPrimaryPortal());
                pg.getPM().deletePortal(gun.getSecondaryPortal());
            } else if (pg.getCfg().cleanup__logout__disable) {
                PortalData primary = pg.getPM().getPortal(gun.getPrimaryPortal());
                PortalData secondary = pg.getPM().getPortal(gun.getSecondaryPortal());

                if (primary != null && primary.isEnabled() && (!primary.isPersistent() || pg.getCfg().cleanup__logout__disablePersistent)) {
                    primary.setEnabled(false);
                    pg.getPM().savePortal(primary);
                }
                if (secondary != null && secondary.isEnabled() && (!secondary.isPersistent() || pg.getCfg().cleanup__logout__disablePersistent)) {
                    secondary.setEnabled(false);
                    pg.getPM().savePortal(secondary);
                }
            }
        }
    }

    @EventHandler
    private void playerDeath(PlayerDeathEvent event) {
        if (!pg.getCfg().cleanup__death__destroy) {
            return;
        }
        List<GunData> guns = pg.getGM().getPlayerGuns(event.getEntity().getUniqueId());
        for (GunData gun : guns) {
            PortalData primary = pg.getPM().getPortal(gun.getPrimaryPortal());
            if (primary != null && !primary.isPersistent()) {
                pg.getPM().deletePortal(gun.getPrimaryPortal());
            }
            PortalData secondary = pg.getPM().getPortal(gun.getSecondaryPortal());
            if (primary != null && !secondary.isPersistent()) {
                pg.getPM().deletePortal(gun.getSecondaryPortal());
            }
        }
    }

}
