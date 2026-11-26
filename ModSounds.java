package com.creeperpastel;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    public static final SoundEvent DRONE_LOOP = register("drone_loop");

    private static SoundEvent register(String name) {
        Identifier id = CreeperPastelMod.id(name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void init() {
        // fuerza la carga de la clase (registro de sonidos)
    }
}
