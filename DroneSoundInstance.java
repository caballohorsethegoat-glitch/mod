package com.creeperpastel.client;

import com.creeperpastel.ModSounds;
import com.creeperpastel.entity.DroneEntity;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;

/** Zumbido en bucle que sigue al dron mientras exista. */
public class DroneSoundInstance extends MovingSoundInstance {
    private final DroneEntity drone;

    public DroneSoundInstance(DroneEntity drone) {
        super(ModSounds.DRONE_LOOP, SoundCategory.HOSTILE, SoundInstance.createRandom());
        this.drone = drone;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 1.2f;
        this.pitch = 1.0f;
        this.x = drone.getX();
        this.y = drone.getY();
        this.z = drone.getZ();
    }

    @Override
    public void tick() {
        if (drone.isRemoved() || !drone.isAlive()) {
            setDone();
            return;
        }
        this.x = drone.getX();
        this.y = drone.getY();
        this.z = drone.getZ();
    }
}
