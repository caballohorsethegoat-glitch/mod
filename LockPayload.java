package com.creeperpastel.network;

import com.creeperpastel.CreeperPastelMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Servidor -> cliente: el dron te agarra (active=true) o te suelta (active=false). */
public record LockPayload(int droneId, boolean active) implements CustomPayload {
    public static final CustomPayload.Id<LockPayload> ID = new CustomPayload.Id<>(CreeperPastelMod.id("lock"));
    public static final PacketCodec<RegistryByteBuf, LockPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, LockPayload::droneId,
            PacketCodecs.BOOL, LockPayload::active,
            LockPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
