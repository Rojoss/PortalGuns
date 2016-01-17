package com.jroossien.portalguns.listeners;

import com.jroossien.portalguns.PortalGuns;
import com.jroossien.portalguns.PortalType;
import com.jroossien.portalguns.config.messages.Msg;
import com.jroossien.portalguns.guns.GunData;
import com.jroossien.portalguns.portals.PortalData;
import com.jroossien.portalguns.util.ItemUtil;
import com.jroossien.portalguns.util.Parse;
import com.jroossien.portalguns.util.Str;
import com.jroossien.portalguns.util.Util;
import com.jroossien.portalguns.util.item.EItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

public class PortalListener implements Listener {

    private PortalGuns pg;
    private BlockFace[] sides = {BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH};

    public PortalListener(PortalGuns pg) {
        this.pg = pg;
    }

    @EventHandler
    private void usePortal(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockY() == event.getTo().getBlockY() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        for (PortalData portal : pg.getPM().getPortals().values()) {
            if (!portal.isValid()) {
                continue;
            }
            if (portal.onCooldown()) {
                return;
            }
            if (!portal.getBlock1().equals(event.getTo().getBlock()) && !portal.getBlock2().equals(event.getTo().getBlock())) {
                continue;
            }
            GunData gun = pg.getGM().getGun(portal.getGun());
            if (gun == null) {
                return;
            }

            //Get the other portal.
            PortalData otherportal = pg.getPM().getPortal(gun.getPortal(portal.getType() == PortalType.PRIMARY ? PortalType.SECONDARY : PortalType.PRIMARY));
            if (otherportal == null) {
                return;
            }

            //Put the other portal on cooldown to fix infinite looping with portals on the ground.
            otherportal.setCooldown(System.currentTimeMillis() + pg.getCfg().portal__fixDelay);

            //Get location and add some offset to prevent glitching in blocks.
            Location targetLoc = otherportal.getCenter().clone();
            //
            if (otherportal.getDirection() == BlockFace.DOWN) {
                targetLoc.add(0,-1.5f,0);
            } else if (otherportal.getDirection() != BlockFace.UP) {
                targetLoc.add(0,-1,0);
            }

            //Calculate pitch and yaw to look away from the portal.
            targetLoc.setYaw(Util.getYaw(otherportal.getDirection(), event.getPlayer().getLocation().getYaw()));
            targetLoc.setPitch(event.getPlayer().getLocation().getPitch());

            //Teleport!
            event.setTo(targetLoc);
            event.getPlayer().teleport(targetLoc);
            Vector velocity = new Vector(otherportal.getDirection().getModX(), otherportal.getDirection().getModY(), otherportal.getDirection().getModZ());
            event.getPlayer().setVelocity(velocity.multiply(0.2f));
            return;
        }
    }

    @EventHandler
    private void usePortalGun(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        EItem item = new EItem(event.getItem());

        //Validate the item.
        if (!ItemUtil.compare(item, pg.getGM().getBlankGunItem(), false, true, false, true)) {
            return;
        }
        if (item.getLore().isEmpty() || !Str.replaceColor(item.getLore().get(0)).startsWith(Msg.GUN_UID_PREFIX.getMsg()) || !Str.replaceColor(item.getLore().get(1)).startsWith(Msg.GUN_OWNER.getMsg())) {
            return;
        }

        //Block placing/using gun.
        event.setUseItemInHand(Event.Result.DENY);
        event.setCancelled(true);


        //Get the gun.
        UUID gunUid = Parse.UUID(Str.stripColor(item.getLore(0)));
        GunData gun = pg.getGM().getGun(gunUid);
        if (gun == null) {
            item.setName(Msg.INACTIVE_GUN.getMsg());
            player.setItemInHand(item);
            return;
        }

        //Check if gun is owned by the player.
        if (gun.getOwner() != null && !gun.getOwner().equals(player.getUniqueId())) {
            Bukkit.broadcastMessage("Not your gun!");
            //TODO: Fail..
            return;
        }
        //Update owner name if player changed name.
        if (gun.getOwner() != null) {
            item.setLore(1, Msg.GUN_OWNER.getMsg() + player.getName());
            player.setItemInHand(item);
        }

        //Control panel.
        if (player.isSneaking()) {
            pg.getControlPanel().show(player);
            return;
        }

        //Get block and block face either by clicking it or from a distance.
        boolean clickBlock = event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK;
        Block block = clickBlock ? event.getClickedBlock() : null;
        BlockFace face = clickBlock ? event.getBlockFace() : null;
        if (!clickBlock) {
            List<Block> blocks = player.getLastTwoTargetBlocks(ItemUtil.TRANSPARENT_MATERIALS, pg.getCfg().portalgun__maxUseDistance);
            block = blocks.get(1);
            face = blocks.get(1).getFace(blocks.get(0));
        }
        if (!canAttachPortal(block) || !canHavePortal(block.getRelative(face))) {
            Bukkit.broadcastMessage("Can't attach portal at block");
            //TODO: Fail...
            return;
        }


        //Try to get a nearby side block as a portal needs two blocks.
        Block side = getSideBlock(block, face);
        if (side == null ) {
            Bukkit.broadcastMessage("No side block to attach portal to");
            //TODO: Fail...
            return;
        }

        //Get the center location in front of the two blocks.
        Location center = Util.getCenter(block, side);
        Util.offsetLocation(center, face, 0.75f);

        //Get alternative block face for up and down portals so the particles can be created properly.
        BlockFace dir = null;
        if (face == BlockFace.UP || face == BlockFace.DOWN) {
            dir = side.getFace(block);
            if (dir == BlockFace.NORTH || dir == BlockFace.SOUTH) {
                dir = BlockFace.EAST;
            } else {
                dir = BlockFace.NORTH;
            }
        }

        //Get portal type
        PortalType type = PortalType.PRIMARY;
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            type = PortalType.SECONDARY;
        }


        //Move portal if gun already has a portal for the type.
        PortalData portal = pg.getPM().getPortal(gun.getPortal(type));
        if (portal != null) {
            portal.move(center, block.getRelative(face), side.getRelative(face), face, dir);
            //TODO: Don't save for each portal creation.
            pg.getPM().savePortal(portal, true);
            return;
        }

        //Try create new portal.
        portal = pg.getPM().createPortal(gunUid, center, block.getRelative(face), side.getRelative(face), type, face, dir);
        if (portal == null) {
            Bukkit.broadcastMessage("Failed creating new portal");
            //TODO: Fail...
            return;
        }
        gun.setPortal(type, portal.getUid());
        //TODO: Don't save for each portal creation.
        pg.getGM().saveGun(gun, true);
    }

    private Block getSideBlock(Block block, BlockFace face) {
        if (face == BlockFace.UP || face == BlockFace.DOWN) {
            //Find a block around the block that can have a portal attached.
            for (BlockFace side : sides) {
                Block sideBlock = block.getRelative(side);
                if (!canAttachPortal(sideBlock)) {
                    continue;
                }
                if (!canHavePortal(sideBlock.getRelative(face))) {
                    continue;
                }
                return sideBlock;
            }
            return null;
        } else {
            //Find a block above or underneath the block that can have a portal attached.
            Block sideBlock = block.getRelative(BlockFace.UP);
            if (canAttachPortal(sideBlock) && canHavePortal(sideBlock.getRelative(face))) {
                return sideBlock;
            }
            sideBlock = block.getRelative(BlockFace.DOWN);
            if (canAttachPortal(sideBlock) && canHavePortal(sideBlock.getRelative(face))) {
                return sideBlock;
            }
            return null;
        }
    }

    private boolean canHavePortal(Block block) {
        if (block == null) {
            return false;
        }
        if (!ItemUtil.TRANSPARENT_MATERIALS.contains(block.getType())) {
            return false;
        }
        return true;
    }

    private boolean canAttachPortal(Block block) {
        if (block == null) {
            return false;
        }
        if (ItemUtil.TRANSPARENT_MATERIALS.contains(block.getType())) {
            return false;
        }
        if (pg.getCfg().blockedPortalMaterials.contains(block.getType().toString())) {
            return false;
        }
        return true;
    }

}
