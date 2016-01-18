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
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.*;

public class ControlPanel extends Menu {

    private PortalGuns pg;
    private GunManager gm;
    private PortalManager pm;

    private Map<UUID, UUID> guns = new HashMap<UUID, UUID>();
    private Map<UUID, String> input = new HashMap<UUID, String>();

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
        GunData gun = null;
        if (!guns.containsKey(player.getUniqueId())) {
            if (!ItemUtil.compare(player.getItemInHand(), gm.getBlankGunItem(), false, false, false, true)) {
                player.closeInventory();
                return;
            }
            EItem item = new EItem(player.getItemInHand());
            gun = gm.getGun(Parse.UUID(Str.stripColor(item.getLore().get(0))));
            if (gun == null || !gun.isValid()) {
                player.closeInventory();
                return;
            }
            guns.put(player.getUniqueId(), gun.getUid());
        } else {
            gun = gm.getGun(guns.get(player.getUniqueId()));
            if (gun == null || !gun.isValid()) {
                player.closeInventory();
                return;
            }
        }
        player.playSound(player.getLocation(), Sound.HORSE_ARMOR, 1, 2);
        updateContent(player, gun);
    }

    @Override
    protected void onClose(InventoryCloseEvent event) {
        ((Player)event.getPlayer()).playSound(event.getPlayer().getLocation(), Sound.HORSE_SADDLE, 1, 2);
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
            player.playSound(player.getLocation(), Sound.ITEM_BREAK, 0.5f, 2);
            return;
        }
        GunData gun = gm.getGun(guns.get(uuid));
        if (gun == null || !gun.isValid()) {
            player.playSound(player.getLocation(), Sound.ITEM_BREAK, 0.5f, 2);
            return;
        }
        int slot = event.getRawSlot();

        //Add/Remove shared players.
        if (slot == 3) {
            if (!Util.hasPermission(player, "portalguns.controlpanel.share")) {
                player.playSound(player.getLocation(), Sound.ITEM_BREAK, 0.5f, 2);
                Msg.CANT_SHARE.send(player);
                return;
            }
            if (gun.getType() == GunType.GLOBAL) {
                return;
            }
            String inputType = "addShare";
            if (event.isRightClick()) {
                inputType = "removeShare";
                Msg.INPUT_SHARES_REMOVE.send(player);
            } else {
                Msg.INPUT_SHARES_ADD.send(player);
            }

            player.playSound(player.getLocation(), Sound.NOTE_STICKS, 0.5f, 0);
            input.put(player.getUniqueId(), gun.getUid().toString() + ":" + inputType);
            player.closeInventory();
            return;
        }

        PortalType type = PortalType.PRIMARY;
        if ((slot > 4 && slot <=8) || (slot > 13 && slot <=17) || (slot > 22 && slot <=26) || (slot > 31 && slot <=35) || (slot > 40 && slot <=44) || (slot > 49 && slot <=53)) {
            type = PortalType.SECONDARY;
        }

        PortalData portal = pm.getPortal(gun.getPortal(type));
        if (portal == null) {
            player.playSound(player.getLocation(), Sound.ITEM_BREAK, 0.5f, 2);
            return;
        }
        boolean update = false;

        //Delete portal
        if (slot == 0 || slot == 8) {
            if (!Util.hasPermission(player, "portalguns.controlpanel.delete")) {
                player.playSound(player.getLocation(), Sound.ITEM_BREAK, 0.5f, 2);
                Msg.CANT_DESTROY.send(player);
                return;
            }
            player.playSound(player.getLocation(), Sound.NOTE_STICKS, 0.5f, 0);
            portal.getCenter().getWorld().playSound(portal.getCenter(), Sound.ZOMBIE_REMEDY, 1, 2);
            pm.deletePortal(gun.getPortal(type));
            updateContent(player, gun);
            return;
        }

        //Toggle persistent mode
        if (slot == 1 || slot == 7) {
            if (!Util.hasPermission(player, "portalguns.controlpanel.persistence")) {
                player.playSound(player.getLocation(), Sound.ITEM_BREAK, 0.5f, 2);
                Msg.CANT_TOGGLE_PERSISTENCE.send(player);
                return;
            }
            portal.setPersistent(!portal.isPersistent());
            update = true;
        }

        if (slot == 28 || slot == 32 || slot == 37 || slot == 41 || slot == 29 || slot == 33 || slot == 38 || slot == 42 || slot == 30 || slot == 34 || slot == 39 || slot == 43) {
            if (!Util.hasPermission(player, "portalguns.controlpanel.color")) {
                Msg.CANT_COLOR.send(player);
                player.playSound(player.getLocation(), Sound.ITEM_BREAK, 0.5f, 2);
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
            player.playSound(player.getLocation(), Sound.NOTE_STICKS, 0.5f, 0);
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
            setSlot(20, new EItem(Material.BANNER, 1, (short)4).setName(Msg.PRIMARY_NAME.getMsg()).setLore(Msg.PORTAL_DESC_INACTIVE.getMsg(Param.P("{type}", Msg.PRIMARY.getMsg()))), player);
        } else {
            Color color = gun.getColor(PortalType.PRIMARY);
            if (color == null) {
                color = pg.getCfg().getColor(PortalType.PRIMARY);
            }
            String clr = Msg.COLOR_FORMAT.getMsg(Param.P("{red}", color.getRed()), Param.P("{green}", color.getGreen()), Param.P("{blue}", color.getBlue()));
            setSlot(20, new EItem(Material.BANNER, 1, (short)4).setName(Msg.PRIMARY_NAME.getMsg())
                    .setLore(Msg.PORTAL_DESC.getMsg(Param.P("{type}", Msg.PRIMARY.getMsg()), Param.P("{color}", clr), Param.P("{location}", Parse.Location(primary.getBlock1().getLocation())))), player);
        }
        if (secondary == null) {
            setSlot(24, new EItem(Material.BANNER, 1, (short)14).setName(Msg.SECONDARY_NAME.getMsg()).setLore(Msg.PORTAL_DESC_INACTIVE.getMsg(Param.P("{type}", Msg.SECONDARY.getMsg()))), player);
        } else {
            Color color = gun.getColor(PortalType.SECONDARY);
            if (color == null) {
                color = pg.getCfg().getColor(PortalType.SECONDARY);
            }
            String clr = Msg.COLOR_FORMAT.getMsg(Param.P("{red}", color.getRed()), Param.P("{green}", color.getGreen()), Param.P("{blue}", color.getBlue()));
            setSlot(24, new EItem(Material.BANNER, 1, (short)14).setName(Msg.SECONDARY_NAME.getMsg())
                    .setLore(Msg.PORTAL_DESC.getMsg(Param.P("{type}", Msg.SECONDARY.getMsg()), Param.P("{color}", clr), Param.P("{location}", Parse.Location(secondary.getBlock1().getLocation())))), player);
        }


        //Glass panes
        fill(new EItem(Material.STAINED_GLASS_PANE, 1, (short)11).setName(Msg.SEPERATOR_LEFT.getMsg()), player, 9,10,11,12, 18, 27, 36, 45,46,47,48);
        fill(new EItem(Material.STAINED_GLASS_PANE, 1, (short)12).setName(Msg.SEPERATOR_CENTER.getMsg()), player, 13,22,31,40,49);
        fill(new EItem(Material.STAINED_GLASS_PANE, 1, (short)1).setName(Msg.SEPERATOR_RIGHT.getMsg()), player, 14,15,16,17, 26, 35, 44, 50,51,52,53);


        //Destroy portal
        setSlot(0, new EItem(Material.BARRIER).setName(Msg.DELETE_NAME.getMsg()).setLore(Msg.DELETE_DESC.getMsg(Param.P("{type}", Msg.PRIMARY.getMsg()))), player);
        setSlot(8, new EItem(Material.BARRIER).setName(Msg.DELETE_NAME.getMsg()).setLore(Msg.DELETE_DESC.getMsg(Param.P("{type}", Msg.SECONDARY.getMsg()))), player);

        //Persistent state
        if (primary != null) {
            if (primary.isPersistent()) {
                setSlot(1, new EItem(Material.BEDROCK).setName(Msg.PERSISTENT_NAME.getMsg()).setLore(Msg.PERSISTENT_DESC.getMsg()), player);
            } else {
                setSlot(1, new EItem(Material.GLASS).setName(Msg.NOT_PERSISTENT_NAME.getMsg()).setLore(Msg.NOT_PERSISTENT_DESC.getMsg()), player);
            }
        }
        if (secondary != null) {
            if (secondary.isPersistent()) {
                setSlot(7, new EItem(Material.BEDROCK).setName(Msg.PERSISTENT_NAME.getMsg()).setLore(Msg.PERSISTENT_DESC.getMsg()), player);
            } else {
                setSlot(7, new EItem(Material.GLASS).setName(Msg.NOT_PERSISTENT_NAME.getMsg()).setLore(Msg.NOT_PERSISTENT_DESC.getMsg()), player);
            }
        }

        //Manage shares
        if (gun.getType() != GunType.GLOBAL) {
            List<String> shares = new ArrayList<String>();
            for (UUID uuid : gun.getShares()) {
                String name = pg.getServer().getOfflinePlayer(uuid).getName();
                if (name != null && !name.trim().isEmpty()) {
                    shares.add(name);
                }
            }
            setSlot(3, new EItem(Material.NAME_TAG).setName(Msg.SHARES_NAME.getMsg())
                    .setLore(Msg.SHARES_DESC.getMsg(Param.P("{shares}", shares == null || shares.isEmpty() ? Msg.NOBODY.getMsg() : Str.wrapString(Str.implode(shares, ", ", " & "), 50)))), player);
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

    public boolean hasInput(Player player) {
        return input.containsKey(player.getUniqueId());
    }

    public void setInputResult(Player player, String string) {
        string = string.trim();
        UUID uuid = player.getUniqueId();
        String type = input.get(player.getUniqueId());

        //Disable input when q is typed.
        if (string.equalsIgnoreCase("q")) {
            input.remove(player.getUniqueId());
            Msg.INPUT_DISABLED.send(player);
            return;
        }

        String[] split = type.split(":");
        UUID gunUid = Parse.UUID(split[0]);
        GunData gun = pg.getGM().getGun(gunUid);
        if (gun == null || !gun.isValid()) {
            //This should never happen but just in case. (Don't want users getting stuck in input mode)
            input.remove(player.getUniqueId());
            Msg.INPUT_DISABLED.send(player);
            return;
        }
        type = split[1];

        //Add/remove player to shares.
        if (type.equalsIgnoreCase("addShare") || type.equalsIgnoreCase("removeShare")) {
            OfflinePlayer oPlayer = pg.getServer().getOfflinePlayer(string);
            if (oPlayer == null || !oPlayer.hasPlayedBefore()) {
                Msg.INPUT_INVALID_PLAYER.send(player);
                return;
            }
            if (type.equalsIgnoreCase("addShare")) {
                if (gun.getShares().contains(oPlayer.getUniqueId())) {
                    Msg.INPUT_ALREADY_SHARED.send(player);
                    return;
                }
                gun.addShare(oPlayer.getUniqueId());
            } else {
                if (!gun.getShares().contains(oPlayer.getUniqueId())) {
                    Msg.INPUT_NOT_SHARED.send(player);
                    return;
                }
                gun.removeShare(oPlayer.getUniqueId());
            }
            input.remove(player.getUniqueId());
            show(player);
            return;
        }

        //This should never happen but just in case. (Don't want users getting stuck in input mode)
        input.remove(player.getUniqueId());
        show(player);
    }
}
