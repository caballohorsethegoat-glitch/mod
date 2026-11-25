package com.creeperpastel.client;

import com.creeperpastel.CreeperPastelMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class CreeperPastelClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(CreeperPastelMod.CREEPER_PASTEL, PastelCreeperRenderer::new);
    }
}
