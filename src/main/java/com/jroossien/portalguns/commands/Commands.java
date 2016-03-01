package com.jroossien.portalguns.commands;

import com.jroossien.portalguns.PortalGuns;
import com.jroossien.portalguns.UserManager;
import com.jroossien.portalguns.config.messages.Msg;
import com.jroossien.portalguns.config.messages.Param;
import com.jroossien.portalguns.guns.GunData;
import com.jroossien.portalguns.guns.GunType;
import com.jroossien.portalguns.util.*;
import com.jroossien.portalguns.util.item.EItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands {
    private PortalGuns pg;

    public Commands(PortalGuns pg) {
        this.pg = pg;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (label.equalsIgnoreCase("portalguns") || label.equalsIgnoreCase("portalgun") || label.equalsIgnoreCase("pguns") || label.equalsIgnoreCase("pgun") ||
                label.equalsIgnoreCase("portalg") || label.equalsIgnoreCase("pg")) {

            if (args.length < 1 || args[0].equalsIgnoreCase("help")) {
                Msg.HELP.send(sender);
                return true;
            }

            //Reload
            if (args[0].equalsIgnoreCase("reload")) {
                if (!Util.hasPermission(sender, "portalguns.cmd.reload")) {
                    Msg.NO_PERMISSION.send(sender);
                    return true;
                }

                pg.getCfg().load();
                pg.getRecipes().load();

                Msg.RELOADED.send(sender);
                return true;
            }

            //Info
            if (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("plugin") || args[0].equalsIgnoreCase("version")) {
                sender.sendMessage(Str.color("&8===== &4&lPortalGuns Plugin &8=====\n" +
                        "&6&lAuthor&8&l: &aRojoss&8(&7Jos&8)\n" +
                        "&6&lVersion&8&l: &a" + pg.getDescription().getVersion() + "\n" +
                        "&6&lSpigot URL&8&l: &9https://www.spigotmc.org/resources/17210"));
                return true;
            }

            //Admin
            if (args[0].equalsIgnoreCase("admin") || args[0].equalsIgnoreCase("a")) {
                if (!Util.hasPermission(sender, "portalguns.cmd.admin")) {
                    Msg.NO_PERMISSION.send(sender);
                    return true;
                }
                if (args.length < 2 && !(sender instanceof Player)) {
                    Msg.INVALID_USAGE.send(sender, Param.P("{usage}", "/" + label + " " + args[0] + " {player}"));
                    return true;
                }

                Player player = args.length > 1 ? pg.getServer().getPlayer(args[1]) : (Player)sender;
                if (player == null) {
                    Msg.INVALID_ONLINE_PLAYER.send(sender);
                    return true;
                }

                if (UserManager.get().isAdmin(player.getUniqueId())) {
                    UserManager.get().removeAdmin(player.getUniqueId());
                    Msg.ADMIN_DISABLED.send(player);
                } else {
                    UserManager.get().addAdmin(player.getUniqueId());
                    Msg.ADMIN_ENABLED.send(player);
                }
                if (!sender.equals(player)) {
                    Msg.ADMIN_TOGGLE_OTHER.send(sender, Param.P("{player}", player.getName()), Param.P("{type}",  UserManager.get().isAdmin(player.getUniqueId()) ? Msg.ENABLED.getMsg() : Msg.DISABLED.getMsg()));
                }
                return true;
            }

            //Give
            if (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("spawn") || args[0].equalsIgnoreCase("gun")) {
                if (!Util.hasPermission(sender, "portalguns.cmd.give")) {
                    Msg.NO_PERMISSION.send(sender);
                    return true;
                }
                if (args.length < 4) {
                    Msg.INVALID_USAGE.send(sender, Param.P("{usage}", "/" + label + " " + args[0] + " {player} {type} {index}"));
                    return true;
                }

                Player player = pg.getServer().getPlayer(args[1]);
                if (player == null) {
                    Msg.INVALID_ONLINE_PLAYER.send(sender);
                    return true;
                }

                GunType type = null;
                if (args[2].equalsIgnoreCase("personal") || args[2].equalsIgnoreCase("p")) {
                    type = GunType.PERSONAL;
                } else if (args[2].equalsIgnoreCase("global") || args[2].equalsIgnoreCase("g")) {
                    type = GunType.GLOBAL;
                } else {
                    Msg.INVALID_TYPE.send(sender);
                    return true;
                }

                Short index = Parse.Short(args[3]);
                if (index == null) {
                    Msg.NOT_A_NUMBER.send(sender, Param.P("{input}", args[3]));
                    return true;
                }
                if (index <= 0) {
                    index = pg.getGM().getAvailableIndex(type == GunType.GLOBAL ? null : player.getUniqueId());
                }

                GunData gun = pg.getGM().createGun(type, index, type == GunType.GLOBAL ? null : player.getUniqueId());
                if (gun == null || !gun.isValid()) {
                    Msg.FAILED_GUN.send(sender);
                    return true;
                }
                EItem item = pg.getGM().getGunItem(gun.getUid());
                ItemUtil.add(player.getInventory(), item, true, false);

                Msg.GUN_RECEIVED.send(player, Param.P("{type}", type.toString().toLowerCase()), Param.P("{index}", index));
                if (!player.equals(sender)) {
                    Msg.GUN_GIVEN.send(sender, Param.P("{type}", type.toString().toLowerCase()), Param.P("{index}", index), Param.P("{player}", player.getName()));
                }
                return true;
            }

            Msg.HELP.send(sender);
            return true;
        }
        return false;
    }
}