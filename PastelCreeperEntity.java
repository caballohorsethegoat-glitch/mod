package com.creeperpastel.entity;

import com.creeperpastel.network.SplatPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

/**
 * Creeper Pastel: se comporta exactamente como un creeper (mecha, explosion,
 * sonidos y IA). Al explotar, mancha de pastel la pantalla de los jugadores cercanos.
 */
public class PastelCreeperEntity extends CreeperEntity {
    private static final double SPLAT_RANGE = 24.0;

    public PastelCreeperEntity(EntityType<? extends CreeperEntity> type, World world) {
        super(type, world);
    }

    @Override
    public void remove(RemovalReason reason) {
        // El creeper vanilla se hace discard() justo despues de explotar.
        if (!getWorld().isClient && reason == RemovalReason.DISCARDED
                && (isIgnited() || getFuseSpeed() > 0)
                && getWorld() instanceof ServerWorld sw) {
            for (ServerPlayerEntity p : sw.getPlayers()) {
                double d = p.distanceTo(this);
                if (d <= SPLAT_RANGE) {
                    float intensity = 1.0f; // siempre manchado completo
                    ServerPlayNetworking.send(p, new SplatPayload(intensity));
                }
            }
        }
        super.remove(reason);
    }
}
