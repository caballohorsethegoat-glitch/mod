package com.creeperpastel.network;

import com.creeperpastel.CreeperPastelMod;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Servidor -> cliente: "mancha la pantalla de pastel". intensity: 0..1 (segun la cercania). */
public record SplatPayload(float intensity) implements CustomPayload {
    public static final CustomPayload.Id<SplatPayload> ID =
            new CustomPayload.Id<>(CreeperPastelMod.id("splat"));
    public static final PacketCodec<net.minecraft.network.RegistryByteBuf, SplatPayload> CODEC =
            PacketCodecs.FLOAT.xmap(SplatPayload::new, SplatPayload::intensity).cast();

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
