package com.subpar77.trialmod.foundry;

import com.subpar77.trialmod.TrialMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModBlockTags {
    public static final TagKey<Block> VALID_BASIN_WALLS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(TrialMod.MODID, "valid_basin_walls"));
    public static final TagKey<Block> VALID_BASIN_FLOORS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(TrialMod.MODID, "valid_basin_floors"));

    private ModBlockTags() {}
}
