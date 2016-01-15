package com.jroossien.portalguns.listeners;

import com.jroossien.portalguns.PortalGuns;
import com.jroossien.portalguns.config.messages.Msg;
import com.jroossien.portalguns.guns.GunData;
import com.jroossien.portalguns.guns.GunType;
import com.jroossien.portalguns.util.ItemUtil;
import com.jroossien.portalguns.util.Str;
import com.jroossien.portalguns.util.item.EItem;
import com.jroossien.portalguns.util.item.ItemParser;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class CraftListener implements Listener {

    private PortalGuns pg;
    private EItem blankGunItem = new EItem(Material.BREWING_STAND_ITEM).setName(Msg.GUN_NAME.getMsg()).addAllFlags(true).makeGlowing(true);

    public CraftListener(PortalGuns pg) {
        this.pg = pg;
    }

    @EventHandler
    private void invDrag(final InventoryDragEvent event) {
        //Cancel dragging more index items in (only allow clicking them in (See listener below))
        if (event.getInventory().getType() != InventoryType.WORKBENCH) {
            return;
        }
        boolean inGrid = false;
        for (int i = 1; i < 10; i++) {
            if (event.getInventorySlots().contains(i)) {
                inGrid = true;
                break;
            }
        }
        if (!inGrid) {
            return;
        }
        ItemStack result = event.getInventory().getItem(0);
        if (ItemUtil.compare(result, blankGunItem, false, false, false, true)) {
            event.setResult(Event.Result.DENY);
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void invClick(final InventoryClickEvent event) {
        //Fix to force PrepareCraftEvent when changing amounts of an item.
        if (event.getInventory().getType() != InventoryType.WORKBENCH) {
            return;
        }
        if (event.getSlot() > 9) {
            return;
        }
        if (event.getAction() != InventoryAction.PLACE_ONE && event.getAction() != InventoryAction.PLACE_SOME && event.getAction() != InventoryAction.PLACE_ALL && event.getAction() != InventoryAction.DROP_ONE_SLOT) {
            return;
        }
        if (ItemUtil.compare(event.getInventory().getItem(0), blankGunItem, false, false, false, true)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    //Setting the result to what it was calls PrepareCraftEvent.
                    event.setCurrentItem(event.getCurrentItem());
                }
            }.runTaskLater(pg, 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void prepareCraft(final PrepareItemCraftEvent event){
        final CraftingInventory inv = event.getInventory();
        final EItem result = new EItem(inv.getResult());
        if (ItemUtil.compare(result, blankGunItem, false, false, false, true)) {
            //Check if it's a global or personal gun and get the owner and index.
            UUID owner = null;
            short index = 1;
            if (Str.stripColor(result.getLore(0)).equalsIgnoreCase("Global")) {
                index = getIndex(inv.getMatrix(), pg.getRecipes().globalGun__indexItem);
            } else if (Str.stripColor(result.getLore(0)).equalsIgnoreCase("Personal")) {
                index = getIndex(inv.getMatrix(), pg.getRecipes().personalGun__indexItem);
                owner = event.getView().getPlayer().getUniqueId();
            }

            //Update the result based on the index and owner.
            final GunData gun = pg.getGM().getGun(owner, index);
            if (gun == null) {
                inv.setItem(0, blankGunItem.clone().setLore(result.getLore(0), Msg.CRAFT_NEW.getMsg()));
            } else {
                //TODO: Add more data to the lore like portal colors etc.
                inv.setItem(0, blankGunItem.clone().setLore(result.getLore(0), Msg.CRAFT_COPY.getMsg(), Msg.CRAFT_GUN_INFO_SEPARATOR.getMsg(),
                        Msg.GUN_UID_PREFIX.getMsg() + gun.getUid().toString(), Msg.GUN_OWNER.getMsg() +  (owner == null ? Msg.GLOBAL_OWNER.getMsg() : pg.getServer().getOfflinePlayer(owner).getName())));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void craft(final CraftItemEvent event) {
        final CraftingInventory inv = event.getInventory();
        final EItem result = new EItem(inv.getResult());
        if (ItemUtil.compare(result, blankGunItem, false, false, false, true)) {
            //Check if it's a global or personal gun and get the owner and index.
            UUID owner = null;
            short index = 1;
            GunType type = GunType.GLOBAL;
            if (Str.stripColor(result.getLore(0)).equalsIgnoreCase("Global")) {
                index = getIndex(inv.getMatrix(), pg.getRecipes().globalGun__indexItem);
            } else if (Str.stripColor(result.getLore(0)).equalsIgnoreCase("Personal")) {
                index = getIndex(inv.getMatrix(), pg.getRecipes().personalGun__indexItem);
                owner = event.getView().getPlayer().getUniqueId();
                type = GunType.PERSONAL;
            }
            //Get the gun and/or create a new one if needed.
            GunData gun = pg.getGM().getGun(owner, index);
            if (gun == null) {
                gun = pg.getGM().createGun(type, index, owner);
                if (gun == null) {
                    inv.setResult(ItemUtil.AIR);
                    event.setResult(Event.Result.DENY);
                    event.setCancelled(true);
                    return;
                }
            }
            inv.setResult(pg.getGM().getGunItem(gun.getUid()));
        }
    }

    private short getIndex(ItemStack[] contents, String indexItem) {
        MaterialData matData = ItemParser.getItem(indexItem);
        if (matData == null || matData.getItemType() == null || matData.getItemType() == Material.AIR) {
            return 1;
        }
        short count = 0;
        for (ItemStack item : contents) {
            if (item != null && item.getType() == matData.getItemType()) {
                count += item.getAmount();
            }
        }
        return count == 0 ? 1: count;
    }

    public EItem getGunItem(boolean global) {
        return blankGunItem.clone().setLore(global ? "&7Global" : "&7Personal");
    }
}
