package com.jroossien.portalguns.util;

import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;

public class Util {

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
}
