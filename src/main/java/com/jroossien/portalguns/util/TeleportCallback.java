package com.jroossien.portalguns.util;

import org.bukkit.entity.Entity;

import java.util.List;

public abstract class TeleportCallback {
    public abstract void teleported(List<Entity> entities);
}
