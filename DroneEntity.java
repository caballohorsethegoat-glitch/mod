package com.creeperpastel.entity;

import com.creeperpastel.network.LockPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.FlyGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Dron: vuela, detecta jugadores, se acerca hasta una distancia moderada y
 * "agarra" al jugador (el cliente bloquea la camara hacia el dron) hasta que
 * el jugador llena la barra pulsando ESPACIO.
 */
public class DroneEntity extends HostileEntity {
    public static final double HOVER_DIST = 5.0;   // distancia a la que se queda flotando
    public static final double LOCK_DIST = 8.5;    // a esta distancia (con linea de vision) te agarra
    public static final int ESCAPE_COOLDOWN = 400; // ticks que huye despues de que te liberas (20 s)

    /** Lo rellena el cliente: inicia el sonido en bucle del dron. */
    public static Consumer<DroneEntity> clientTickHook = e -> {};
    public boolean soundStarted = false;

    private UUID grabbed = null;
    private int cooldown = 0;

    public DroneEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
        this.moveControl = new FlightMoveControl(this, 20, true);
        this.setPersistent();
    }

    public static DefaultAttributeContainer.Builder createDroneAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.6)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new BirdNavigation(this, world);
    }

    @Override
    protected void initGoals() {
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.goalSelector.add(1, new HoverGoal(this));
        this.goalSelector.add(5, new FlyGoal(this, 1.0));
    }

    @Override
    public boolean isDisallowedInPeaceful() {
        return false;
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, net.minecraft.entity.damage.DamageSource source) {
        return false;
    }

    public boolean isGrabbing(PlayerEntity p) {
        return grabbed != null && grabbed.equals(p.getUuid());
    }

    public int getCooldown() {
        return cooldown;
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient) {
            clientTickHook.accept(this);
            return;
        }
        if (cooldown > 0) cooldown--;

        LivingEntity target = getTarget();
        if (target != null && cooldown == 0) {
            // mira de frente al jugador (la camara del dron apunta a el)
            double dx = target.getX() - getX();
            double dz = target.getZ() - getZ();
            float yaw = (float) (-MathHelper.atan2(dx, dz) * 180.0 / Math.PI);
            setYaw(yaw);
            setBodyYaw(yaw);
            setHeadYaw(yaw);
        }

        if (grabbed != null) {
            PlayerEntity p = getWorld().getPlayerByUuid(grabbed);
            if (p == null || !p.isAlive() || p.isSpectator() || distanceTo(p) > LOCK_DIST + 6.0) {
                releasePlayer(false);
            } else if (age % 20 == 0 && p instanceof ServerPlayerEntity sp) {
                ServerPlayNetworking.send(sp, new LockPayload(getId(), true)); // refresco
            }
        } else if (cooldown == 0 && target instanceof ServerPlayerEntity sp && sp.isAlive()
                && distanceTo(sp) <= LOCK_DIST && canSee(sp)) {
            grabbed = sp.getUuid();
            ServerPlayNetworking.send(sp, new LockPayload(getId(), true));
        }
    }

    /** Suelta al jugador. Si escaped=true el dron huye durante un rato. */
    public void releasePlayer(boolean escaped) {
        if (grabbed != null) {
            PlayerEntity p = getWorld().getPlayerByUuid(grabbed);
            if (p instanceof ServerPlayerEntity sp) {
                ServerPlayNetworking.send(sp, new LockPayload(getId(), false));
            }
            grabbed = null;
        }
        if (escaped) cooldown = ESCAPE_COOLDOWN;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!getWorld().isClient) releasePlayer(false);
        super.remove(reason);
    }

    /** Se acerca al jugador hasta HOVER_DIST y flota; tras ser burlado, huye. */
    private static class HoverGoal extends Goal {
        private final DroneEntity drone;

        HoverGoal(DroneEntity drone) {
            this.drone = drone;
            setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            LivingEntity t = drone.getTarget();
            return t != null && t.isAlive();
        }

        @Override
        public boolean shouldContinue() {
            return canStart();
        }

        @Override
        public void tick() {
            LivingEntity t = drone.getTarget();
            if (t == null) return;
            if (drone.cooldown > 0) {
                Vec3d away = drone.getPos().subtract(t.getPos()).multiply(1, 0, 1);
                away = away.lengthSquared() < 0.01 ? new Vec3d(1, 0, 0) : away.normalize();
                Vec3d to = drone.getPos().add(away.multiply(8)).add(0, 2, 0);
                drone.getMoveControl().moveTo(to.x, to.y, to.z, 1.3);
                return;
            }
            Vec3d eye = t.getEyePos();
            Vec3d dir = drone.getPos().subtract(eye);
            dir = dir.lengthSquared() < 0.01 ? new Vec3d(1, 0, 0) : dir.normalize();
            Vec3d want = eye.add(dir.multiply(HOVER_DIST)).add(0, 0.3, 0);
            if (drone.squaredDistanceTo(want) > 0.4) {
                drone.getMoveControl().moveTo(want.x, want.y, want.z, 1.3);
            }
            drone.getLookControl().lookAt(t, 30.0f, 30.0f);
        }
    }
}
