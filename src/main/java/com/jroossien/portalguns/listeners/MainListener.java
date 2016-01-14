package com.jroossien.portalguns.listeners;

import com.jroossien.portalguns.PortalGuns;
import com.jroossien.portalguns.PortalType;
import com.jroossien.portalguns.guns.GunData;
import com.jroossien.portalguns.portals.PortalData;
import com.jroossien.portalguns.util.Util;
import com.jroossien.portalguns.util.particles.ParticleEffect;
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

public class MainListener implements Listener {

    private PortalGuns pg;
    private BlockFace[] sides = {BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH};

    public MainListener(PortalGuns pg) {
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
            if (!portal.getBlock1().equals(event.getTo().getBlock()) && !portal.getBlock2().equals(event.getTo().getBlock())) {
                continue;
            }
            GunData gun = pg.getGM().getGun(portal.getGun());
            if (gun == null) {
                return;
            }

            //Get the other portal location.
            PortalData otherportal = pg.getPM().getPortal(gun.getPortal(portal.getType() == PortalType.PRIMARY ? PortalType.SECONDARY : PortalType.PRIMARY));
            Location targetLoc = otherportal.getCenter().clone();

            //Add some offset so you don't spawn inside the portal.
            Vector offset = new Vector(otherportal.getDirection().getModX(), otherportal.getDirection().getModY(), otherportal.getDirection().getModZ());
            if (otherportal.getDirection() != BlockFace.UP) {
                offset.setY(offset.getY() - (otherportal.getDirection() == BlockFace.DOWN ? 1.5d : 1));
            }
            targetLoc.add(offset);

            //Calculate pitch and yaw to look away from the portal.
            targetLoc.setYaw(Util.getYaw(otherportal.getDirection(), event.getPlayer().getLocation().getYaw()));
            targetLoc.setPitch(Util.getPitch(otherportal.getDirection(), event.getPlayer().getLocation().getPitch()));

            //Teleport!
            event.setTo(targetLoc);
            return;
        }
    }

    @EventHandler
    private void usePortalGun(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        //Validate the item.
        //TODO: Do a proper portal item check.
        if (item == null || item.getType() != Material.BREWING_STAND_ITEM || !item.hasItemMeta()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName() && !meta.hasLore()) {
            return;
        }

        event.setUseItemInHand(Event.Result.DENY);
        event.setCancelled(true);

        //Get the gun.
        UUID gunUid = UUID.fromString(Util.stripAllColor(meta.getLore().get(0)));
        GunData gun = pg.getGM().getGun(gunUid);
        if (gun == null) {
            //TODO: Fail...
            return;
        }

        //Get block and block face either by clicking it or from a distance.
        boolean clickBlock = event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK;
        Block block = clickBlock ? event.getClickedBlock() : null;
        BlockFace face = clickBlock ? event.getBlockFace() : null;
        if (!clickBlock) {
            List<Block> blocks = player.getLastTwoTargetBlocks(Util.TRANSPARENT_MATERIALS, pg.getCfg().portalgun__maxUseDistance);
            block = blocks.get(1);
            face = blocks.get(1).getFace(blocks.get(0));
        }
        //TODO: Check if portal can be attached to this block.
        if (block == null || Util.TRANSPARENT_MATERIALS.contains(block.getType())) {
            //TODO: Fail...
            return;
        }

        //Try to get a nearby side block as a portal needs two blocks.
        Block side = getSideBlock(block, face);
        if (side == null ) {
            //TODO: Fail...
            return;
        }

        //Get the center location in front of the two blocks.
        Location center = getCenter(block, side, face);

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
            //TODO: Fail...
            return;
        }
        gun.setPortal(type, portal.getUid());

        //TODO: Don't save for each portal creation.
        pg.getGM().saveGun(gun, true);
    }

    public Block getSideBlock(Block block, BlockFace face) {
        if (face == BlockFace.UP || face == BlockFace.DOWN) {
            for (BlockFace side : sides) {
                Block sideBlock = block.getRelative(side);
                //TODO: Check if portal can be attached to this block.
                if (sideBlock.getType() == Material.AIR) {
                    continue;
                }
                if (!Util.TRANSPARENT_MATERIALS.contains(sideBlock.getRelative(face).getType())) {
                    continue;
                }
                return sideBlock;
            }
            return null;
        } else {
            Block sideBlock = block.getRelative(BlockFace.UP);
            //TODO: Check if portal can be attached to this block.
            if (sideBlock.getType() == Material.AIR || !Util.TRANSPARENT_MATERIALS.contains(sideBlock.getRelative(face).getType())) {
                //Try creating portal downwards
                sideBlock = block.getRelative(BlockFace.DOWN);
                //TODO: Check if portal can be attached to this block.
                if (sideBlock.getType() == Material.AIR || !Util.TRANSPARENT_MATERIALS.contains(sideBlock.getRelative(face).getType())) {
                    return null;
                }
            }
            return sideBlock;
        }
    }

    private Location getCenter(Block mainBlock, Block sideBlock, BlockFace face) {
        //Get the absolute center of the two blocks.
        double x = (double)(mainBlock.getX() + sideBlock.getX() + 1) / 2;
        double y = (double)(mainBlock.getY() + sideBlock.getY() + 1) / 2;
        double z = (double)(mainBlock.getZ() + sideBlock.getZ() + 1) / 2;

        //Add offset based on blockface.
        Location loc = new Location(mainBlock.getWorld(), x,y,z);
        loc.add((double)face.getModX()*0.7f, (double)face.getModY()*0.7f, (double)face.getModZ()*0.7f);

        return loc;
    }

}
