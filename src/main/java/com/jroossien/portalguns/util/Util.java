package com.jroossien.portalguns.util;

import com.jroossien.portalguns.PortalGuns;
import com.jroossien.portalguns.UserManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Util {

    public static final int MSEC_IN_DAY = 86400000;
    public static final int MSEC_IN_HOUR = 3600000;
    public static final int MSEC_IN_MIN = 60000;
    public static final int MSEC_IN_SEC = 1000;

    /**
     * Checks if the sender has the specified permission.
     * It will check recursively at starting at the bottom component of the permission.
     * So if the input permission is 'cmdsigns.signs.create.cmd' the player needs to have any of the following permissions.
     * cmdsigns.*
     * cmdsigns.signs.*
     * cmdsigns.signs.create.*
     * cmdsigns.signs.create.cmd
     * @param sender The sender to do the permission check on. (Remember that Player is a CommandSender!)
     * @param permission The permission to check. This should be the FULL permission string not a sub permission.
     * @return true if the sender has any of the permissions and false if not.
     */
    public static boolean hasPermission(CommandSender sender, String permission) {
        if (sender instanceof Player && UserManager.get().isAdmin(((Player)sender).getUniqueId())) {
            return true;
        }
        permission = permission.toLowerCase().trim();
        if (sender.hasPermission(permission)) {
            return true;
        }
        String[] components = permission.split("\\.");
        String perm = "";
        for (String component : components) {
            perm += component + ".";
            if (sender.hasPermission(perm + "*")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get yaw based on blockface.
     * For example for DOWN it would be 90 to look down too.
     * Only supports NORTH,EAST,SOUTH,WEST,UP & DOWN
     * @param dir The block face direction.
     * @param playerPitch When the block face isn't up or down it will return this value back.
     * @return Pitch based on direction.
     */
    public static float getPitch(BlockFace dir, float playerPitch) {
        if (dir == BlockFace.UP) {
            return -90;
        }
        if (dir == BlockFace.DOWN) {
            return 90;
        }
        return playerPitch;
    }

    /**
     * GEt yaw based on blockface.
     * For example for NORTH it would reutrn 180 degrees to look towards NORTH.
     * Only supports NORTH,EAST,SOUTH,WEST,UP & DOWN
     * @param dir The block face direction.
     * @param playerYaw  When the block face isn't sideways it will return this value back.
     * @return Yaw based on direction.
     */
    public static float getYaw(BlockFace dir, float playerYaw) {
        if (dir == BlockFace.NORTH) {
            return 180;
        }
        if (dir == BlockFace.EAST) {
            return 270;
        }
        if (dir == BlockFace.SOUTH) {
            return 0;
        }
        if (dir == BlockFace.WEST) {
            return 90;
        }
        return playerYaw;
    }

    public static float normalAngle(float angle) {
        while (angle <= -180) angle += 360;
        while (angle > 180) angle -= 360;
        return angle;
    }

    public static BlockFace yawToFace(float yaw) {
        yaw = Util.normalAngle(yaw);
        switch ((int) yaw) {
            case 0 : return BlockFace.NORTH;
            case 90 : return BlockFace.EAST;
            case 180 : return BlockFace.SOUTH;
            case 270 : return BlockFace.WEST;
        }
        //Let's apply angle differences
        if (yaw >= -45 && yaw < 45) {
            return BlockFace.NORTH;
        } else if (yaw >= 45 && yaw < 135) {
            return BlockFace.EAST;
        } else if (yaw >= -135 && yaw < -45) {
            return BlockFace.WEST;
        } else {
            return BlockFace.SOUTH;
        }
    }

    /**
     * Get the absolute center location between two blocks of the x,y,z axis.
     * @param block1 The first block.
     * @param block2 The second block.
     * @return The center location between the two blocks.
     */
    public static Location getCenter(Block block1, Block block2) {
        double x = (double)(block1.getX() + block2.getX() + 1) / 2;
        double y = (double)(block1.getY() + block2.getY() + 1) / 2;
        double z = (double)(block1.getZ() + block2.getZ() + 1) / 2;
        return new Location(block1.getWorld(), x, y, z);
    }

    /**
     * Offset a location relative to a certain direction/blockface.
     * @param location The location to offset (This instance gets modified).
     * @param direction The direction to offset to.
     * @param offset The amount of offset to add for example 0.5 adds half a block offset.
     * @return The location that was passed in. (Not a new location!)
     */
    public static Location offsetLocation(Location location, BlockFace direction, double offset) {
        location.add((double)direction.getModX() * offset, (double)direction.getModY() * offset, (double)direction.getModZ() * offset);
        return location;
    }

    public static double getMax(Vector v) {
        double max = Math.max(Math.max(v.getX(), v.getY()), v.getZ());
        double min = Math.abs(Math.min(Math.min(v.getX(), v.getY()), v.getZ()));
        return Math.max(max, min);
    }

    /**
     * Teleport an entity to a specific location.
     * All passengers and vehicles will be teleported too.
     * @param entity The entity to teleport. (Usually the player)
     * @param location The location to teleport the entity too.
     */
    public static void teleport(Entity entity, Location location, boolean telportLeashed, final TeleportCallback callback) {
        Location originalLoc = entity.getLocation();
        final List<Entity> entities = new ArrayList<Entity>();
        while (entity.isInsideVehicle() && entity.getVehicle() != null) {
            entity = entity.getVehicle();
        }
        entities.add(entity);
        while (entity.getPassenger() != null) {
            entity = entity.getPassenger();
            entities.add(entity);
        }

        Player player = null;
        List<Entity> nearbyEntities = entity.getNearbyEntities(20, 10, 20);
        for (Entity e : entities) {
            if (e.getPassenger() != null) {
                e.eject();
            }
            e.teleport(location);
            if (e instanceof Player) {
                player = (Player)e;
            }
        }

        if (telportLeashed) {
            if (player != null) {
                for (Entity nearby : nearbyEntities) {
                    if (nearby instanceof LivingEntity && ((LivingEntity)nearby).isLeashed() && ((LivingEntity)nearby).getLeashHolder().equals(player)) {
                        nearby.teleport(location);
                        ((LivingEntity) nearby).setLeashHolder(player);
                    }
                }
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < entities.size(); i++) {
                    if (i+1 < entities.size()) {
                        entities.get(i).setPassenger(entities.get(i+1));
                    }
                }
                callback.teleported(entities);
            }
        }.runTaskLater(PortalGuns.inst(), location.getWorld().equals(originalLoc.getWorld()) ? 1 : 10);
    }

    /**
     * Format a timestamp to a string with days/hours/mins/secs/ms.
     * For example: '%Dd %H:%M:%S' will be replaced with something like '1d 23:12:52'
     * The possible options in the syntax are:
     * %D = Days
     * %H = Hours
     * %M = Minutes
     * %S = Seconds
     * %MS = MilliSeconds
     * %% Remainder percentage of seconds with 1 decimal like '%S.%%s' could be '34.1s'
     * %%% Remainder percentage of seconds with 2 decimals like '%S.%%%s' could be '34.13s'
     * @param time The time to convert to min:sec
     * @param syntax The string with the above options which will be replaced with the time.
     * @return Formatted time string.
     */
    public static String formatTime(long time, String syntax, boolean extraZeros) {
        //time = time / 1000;

        int days = (int) time / MSEC_IN_DAY;
        time = time - days * MSEC_IN_DAY;

        int hours = (int) time / MSEC_IN_HOUR;
        time = time - hours * MSEC_IN_HOUR;

        int mins = (int) time / MSEC_IN_MIN;
        time = time - mins * MSEC_IN_MIN;

        int secs = (int) time / MSEC_IN_SEC;
        time = time - secs * MSEC_IN_SEC;

        int ms = (int) time;
        int ds = (int) time / 100;
        int fs = (int) time / 10;

        syntax = syntax.replace("%D", "" + days);
        syntax = syntax.replace("%H", "" + (hours < 10 && extraZeros ? "0"+hours : hours));
        syntax = syntax.replace("%M", "" + (mins < 10 && extraZeros ? "0"+mins : mins));
        syntax = syntax.replace("%S", "" + (secs < 10 && extraZeros ? "0"+secs : secs));
        syntax = syntax.replace("%MS", "" + ms);
        syntax = syntax.replace("%%%", "" + fs);
        syntax = syntax.replace("%%", "" + ds);
        return syntax;
    }
}
