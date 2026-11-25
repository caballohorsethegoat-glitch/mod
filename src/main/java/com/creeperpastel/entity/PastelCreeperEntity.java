package com.creeperpastel.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.world.World;

/**
 * Creeper Pastel: se comporta exactamente como un creeper (mecha, explosion,
 * sonidos y IA). Solo cambian modelo, textura y hitbox.
 */
public class PastelCreeperEntity extends CreeperEntity {
    public PastelCreeperEntity(EntityType<? extends CreeperEntity> type, World world) {
        super(type, world);
    }
}
