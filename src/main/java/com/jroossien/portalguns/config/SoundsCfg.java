package com.jroossien.portalguns.config;

import com.jroossien.portalguns.PortalGuns;
import com.jroossien.portalguns.util.Parse;
import com.jroossien.portalguns.util.SoundEffect;
import org.bukkit.Sound;

import java.util.HashMap;
import java.util.Map;

public class SoundsCfg extends EasyConfig {

    public Map<String, String> sounds = new HashMap<>();
    private Map<String, SoundEffect> soundEffects = new HashMap<>();

    public SoundsCfg(String fileName) {
        this.setFile(fileName);
        load();
    }

    @Override
    public void load() {
        super.load();

        loadDefault("panel-open", Sound.HORSE_ARMOR, 1, 2);
        loadDefault("panel-close", Sound.HORSE_SADDLE, 1, 2);
        loadDefault("panel-fail", Sound.ITEM_BREAK, 0.5f, 0);
        loadDefault("panel-click", Sound.NOTE_STICKS, 0.5f, 2);
        loadDefault("portal-create", Sound.WITHER_HURT, 0.6f, 2);
        loadDefault("portal-destroy", Sound.ZOMBIE_REMEDY, 1, 2);
        loadDefault("portal-enter", Sound.ZOMBIE_INFECT, 1, 2);
        loadDefault("portal-leave", Sound.ZOMBIE_INFECT, 1, 1);
        loadDefault("portalgun-fail", Sound.FIZZ, 0.5f, 2);
        loadDefault("portalgun-break", Sound.ITEM_BREAK, 1, 1);

        for (Map.Entry<String, String> entry : sounds.entrySet()) {
            String[] split = entry.getValue().split(":");
            if (split.length < 3) {
                PortalGuns.inst().warn("Failed to load the sound '" + entry.getKey() + "'! Please check the sounds config and make sure it has the syntax 'sound:volume:pitch'.");
                continue;
            }

            Sound sound = null;
            String customSound = null;
            if (split[0].startsWith("!")) {
                customSound = split[0].substring(1);
            } else {
                try {
                    sound = Sound.valueOf(split[0]);
                } catch (Exception e) {}
                if (sound == null) {
                    PortalGuns.inst().warn("Failed to load the sound '" + entry.getKey() + "'! Failed to find the sound specified. [value=" + split[0] + "] (To use custom sound names prefix the name with a '!')");
                    continue;
                }
            }

            Float volume = Parse.Float(split[1]);
            if (volume == null) {
                PortalGuns.inst().warn("Failed to load the sound '" + entry.getKey() + "'! The volume value must be a decimal number. [value=" + split[1] + "]");
                continue;
            }

            Float pitch = Parse.Float(split[2]);
            if (pitch == null) {
                PortalGuns.inst().warn("Failed to load the sound '" + entry.getKey() + "'! The pitch value must be a decimal number. [value=" + split[2] + "]");
                continue;
            }

            if (sound == null) {
                soundEffects.put(entry.getKey(), new SoundEffect(customSound, volume, pitch));
            } else {
                soundEffects.put(entry.getKey(), new SoundEffect(sound, volume, pitch));
            }
        }

        save();
    }

    private void loadDefault(String name, Sound sound, float volume, float pitch) {
        if (!sounds.containsKey(name)) {
            sounds.put(name, sound.toString() + ":" + volume + ":" + pitch);
        }
    }

    public SoundEffect getSound(String name) {
        if (soundEffects.containsKey(name)) {
            return soundEffects.get(name);
        }
        return new SoundEffect((Sound)null);
    }
}
