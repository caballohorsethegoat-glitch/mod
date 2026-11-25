package com.creeperpastel;

import com.creeperpastel.entity.PastelCreeperEntity;
import com.creeperpastel.network.SplatPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;

public class CreeperPastelMod implements ModInitializer {
    public static final String MOD_ID = "creeperpastel";

    public static final EntityType<PastelCreeperEntity> CREEPER_PASTEL = Registry.register(
            Registries.ENTITY_TYPE,
            id("creeper_pastel"),
            EntityType.Builder.create(PastelCreeperEntity::new, SpawnGroup.MONSTER)
                    // Hitbox del modelo: 12 x 18 px -> 0.75 x 1.125 bloques
                    .dimensions(0.75f, 1.125f)
                    .maxTrackingRange(8)
                    .build("creeper_pastel"));

    public static final Item CREEPER_PASTEL_SPAWN_EGG = Registry.register(
            Registries.ITEM,
            id("creeper_pastel_spawn_egg"),
            new SpawnEggItem(CREEPER_PASTEL, 0xFFFFFF, 0xFFFFFF, new Item.Settings()));

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(SplatPayload.ID, SplatPayload.CODEC);

        FabricDefaultAttributeRegistry.register(CREEPER_PASTEL, PastelCreeperEntity.createCreeperAttributes());

        SpawnRestriction.register(CREEPER_PASTEL, SpawnLocationTypes.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark);

        // Aparece en el Overworld como un mob hostil mas (peso, grupo min, grupo max)
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, CREEPER_PASTEL, 20, 1, 2);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(e -> e.add(CREEPER_PASTEL_SPAWN_EGG));
    }
}
