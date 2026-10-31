package com.octorecruits.client;

import com.octorecruits.OctoRecruits;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = OctoRecruits.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class OctoClientEvents {
    private OctoClientEvents() {}

    /** Adds the tentacle layer to the renderers of every Recruits entity type. */
    @SubscribeEvent
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        if (!ModList.get().isLoaded("tentaworks")) {
            return;
        }
        for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES) {
            var key = ForgeRegistries.ENTITY_TYPES.getKey(type);
            if (key == null || !"recruits".equals(key.getNamespace())) {
                continue;
            }
            Object renderer = event.getRenderer((EntityType) type);
            if (renderer instanceof LivingEntityRenderer living) {
                living.addLayer(new OctoTentacleLayer(living));
            }
        }
    }
}
