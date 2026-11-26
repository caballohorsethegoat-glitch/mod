package com.creeperpastel.network;

import com.creeperpastel.CreeperPastelMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/** Cliente -> servidor: el jugador lleno la barra y se libero del dron. */
public record EscapePayload() implements CustomPayload {
    public static final CustomPayload.Id<EscapePayload> ID = new CustomPayload.Id<>(CreeperPastelMod.id("escape"));
    public static final PacketCodec<RegistryByteBuf, EscapePayload> CODEC = PacketCodec.unit(new EscapePayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
