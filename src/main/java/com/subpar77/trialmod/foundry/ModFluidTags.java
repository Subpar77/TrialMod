package com.subpar77.trialmod.foundry;

import com.subpar77.trialmod.TrialMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class ModFluidTags {
    public static final TagKey<Fluid> VALID_BASIN_FLUIDS = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(TrialMod.MODID, "valid_basin_fluids"));

    private ModFluidTags() {}
}
