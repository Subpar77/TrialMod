package com.subpar77.trialmod.foundry.material;

import com.subpar77.trialmod.block.ModBlocks;
import com.subpar77.trialmod.fluid.ModFluids;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.Optional;
import java.util.function.Supplier;

public enum FoundryMaterial {
    COPPER(2000, 1900,
            () -> ModFluids.MOLTEN_COPPER_SOURCE.get(),
            () -> ModBlocks.COPPER_SLAG_BLOCK.get());

    private final float meltingTemperature;
    private final float solidificationTemperature;
    private final Supplier<? extends Fluid> moltenFluid;
    private final Supplier<? extends Block> solidifiedBlock;

    FoundryMaterial(float meltingTemperature, float solidificationTemperature, Supplier<? extends Fluid> moltenFluid, Supplier<? extends Block> solidifiedBlock) {
        this.meltingTemperature = meltingTemperature;
        this.solidificationTemperature = solidificationTemperature;
        this.moltenFluid = moltenFluid;
        this.solidifiedBlock = solidifiedBlock;
    }

    public float getMeltingTemperature() {
        return meltingTemperature;
    }

    public float getSolidificationTemperature() {
        return solidificationTemperature;
    }

    public Fluid getMoltenFluid() {
        return moltenFluid.get();
    }

    public Block getSolidifiedBlock() {
        return solidifiedBlock.get();
    }

    public static Optional<FoundryMaterial> fromFluid(Fluid fluid) {
        for (FoundryMaterial material : values()) {

            if (material.getMoltenFluid() == fluid) {
                return Optional.of(material);
            }
        }

        return Optional.empty();
    }

    public static Optional<FoundryMaterial> fromSolidifiedBlock(Block block) {
        for (FoundryMaterial material : values()) {
            if (material.getSolidifiedBlock() == block) {
                return Optional.of(material);
            }
        }

        return Optional.empty();
    }
}
