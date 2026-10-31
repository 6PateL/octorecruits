package com.octorecruits;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(OctoRecruits.MODID)
public class OctoRecruits {
    public static final String MODID = "octorecruits";
    public static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    public OctoRecruits() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, OctoConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(new OctoEvents());
    }
}
