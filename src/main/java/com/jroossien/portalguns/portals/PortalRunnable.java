package com.jroossien.portalguns.portals;

import com.jroossien.portalguns.PortalGuns;
import com.jroossien.portalguns.UserManager;
import com.jroossien.portalguns.guns.GunData;
import com.jroossien.portalguns.guns.GunType;
import com.jroossien.portalguns.util.particles.ParticleEffect;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class PortalRunnable extends BukkitRunnable {

    private PortalGuns pg;
    private PortalManager pm;

    private int step = 0;
    private final int particles = 20;
    private final double width = 0.45f;
    private final double length = 0.9f;

    public PortalRunnable(PortalManager pm) {
        this.pm = pm;
        this.pg = PortalGuns.inst();
    }

    @Override
    public void run() {
        for (PortalData portal : pm.getPortals().values()) {
            if (!portal.isValid() || !portal.isEnabled()) {
                continue;
            }
            Location loc = portal.getCenter();
            if (loc.getWorld() == null ||loc.getChunk() == null || !loc.getChunk().isLoaded()) {
                continue;
            }
            GunData gun = pg.getGM().getGun(portal.getGun());
            if (gun == null) {
                continue;
            }
            Color color = gun.getColor(portal.getType());
            if (color == null) {
                color = pg.getCfg().getColor(portal.getType());
            }

            for (int i = 0; i <= 1; i++) {
                double inc = (2 * Math.PI) / particles;
                double angle = (step + (i == 1 ? particles/2 : 0)) * inc;

                double widthAxis = Math.cos(angle) * width;
                double lengthAxis = Math.sin(angle) * length;

                Vector v = rotateVector(widthAxis, lengthAxis, portal.getDirection(), portal.getSecondaryDirection());

                if (gun.getType() == GunType.GLOBAL || pg.getCfg().portal__alwaysVisible) {
                    ParticleEffect.REDSTONE.display(new ParticleEffect.OrdinaryColor(color.getRed(), color.getGreen(), color.getBlue()), loc.add(v), 32);
                } else {
                    List<Player> players = gun.getPlayers();
                    List<Player> admins = UserManager.get().getAdminPlayers();
                    for (Player admin : admins) {
                        if (!players.contains(admin)) {
                            players.add(admin);
                        }
                    }
                    ParticleEffect.REDSTONE.display(new ParticleEffect.OrdinaryColor(color.getRed(), color.getGreen(), color.getBlue()), loc.add(v), players);
                }
                loc.subtract(v);
            }
        }
        step++;
        if (step >= particles) {
            step = 0;
        }
    }

    private Vector rotateVector(double widthAxis, double lengthAxis, BlockFace dir, BlockFace secondaryDir) {
        if (dir == BlockFace.UP || dir == BlockFace.DOWN) {
            if (secondaryDir == BlockFace.NORTH || secondaryDir == BlockFace.SOUTH) {
                return new Vector(lengthAxis, 0, widthAxis);
            } else {
                return new Vector(widthAxis, 0, lengthAxis);
            }
        } else if (dir == BlockFace.NORTH || dir == BlockFace.SOUTH) {
            return new Vector(widthAxis, lengthAxis, 0);
        } else if (dir == BlockFace.EAST || dir == BlockFace.WEST) {
            return new Vector(0, lengthAxis, widthAxis);
        }
        return new Vector(0, 0, 0);
    }
}
