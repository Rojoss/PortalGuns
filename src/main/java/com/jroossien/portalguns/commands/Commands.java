package com.jroossien.portalguns.commands;

import com.jroossien.portalguns.PortalGuns;
import com.jroossien.portalguns.guns.GunData;
import com.jroossien.portalguns.guns.GunType;
import com.jroossien.portalguns.util.Parse;
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

            //Temporary stuff for testing.
            if (args.length < 1) {
                sender.sendMessage("Specify gun index!");
                return true;
            }

            Player player = (Player)sender;
            GunData gun = pg.getGM().createGun(GunType.PERSONAL, Parse.Short(args[0]), player.getUniqueId());
            EItem item = pg.getGM().getGunItem(gun.getUid());
            player.getInventory().addItem(item);
            player.sendMessage("Gun given!");
            return true;
        }
        return false;
    }

    private void showHelp(CommandSender sender) {

    }
}