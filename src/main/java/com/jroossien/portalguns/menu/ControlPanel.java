package com.jroossien.portalguns.menu;

import com.jroossien.portalguns.PortalGuns;
import com.jroossien.portalguns.PortalType;
import com.jroossien.portalguns.config.messages.Msg;
import com.jroossien.portalguns.config.messages.Param;
import com.jroossien.portalguns.guns.GunData;
import com.jroossien.portalguns.guns.GunManager;
import com.jroossien.portalguns.guns.GunType;
import com.jroossien.portalguns.portals.PortalData;
import com.jroossien.portalguns.portals.PortalManager;
import com.jroossien.portalguns.util.ItemUtil;
import com.jroossien.portalguns.util.Parse;
import com.jroossien.portalguns.util.Str;
import com.jroossien.portalguns.util.Util;
import com.jroossien.portalguns.util.item.EItem;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ControlPanel extends Menu {

    private PortalGuns pg;
    private GunManager gm;
    private PortalManager pm;

    private Map<UUID, UUID> guns = new HashMap<UUID, UUID>();

    public ControlPanel(PortalGuns pg) {
        super(pg, "control-panel", 6, Msg.MENU_TITLE.getMsg());
        this.pg = pg;
        this.gm = pg.getGM();
        this.pm = pg.getPM();
    }

    @Override
    protected void onDestroy() {}

    @Override
    protected void onShow(InventoryOpenEvent event) {
        Player player = (Player)event.getPlayer();
        if (!ItemUtil.compare(player.getItemInHand(), gm.getBlankGunItem(), false, false, false, true)) {
            player.closeInventory();
            return;
        }
        EItem item = new EItem(player.getItemInHand());
        GunData gun = gm.getGun(Parse.UUID(Str.stripColor(item.getLore().get(0))));
        if (gun == null || !gun.isValid()) {
            player.closeInventory();
            return;
        }
        guns.put(player.getUniqueId(), gun.getUid());
        updateContent(player, gun);
    }

    @Override
    protected void onClose(InventoryCloseEvent event) {
        if (guns.containsKey(event.getPlayer().getUniqueId())) {
            guns.remove(event.getPlayer().getUniqueId());
        }
    }

    @Override
    protected void onClick(InventoryClickEvent event) {
        event.setCancelled(true);

        Player player = (Player)event.getWhoClicked();
        UUID uuid = player.getUniqueId();

        if (!guns.containsKey(uuid)) {
            return;
        }
        GunData gun = gm.getGun(guns.get(uuid));
        if (gun == null || !gun.isValid()) {
            return;
        }
        int slot = event.getRawSlot();

        PortalType type = PortalType.PRIMARY;
        if ((slot > 4 && slot <=8) || (slot > 13 && slot <=17) || (slot > 22 && slot <=26) || (slot > 31 && slot <=35) || (slot > 40 && slot <=44) || (slot > 49 && slot <=53)) {
            type = PortalType.SECONDARY;
        }

        PortalData portal = pm.getPortal(gun.getPortal(type));
        if (portal == null) {
            //Fail...
            return;
        }
        boolean update = false;

        //Delete portal
        if (slot == 0 || slot == 8) {
            if (!Util.hasPermission(player, "portalguns.controlpanel.delete")) {
                return;
            }
            pm.deletePortal(gun.getPortal(type));
            updateContent(player, gun);
            return;
        }

        //Toggle persistent mode
        if (slot == 1 || slot == 7) {
            if (!Util.hasPermission(player, "portalguns.controlpanel.persistence")) {
                return;
            }
            portal.setPersistent(!portal.isPersistent());
            update = true;
        }

        //Add/Remove shared players.
        if (slot == 2 || slot == 6) {
            if (!Util.hasPermission(player, "portalguns.controlpanel.share")) {
                return;
            }
            if (gun.getType() == GunType.GLOBAL) {
                //Fail...
                return;
            }
            //TODO: Implement this.
            update = true;
        }

        if (slot == 28 || slot == 32 || slot == 37 || slot == 41 || slot == 29 || slot == 33 || slot == 38 || slot == 42 || slot == 30 || slot == 34 || slot == 39 || slot == 43) {
            if (Util.hasPermission(player, "portalguns.controlpanel.color")) {
                return;
            }

            Color color = gun.getColor(type);
            if (color == null) {
                color = pg.getCfg().getColor(type);
            }
            int change = 1;
            if (event.getClick() == ClickType.SHIFT_LEFT) {
                change = 5;
            } else if (event.getClick() == ClickType.RIGHT) {
                change = 20;
            } else if (event.getClick() == ClickType.SHIFT_RIGHT) {
                change = 50;
            }
            if (slot == 37 || slot == 38 || slot == 39 || slot == 41 || slot == 42 || slot == 43) {
                change *= -1;
            }

            if (slot == 28 || slot == 32 || slot == 37 || slot == 41) {
                color = color.setRed(Math.min(Math.max(color.getRed() + change, 0), 255));
                gun.setColor(type, color);
                update = true;
            } else if (slot == 29 || slot == 33 || slot == 38 || slot == 42) {
                color = color.setGreen(Math.min(Math.max(color.getGreen() + change, 0), 255));
                gun.setColor(type, color);
                update = true;
            } else if (slot == 30 || slot == 34 || slot == 39 || slot == 43) {
                color = color.setBlue(Math.min(Math.max(color.getBlue() + change, 0), 255));
                gun.setColor(type, color);
                update = true;
            }
        }

        if (update) {
            gm.saveGun(gun);
            pm.savePortal(portal);

            updateContent(player, gun);
        }
    }

    private void updateContent(Player player, GunData gun) {
        if (gun == null || !gun.isValid()) {
            player.closeInventory();
            return;
        }
        clearMenu(player);

        PortalData primary = pm.getPortal(gun.getPortal(PortalType.PRIMARY));
        PortalData secondary = pm.getPortal(gun.getPortal(PortalType.SECONDARY));

        //Gun
        setSlot(4, new EItem(player.getItemInHand()), player);

        //Portals
        if (primary == null) {
            setSlot(20, new EItem(Material.BANNER, 1, (short)4), player);
        } else {
            setSlot(20, new EItem(Material.BANNER, 1, (short)4).makeGlowing(true), player);
        }
        if (secondary == null) {
            setSlot(20, new EItem(Material.BANNER, 1, (short)14), player);
        } else {
            setSlot(24, new EItem(Material.BANNER, 1, (short)14).makeGlowing(true), player);
        }


        //Glass panes
        fill(new EItem(Material.STAINED_GLASS_PANE, 1, (short)11), player, 9,10,11,12, 18, 27, 36, 45,46,47,48);
        fill(new EItem(Material.STAINED_GLASS_PANE, 1, (short)12), player, 13,22,31,40,49);
        fill(new EItem(Material.STAINED_GLASS_PANE, 1, (short)1), player, 14,15,16,17, 26, 35, 44, 50,51,52,53);


        //Destroy portal
        setSlot(0, new EItem(Material.BARRIER), player);
        setSlot(8, new EItem(Material.BARRIER), player);

        //Persistent state
        if (primary != null) {
            if (primary.isPersistent()) {
                setSlot(1, new EItem(Material.BEDROCK), player);
            } else {
                setSlot(1, new EItem(Material.GLASS), player);
            }
        }
        if (secondary != null) {
            if (secondary.isPersistent()) {
                setSlot(7, new EItem(Material.BEDROCK), player);
            } else {
                setSlot(7, new EItem(Material.GLASS), player);
            }
        }

        //Manage shares
        if (gun.getType() != GunType.GLOBAL) {
            setSlot(2, new EItem(Material.NAME_TAG), player);
            setSlot(6, new EItem(Material.NAME_TAG), player);
        }


        //Color changing - Primary
        String descIncrease = Msg.COLOR_DESC.getMsg(Param.P("{type}", Msg.COLOR_DESC_INCREASE.getMsg()));
        String descDecrease = Msg.COLOR_DESC.getMsg(Param.P("{type}", Msg.COLOR_DESC_DECREASE.getMsg()));
        Color primaryColor = gun.getColor(PortalType.PRIMARY);
        if (primaryColor == null) {
            primaryColor = pg.getCfg().getColor(PortalType.PRIMARY);
        }
        setSlot(28, new EItem(Material.WOOL, 1, (short)14).setName("&4&l+" + Msg.RED.getMsg(Param.P("{value}", primaryColor.getRed()))).setLore(descIncrease), player);
        setSlot(29, new EItem(Material.WOOL, 1, (short)5).setName("&2&l+" + Msg.GREEN.getMsg(Param.P("{value}", primaryColor.getGreen()))).setLore(descIncrease), player);
        setSlot(30, new EItem(Material.WOOL, 1, (short)11).setName("&1&l+" + Msg.BLUE.getMsg(Param.P("{value}", primaryColor.getBlue()))).setLore(descIncrease), player);

        setSlot(37, new EItem(Material.WOOL, 1, (short)14).setName("&4&l-" + Msg.RED.getMsg(Param.P("{value}", primaryColor.getRed()))).setLore(descDecrease), player);
        setSlot(38, new EItem(Material.WOOL, 1, (short)5).setName("&2&l-" + Msg.GREEN.getMsg(Param.P("{value}", primaryColor.getGreen()))).setLore(descDecrease), player);
        setSlot(39, new EItem(Material.WOOL, 1, (short)11).setName("&1&l-" + Msg.BLUE.getMsg(Param.P("{value}", primaryColor.getBlue()))).setLore(descDecrease), player);

        //Color changing - Secondary
        Color secondaryColor = gun.getColor(PortalType.SECONDARY);
        if (secondaryColor == null) {
            secondaryColor = pg.getCfg().getColor(PortalType.SECONDARY);
        }
        setSlot(32, new EItem(Material.WOOL, 1, (short)14).setName("&4&l+" + Msg.RED.getMsg(Param.P("{value}", secondaryColor.getRed()))).setLore(descIncrease), player);
        setSlot(33, new EItem(Material.WOOL, 1, (short)5).setName("&2&l+" + Msg.GREEN.getMsg(Param.P("{value}", secondaryColor.getGreen()))).setLore(descIncrease), player);
        setSlot(34, new EItem(Material.WOOL, 1, (short)11).setName("&1&l+" + Msg.BLUE.getMsg(Param.P("{value}", secondaryColor.getBlue()))).setLore(descIncrease), player);

        setSlot(41, new EItem(Material.WOOL, 1, (short)14).setName("&4&l-" + Msg.RED.getMsg(Param.P("{value}", secondaryColor.getRed()))).setLore(descDecrease), player);
        setSlot(42, new EItem(Material.WOOL, 1, (short)5).setName("&2&l-" + Msg.GREEN.getMsg(Param.P("{value}", secondaryColor.getGreen()))).setLore(descDecrease), player);
        setSlot(43, new EItem(Material.WOOL, 1, (short)11).setName("&1&l-" + Msg.BLUE.getMsg(Param.P("{value}", secondaryColor.getBlue()))).setLore(descDecrease), player);
    }

    private void fill(EItem item, Player player, int... slots) {
        for (int slot : slots) {
            setSlot(slot, item, player);
        }
    }
}
