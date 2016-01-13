package com.jroossien.portalguns.commands;

import com.jroossien.portalguns.PortalGuns;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class Commands {
    private PortalGuns pg;

    public Commands(PortalGuns pg) {
        this.pg = pg;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (label.equalsIgnoreCase("portalguns") || label.equalsIgnoreCase("portalgun") || label.equalsIgnoreCase("pguns") || label.equalsIgnoreCase("pgun") ||
                label.equalsIgnoreCase("portalg") || label.equalsIgnoreCase("pg")) {

            return true;
        }
        return false;
    }

    private void showHelp(CommandSender sender) {

    }
}