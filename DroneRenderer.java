package com.creeperpastel.client;

import com.creeperpastel.CreeperPastelMod;
import com.creeperpastel.entity.DroneEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class DroneRenderer extends MobEntityRenderer<DroneEntity, DroneModel> {
    private static final Identifier TEXTURE = CreeperPastelMod.id("textures/entity/dron.png");

    public DroneRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new DroneModel(), 0.3f);
    }

    @Override
    public Identifier getTexture(DroneEntity entity) {
        return TEXTURE;
    }
}
