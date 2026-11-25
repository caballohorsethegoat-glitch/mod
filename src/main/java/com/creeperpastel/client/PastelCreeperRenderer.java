package com.creeperpastel.client;

import com.creeperpastel.CreeperPastelMod;
import com.creeperpastel.entity.PastelCreeperEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class PastelCreeperRenderer extends MobEntityRenderer<PastelCreeperEntity, PastelCreeperModel> {
    private static final Identifier TEXTURE = CreeperPastelMod.id("textures/entity/creeper_pastel.png");

    public PastelCreeperRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new PastelCreeperModel(), 0.5f);
    }

    @Override
    public Identifier getTexture(PastelCreeperEntity entity) {
        return TEXTURE;
    }

    // Efecto de "hincharse" al encender la mecha (igual que el creeper vanilla)
    @Override
    protected void scale(PastelCreeperEntity entity, MatrixStack matrices, float tickDelta) {
        float f = entity.getClientFuseTime(tickDelta);
        float g = 1.0f + MathHelper.sin(f * 100.0f) * f * 0.01f;
        f = MathHelper.clamp(f, 0.0f, 1.0f);
        f *= f;
        f *= f;
        float h = (1.0f + f * 0.4f) * g;
        float i = (1.0f + f * 0.1f) / g;
        matrices.scale(h, i, h);
    }

    // Parpadeo blanco al encender la mecha
    @Override
    protected float getAnimationCounter(PastelCreeperEntity entity, float tickDelta) {
        float f = entity.getClientFuseTime(tickDelta);
        return (int) (f * 10.0f) % 2 == 0 ? 0.0f : MathHelper.clamp(f, 0.5f, 1.0f);
    }
}
