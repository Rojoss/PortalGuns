package com.jroossien.portalguns.util;

import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    private static Random random;
    public static final Set<Material> TRANSPARENT_MATERIALS = new HashSet<Material>();

    static {
        random = new Random();
        for (Material material : Material.values()) {
            if (material.isTransparent()) {
                TRANSPARENT_MATERIALS.add(material);
            }
        }
    }

    /**
     * Integrate ChatColor in a string based on color codes.
     * @param str The string to apply color to.
     * @return formatted string
     */
    public static String color(String str) {
        for (ChatColor c : ChatColor.values()) {
            str = str.replaceAll("&" + c.getChar() + "|&" + Character.toUpperCase(c.getChar()), "§" + c.getChar());
        }
        return str;
    }

    /**
     * Integrate ChatColor in a string based on color codes.
     * @param str The string to apply color to.
     * @return formatted string
     */
    public static String colorChatColors(String str) {
        for (ChatColor c : ChatColor.values()) {
            str = str.replaceAll("&" + c.getChar() + "|&" + Character.toUpperCase(c.getChar()), c.toString());
        }
        return str;
    }

    /**
     * Remove all color and put colors as the formatting codes like &amp;1.
     * @param str The string to remove color from.
     * @return formatted string
     */
    public static String removeColor(String str) {
        for (ChatColor c : ChatColor.values()) {
            str = str.replace(c.toString(), "&" + c.getChar());
        }
        return str;
    }

    /**
     * Strips all coloring from the specified string.
     * @param str The string to remove color from.
     * @return String without any colors and without any color symbols.
     */
    public static String stripAllColor(String str) {
        return ChatColor.stripColor(colorChatColors(str));
    }



    /**
     * Get a random number between start and end.
     * @param start
     * @param end
     * @return random int
     */
    public static int random(int start, int end) {
        return start + random.nextInt(end - start + 1);
    }

    /**
     * Get a random float number between start and end.
     * @param start
     * @param end
     * @return random float
     */
    public static float randomFloat(float start, float end) {
        return random.nextFloat() * (end - start) + start;
    }

    /**
     * Get a random float (Same as Random.nextFloat())
     * @return random float between 0-1
     */
    public static float randomFloat() {
        return random.nextFloat();
    }

    /**
     * Get a random value out of a Array.
     * @param array The array like String[] or int[]
     * @return Random value out of array.
     */
    public static <T> T random(T[] array) {
        return array[random(0, array.length-1)];
    }

    /**
     * Get a random value out of a List.
     * @param list The list like List<String>
     * @return Random value out of list.
     */
    public static <T> T random(List<T> list) {
        return list.get(random(0, list.size() - 1));
    }



    public static String implode(Object[] arr, String glue, String lastGlue, int start, int end) {
        String ret = "";

        if (arr == null || arr.length <= 0)
            return ret;

        for (int i = start; i <= end && i < arr.length; i++) {
            if (i >= end-1 || i >= arr.length-2) {
                ret += arr[i].toString() + lastGlue;
            } else {
                ret += arr[i].toString() + glue;
            }
        }

        if (ret.trim().isEmpty()) {
            return ret;
        }
        return ret.substring(0, ret.length() - lastGlue.length());
    }

    public static String implode(Object[] arr, String glue, int start) {
        return implode(arr, glue, glue, start, arr.length - 1);
    }

    public static String implode(Object[] arr, String glue, String lastGlue) {
        return implode(arr, glue, lastGlue, 0, arr.length - 1);
    }

    public static String implode(Object[] arr, String glue) {
        return implode(arr, glue, 0);
    }

    public static String implode(Collection<?> args, String glue) {
        if (args.isEmpty())
            return "";
        return implode(args.toArray(new Object[args.size()]), glue);
    }

    public static String implode(Collection<?> args, String glue, String lastGlue) {
        if (args.isEmpty())
            return "";
        return implode(args.toArray(new Object[args.size()]), glue, lastGlue);
    }



    public static Map<String, File> getFiles(File dir, final String extension) {
        Map<String, File> names = new HashMap<String, File>();
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return names;
        }

        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith("." + extension);
            }
        });

        for (File file : files) {
            names.put(file.getName().substring(0, file.getName().length() - extension.length() - 1), file);
        }

        return names;
    }


    public static List<String> splitNewLinesList(List<String> list) {
        if (list != null && list.size() > 0) {
            List<String> loreList = new ArrayList<String>();
            List<String> listClone = list;
            for (String string : listClone) {
                loreList.addAll(Arrays.asList(string.split("\n")));
            }
            return loreList;
        }
        return list;
    }


    /**
     * Splits the specified string in sections.
     * Strings inside quotes will be placed together in sections.
     * For example 'Essence is "super awesome"' will return {"essence", "is", "super awesome"}
     * @author sk89q, desht
     * @param string The string that needs to be split.
     * @return List of strings split from the input string.
     */
    public static List<String> splitQuotedString(String string) {
        List<String> sections = new ArrayList<String>();

        Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
        Matcher regexMatcher = regex.matcher(string);

        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null) {
                sections.add(regexMatcher.group(1));
            } else if (regexMatcher.group(2) != null) {
                sections.add(regexMatcher.group(2));
            } else {
                sections.add(regexMatcher.group());
            }
        }
        return sections;
    }


    public static double lerp(double start, double end, double perc) {
        if (Double.isNaN(perc) || perc > 1) {
            return end;
        } else if (perc < 0) {
            return start;
        } else {
            return start * (1 - perc) + end * perc;
        }
    }


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
