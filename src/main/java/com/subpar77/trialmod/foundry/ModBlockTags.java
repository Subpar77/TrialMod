package com.subpar77.trialmod.foundry;

import com.subpar77.trialmod.TrialMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;

public class ModBlockTags {
    public static final TagKey<Block> VALID_FOUNDRY_BLOCKS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(TrialMod.MODID, "foundry_bricks"));

    private ModBlockTags() {}
}
