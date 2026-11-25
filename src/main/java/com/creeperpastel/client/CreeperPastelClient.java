package com.creeperpastel.client;

import com.creeperpastel.CreeperPastelMod;
import com.creeperpastel.network.SplatPayload;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CreeperPastelClient implements ClientModInitializer {
    private static final Identifier[] SPLATS = {
            CreeperPastelMod.id("textures/gui/splat1.png"),
            CreeperPastelMod.id("textures/gui/splat2.png")
    };
    private static final long DURATION_MS = 6000;   // duracion total de la mancha
    private static final long HOLD_MS = 1500;       // tiempo a opacidad completa antes de desvanecerse

    private record Splat(int tex, float cx, float cy, float size, float rot) {}

    private static final List<Splat> splats = new ArrayList<>();
    private static long startMs = 0;
    private static float intensity = 0;
    private static final Random RNG = new Random();

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(CreeperPastelMod.CREEPER_PASTEL, PastelCreeperRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(SplatPayload.ID, (payload, context) ->
                context.client().execute(() -> startSplat(payload.intensity())));

        HudRenderCallback.EVENT.register(CreeperPastelClient::renderSplat);
    }

    private static void startSplat(float inten) {
        splats.clear();
        intensity = inten;
        startMs = System.currentTimeMillis();
        int n = 4 + RNG.nextInt(3);
        for (int i = 0; i < n; i++) {
            splats.add(new Splat(RNG.nextInt(SPLATS.length),
                    RNG.nextFloat(), RNG.nextFloat(),
                    0.7f + RNG.nextFloat() * 0.8f,        // tamano relativo al alto de pantalla
                    RNG.nextFloat() * 360.0f));
        }
    }

    private static void renderSplat(DrawContext ctx, net.minecraft.client.render.RenderTickCounter tickCounter) {
        if (splats.isEmpty()) return;
        long elapsed = System.currentTimeMillis() - startMs;
        if (elapsed >= DURATION_MS) {
            splats.clear();
            return;
        }
        float alpha = elapsed <= HOLD_MS ? 1.0f : 1.0f - (elapsed - HOLD_MS) / (float) (DURATION_MS - HOLD_MS);
        alpha = Math.max(0f, Math.min(1f, alpha)) * intensity;

        MinecraftClient mc = MinecraftClient.getInstance();
        int w = mc.getWindow().getScaledWidth();
        int h = mc.getWindow().getScaledHeight();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        for (Splat s : splats) {
            int size = (int) (h * s.size());
            ctx.getMatrices().push();
            ctx.getMatrices().translate(s.cx() * w, s.cy() * h, 0);
            ctx.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(s.rot()));
            ctx.drawTexture(SPLATS[s.tex()], -size / 2, -size / 2, size, size, 0, 0, 256, 256, 256, 256);
            ctx.getMatrices().pop();
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }
}
