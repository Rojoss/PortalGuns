package com.jroossien.portalguns;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserManager {

    private static UserManager instance;

    private List<UUID> admins = new ArrayList<UUID>();

    public UserManager() {
        instance = this;
    }

    public boolean addAdmin(UUID player) {
        if (admins.contains(player)) {
            return false;
        }
        admins.add(player);
        return true;
    }

    public boolean removeAdmin(UUID player) {
        if (!admins.contains(player)) {
            return false;
        }
        admins.remove(player);
        return true;
    }

    public boolean isAdmin(UUID player) {
        return admins.contains(player);
    }

    public List<UUID> getAdmins() {
        return admins;
    }

    public List<Player> getAdminPlayers() {
        List<Player> players = new ArrayList<Player>();
        for (UUID uuid : admins) {
            Player player = Bukkit.getServer().getPlayer(uuid);
            if (player != null) {
                players.add(player);
            }
        }
        return players;
    }

    public static UserManager get() {
        if (instance == null) {
            return new UserManager();
        }
        return instance;
    }
}
