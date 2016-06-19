package com.jroossien.portalguns.listeners;

import com.jroossien.portalguns.PortalGuns;
import com.jroossien.portalguns.PortalType;
import com.jroossien.portalguns.UserManager;
import com.jroossien.portalguns.config.messages.Msg;
import com.jroossien.portalguns.config.messages.Param;
import com.jroossien.portalguns.guns.GunData;
import com.jroossien.portalguns.guns.GunType;
import com.jroossien.portalguns.portals.PortalData;
import com.jroossien.portalguns.util.*;
import com.jroossien.portalguns.util.item.EItem;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class PortalListener implements Listener {

    private PortalGuns pg;
    private BlockFace[] sidesNorth = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
    private BlockFace[] sidesEast = {BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH};

    private Map<UUID, List<EItem>> itemDrops = new HashMap<UUID, List<EItem>>();

    public PortalListener(PortalGuns pg) {
        this.pg = pg;
    }

    @EventHandler
    private void usePortal(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockY() == event.getTo().getBlockY() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        PortalData portal = pg.getPM().getPortal(event.getTo().getBlock());
        if (portal == null) {
            return;
        }
        portal.use(event.getPlayer(), event.getPlayer(), event.getPlayer().getVelocity(), true);
    }

    @EventHandler
    private void dropItem(final PlayerDropItemEvent event) {
        if (!pg.getCfg().portal__teleport__itemsOnThrow) {
            return;
        }
        final Item item = event.getItemDrop();
        new BukkitRunnable() {
            int count = 0;
            Vector prevVel;
            @Override
            public void run() {
                if (item == null || !item.isValid() || item.isDead()) {
                    cancel();
                    return;
                }

                PortalData portal = pg.getPM().getPortal(item.getLocation().getBlock());
                if (portal != null) {
                    portal.use(event.getPlayer(), item, prevVel == null ? item.getVelocity() : prevVel, false);
                    cancel();
                    return;
                }

                if (item.isOnGround() || count > 40) {
                    cancel();
                    return;
                }
                prevVel = item.getVelocity();
                count++;
            }
        }.runTaskTimer(pg, 10, 3);
    }

    @EventHandler
    private void entityDamage(final EntityDamageByEntityEvent event) {
        if (!pg.getCfg().portal__teleport__entitiesOnDamage) {
            return;
        }
        Player damager = null;
        if (event.getDamager() instanceof Player) {
            damager = (Player)event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile)event.getDamager();
            if (proj.getShooter() instanceof Player) {
                damager = (Player)proj.getShooter();
            }
        }
        if (damager == null) {
            return;
        }

        final Entity entity = event.getEntity();
        final Player player = damager;
        new BukkitRunnable() {
            int count = 0;
            Vector prevVel;
            @Override
            public void run() {
                if (entity == null || !entity.isValid() || entity.isDead()) {
                    cancel();
                    return;
                }

                PortalData portal = pg.getPM().getPortal(entity.getLocation().getBlock());
                if (portal != null) {
                    portal.use(player, entity, prevVel == null ? entity.getVelocity() : prevVel, false);
                    cancel();
                    return;
                }

                if (count > 5) {
                    cancel();
                    return;
                }
                prevVel = entity.getVelocity();
                count++;
            }
        }.runTaskTimer(pg, 1, 3);
    }

    @EventHandler
    private void blockBreak(BlockBreakEvent event) {
        if (!pg.getCfg().portal__preventBreakingAttachedBlocks) {
            return;
        }
        if (Util.hasPermission(event.getPlayer(), "portalguns.bypass.breakattachedblock")) {
            return;
        }
        Location loc = event.getBlock().getLocation();
        for (PortalData portal : pg.getPM().getPortals().values()) {
            if (!portal.isValid()) {
                continue;
            }
            for (Block block : portal.getBlocks()) {
                if (block.getRelative(portal.getDirection().getOppositeFace()).getLocation().equals(loc)) {
                    Msg.CANT_BREAK_PORTAL_ATTACHED.send(event.getPlayer());
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    private void blockBuild(BlockPlaceEvent event) {
        if (!pg.getCfg().portal__preventBuildingInside) {
            return;
        }
        if (Util.hasPermission(event.getPlayer(), "portalguns.bypass.buildinportal")) {
            return;
        }
        Location loc = event.getBlock().getLocation();
        for (PortalData portal : pg.getPM().getPortals().values()) {
            if (!portal.isValid()) {
                continue;
            }
            for (Block block : portal.getBlocks()) {
                if (block.getLocation().equals(loc)) {
                    Msg.CANT_BUILD_IN_PORTAL.send(event.getPlayer());
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    private void dropPortalGun(PlayerDropItemEvent event) {
        EItem item = new EItem(event.getItemDrop().getItemStack());
        if (!pg.getCfg().portalgun__preventDrop) {
            return;
        }
        if (!ItemUtil.compare(item, pg.getGM().getBlankGunItem(), false, true, false, true)) {
            return;
        }
        if (item.getLore().isEmpty() || !Str.replaceColor(item.getLore().get(0)).startsWith(Msg.GUN_UID_PREFIX.getMsg()) || !Str.replaceColor(item.getLore().get(1)).startsWith(Msg.GUN_OWNER.getMsg())) {
            return;
        }
        if (Util.hasPermission(event.getPlayer(), "portalguns.bypass.dropgun")) {
            return;
        }
        event.setCancelled(true);
        Msg.CANT_DROP.send(event.getPlayer());
    }

    @EventHandler
    private void playerDeath(PlayerDeathEvent event) {
        if (!pg.getCfg().portalgun__keepOnDeath) {
            return;
        }
        Player player = event.getEntity();
        List<ItemStack> drops = new ArrayList<ItemStack>(event.getDrops());
        for (ItemStack drop : drops) {
            EItem item = new EItem(drop);
            if (!ItemUtil.compare(item, pg.getGM().getBlankGunItem(), false, true, false, true)) {
                continue;
            }
            if (item.getLore().isEmpty() || !Str.replaceColor(item.getLore().get(0)).startsWith(Msg.GUN_UID_PREFIX.getMsg()) || !Str.replaceColor(item.getLore().get(1)).startsWith(Msg.GUN_OWNER.getMsg())) {
                continue;
            }
            event.getDrops().remove(item);
            List<EItem> playerDrops = new ArrayList<EItem>();
            if (itemDrops.containsKey(player.getUniqueId())) {
                playerDrops = itemDrops.get(player.getUniqueId());
            }
            playerDrops.add(item);
            itemDrops.put(player.getUniqueId(), playerDrops);
        }
    }

    @EventHandler
    private void playerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!itemDrops.containsKey(player.getUniqueId())) {
            return;
        }
        for (EItem item : itemDrops.get(player.getUniqueId())) {
            ItemUtil.add(player.getInventory(), item, true, true);
        }
        itemDrops.remove(player.getUniqueId());
    }

    @EventHandler
    private void usePortalGun(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        //Prevent placing blocks by right clicking with portal gun in offhand
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.OFF_HAND) {
            EItem item = new EItem(player.getInventory().getItemInOffHand());
            if (!ItemUtil.compare(item, pg.getGM().getBlankGunItem(), false, true, false, true)) {
                return;
            }
            if (item.getLore().isEmpty() || !Str.replaceColor(item.getLore().get(0)).startsWith(Msg.GUN_UID_PREFIX.getMsg()) || !Str.replaceColor(item.getLore().get(1)).startsWith(Msg.GUN_OWNER.getMsg())) {
                return;
            }
            event.setUseItemInHand(Event.Result.DENY);
            event.setCancelled(true);
        }

        EItem item = new EItem(player.getInventory().getItemInMainHand());

        //Delete portals by shift right clicking blocks portal is attached too.
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.isSneaking()) {
            Location loc = event.getClickedBlock().getLocation();
            for (PortalData portal : pg.getPM().getPortals().values()) {
                if (!portal.isValid()) {
                    continue;
                }
                GunData gun = pg.getGM().getGun(portal.getGun());
                if (gun == null) {
                    continue;
                }
                if (gun.getOwner() != null && !gun.getOwner().equals(player.getUniqueId())) {
                    continue;
                }
                for (Block block : portal.getBlocks()) {
                    if (block.getRelative(portal.getDirection().getOppositeFace()).getLocation().equals(loc)) {
                        if (!Util.hasPermission(player, "portalguns.portal.destroy." + gun.getType().toString().toLowerCase())) {
                            Msg.CANT_DESTROY.send(player);
                            return;
                        }
                        pg.getSounds().getSound("portal-destroy").play(portal.getCenter().getWorld(), portal.getCenter());
                        portal.getCenter().getWorld().spawnParticle(Particle.SMOKE_NORMAL, portal.getCenter(), 30, 0.6f, 0.6f, 0.6f, 0);
                        pg.getPM().deletePortal(portal.getUid());
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

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

        if (!Util.hasPermission(event.getPlayer(), "portalguns.bypass.worldcheck")) {
            if (!pg.getCfg().worlds.contains(player.getWorld().getName())) {
                pg.getSounds().getSound("portalgun-fail").play(player);
                Msg.WORLD_NOT_LISTED.send(player);
                return;
            }
        }

        //Get the gun.
        UUID gunUid = Parse.UUID(Str.stripColor(item.getLore(0)));
        GunData gun = pg.getGM().getGun(gunUid);
        if (gun == null) {
            item.setName(Msg.INACTIVE_GUN.getMsg());
            player.setItemInHand(item);
            pg.getSounds().getSound("portalgun-break").play(player);
            return;
        }

        //Check if gun is owned by the player.
        if (gun.getOwner() != null && !gun.getOwner().equals(player.getUniqueId()) && !UserManager.get().isAdmin(player.getUniqueId())) {
            pg.getSounds().getSound("portalgun-fail").play(player);
            Msg.NOT_YOUR_GUN.send(player);
            return;
        }
        //Update owner name if player changed name.
        if (gun.getOwner() != null && gun.getOwner().equals(player.getUniqueId())) {
            item.setLore(1, Msg.GUN_OWNER.getMsg() + player.getName());
            player.setItemInHand(item);
        }

        //Control panel.
        if (player.isSneaking()) {
            if (!Util.hasPermission(player, "portalguns.controlpanel." + gun.getType().toString().toLowerCase())) {
                pg.getSounds().getSound("portalgun-fail").play(player);
                Msg.CANT_ACCESS_CONTROL_PANEL.send(player);
                return;
            }

            pg.getControlPanel().show(player);
            return;
        }

        if (!Util.hasPermission(player, "portalguns.portal.create." + gun.getType().toString().toLowerCase())) {
            pg.getSounds().getSound("portalgun-fail").play(player);
            Msg.CANT_CREATE_PORTALS.send(player);
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
            pg.getSounds().getSound("portalgun-fail").play(player);
            Msg.BLOCK_TYPE.send(player);
            return;
        }

        //Get portal type
        PortalType type = PortalType.PRIMARY;
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            type = PortalType.SECONDARY;
        }

        //Check distance between other portal (And check cross world portals)
        PortalData otherPortal = pg.getPM().getPortal(gun.getPortal(type.opposite()));
        if (otherPortal != null && otherPortal.isValid()) {
            Block otherBlock = otherPortal.getBlock1();
            if (!otherBlock.getWorld().equals(block.getWorld())) {
                if (!pg.getCfg().portal__allowCrossWorlds) {
                    pg.getSounds().getSound("portalgun-fail").play(player);
                    Msg.CROSS_WORLD.send(player);
                    return;
                }
            } else {
                if (!Util.hasPermission(event.getPlayer(), "portalguns.bypass.distancecheck")) {
                    int maxDistance = pg.getCfg().portal__maxDistance__personal;
                    if (gun.getType() == GunType.GLOBAL) {
                        maxDistance = pg.getCfg().portal__maxDistance__global;
                    }
                    if (maxDistance > 0 && otherBlock.getLocation().distance(block.getLocation()) > maxDistance) {
                        pg.getSounds().getSound("portalgun-fail").play(player);
                        Msg.DISTANCE.send(player);
                        return;
                    }
                }
            }
        }

        //Try to get a nearby side block as a portal needs two blocks.
        Block side = getSideBlock(block, face, Util.yawToFace(player.getLocation().getYaw()));
        if (side == null ) {
            pg.getSounds().getSound("portalgun-fail").play(player);
            Msg.NO_SIDE_BLOCK.send(player);
            return;
        }

        //Make sure the player can build the portal.
        if (pg.getCfg().portal__checkCanBuild && !Util.hasPermission(event.getPlayer(), "portalguns.bypass.buildcheck")) {
            BlockPlaceEvent blockPlaceEvent = new BlockPlaceEvent(block, block.getState(), block.getRelative(face.getOppositeFace()), item, player, true);
            pg.getServer().getPluginManager().callEvent(blockPlaceEvent);
            if (blockPlaceEvent.isCancelled() || !blockPlaceEvent.canBuild()) {
                pg.getSounds().getSound("portalgun-fail").play(player);
                Msg.CANT_BUILD.send(player);
                return;
            }
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

        //Gun cooldown
        int cooldownTime = gun.getType() == GunType.GLOBAL ? pg.getCfg().portalgun__cooldown__global : pg.getCfg().portalgun__cooldown__personal;
        if (cooldownTime > 0 && !Util.hasPermission(event.getPlayer(), "portalguns.bypass.cooldown")) {
            if (gun.onCooldown(type)) {
                pg.getSounds().getSound("portalgun-fail").play(player);
                String format = "";
                Long timeLeft = gun.getCooldownTime(type);
                if (timeLeft < Util.MSEC_IN_MIN) {
                    format = Msg.TIME_SECONDS.getMsg();
                } else if (timeLeft < Util.MSEC_IN_HOUR) {
                    format = Msg.TIME_MINUTES.getMsg();
                } else if (timeLeft < Util.MSEC_IN_DAY) {
                    format = Msg.TIME_HOURS.getMsg();
                } else if (timeLeft < Util.MSEC_IN_MIN) {
                    format = Msg.TIME_DAYS.getMsg();
                }
                if (!format.isEmpty()) {
                    Msg.ON_COOLDOWN.send(player, Param.P("{type}", type.toString().toLowerCase()), Param.P("{time}", Util.formatTime(timeLeft, format, false)));
                }
                return;
            }
            gun.setCooldown(type, System.currentTimeMillis() + (long)cooldownTime);
        }

        //Move portal if gun already has a portal for the type.
        PortalData portal = pg.getPM().getPortal(gun.getPortal(type));
        if (portal != null) {
            portal.move(center, block.getRelative(face), side.getRelative(face), face, dir);
            pg.getPM().savePortal(portal);
            pg.getGM().saveGun(gun);
            pg.getSounds().getSound("portal-create").play(player);
            if (!Util.hasPermission(event.getPlayer(), "portalguns.bypass.durability")) {
                player.setItemInHand(pg.getGM().decreaseDurability(item));
                if (player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR) {
                    pg.getSounds().getSound("portalgun-break").play(player);
                }
            }
            return;
        }

        //Try create new portal.
        portal = pg.getPM().createPortal(gunUid, center, block.getRelative(face), side.getRelative(face), type, face, dir);
        if (portal == null) {
            pg.getSounds().getSound("portalgun-fail").play(player);
            Msg.FAILED.send(player);
            return;
        }
        pg.getSounds().getSound("portal-create").play(player);
        gun.setPortal(type, portal.getUid());
        if (!Util.hasPermission(event.getPlayer(), "portalguns.bypass.durability")) {
            player.setItemInHand(pg.getGM().decreaseDurability(item));
            if (player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR) {
                pg.getSounds().getSound("portalgun-break").play(player);
            }
        }
        pg.getGM().saveGun(gun);
    }

    private Block getSideBlock(Block block, BlockFace face, BlockFace secondaryFace) {
        if (face == BlockFace.UP || face == BlockFace.DOWN) {
            BlockFace[] sides = secondaryFace == BlockFace.NORTH || secondaryFace == BlockFace.SOUTH ? sidesNorth : sidesEast;
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
