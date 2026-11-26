package com.creeperpastel.mixin.client;

import com.creeperpastel.client.DroneClientState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Mientras el dron te agarra, el mouse no mueve la camara. */
@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void creeperpastel$lockLook(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if (DroneClientState.isLocked() && (Object) this == MinecraftClient.getInstance().player) {
            ci.cancel();
        }
    }
}
