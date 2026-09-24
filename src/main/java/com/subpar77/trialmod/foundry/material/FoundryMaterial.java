package com.subpar77.trialmod.foundry.material;

import com.mojang.serialization.Codec;
import com.subpar77.trialmod.block.ModBlocks;
import com.subpar77.trialmod.fluid.ModFluids;
import com.subpar77.trialmod.item.ModItems;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluid;

import java.util.Optional;
import java.util.function.Supplier;

public enum FoundryMaterial implements StringRepresentable {
    COPPER("copper",2000, 1900,
            () -> ModFluids.MOLTEN_COPPER_SOURCE.get(),
            () -> ModBlocks.COPPER_SLAG_BLOCK.get(),
            () -> ModBlocks.COPPER_SLAG_SLAB.get(),
            () -> ModItems.COPPER_SLAG_CLUMP.get(),
            () -> ModItems.COPPER_SLAG_NUGGET.get(),
            () -> ModBlocks.MOLTEN_COPPER_DISPLAY.get());

    public static final Codec<FoundryMaterial> CODEC = StringRepresentable.fromEnum(FoundryMaterial::values);

    private final String serializedName;
    private final float meltingTemperature;
    private final float solidificationTemperature;
    private final Supplier<? extends Fluid> moltenFluid;
    private final Supplier<? extends Block> solidifiedBlock;
    private final Supplier<? extends Block> solidifiedSlab;
    private final Supplier<? extends Item> solidifiedClump;
    private final Supplier<? extends Item> solidifiedNugget;
    private final Supplier<? extends Block> moltenDisplayBlock;

    FoundryMaterial(String serializedName, float meltingTemperature, float solidificationTemperature,
                    Supplier<? extends Fluid> moltenFluid, Supplier<? extends Block> solidifiedBlock,
                    Supplier<? extends Block> solidifiedSlab, Supplier<? extends Item> solidifiedClump,
                    Supplier<? extends Item> solidifiedNugget, Supplier<? extends Block> moltenDisplayBlock) {

        this.serializedName = serializedName;
        this.meltingTemperature = meltingTemperature;
        this.solidificationTemperature = solidificationTemperature;
        this.moltenFluid = moltenFluid;
        this.solidifiedBlock = solidifiedBlock;
        this.solidifiedSlab = solidifiedSlab;
        this.solidifiedClump = solidifiedClump;
        this.solidifiedNugget = solidifiedNugget;
        this.moltenDisplayBlock = moltenDisplayBlock;
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

    public Block getSolidifiedSlab() { return solidifiedSlab.get();
    }

    public Item getSolidifiedClump() { return solidifiedClump.get();
    }

    public Item getSolidifiedNugget() { return solidifiedNugget.get();
    }



    public Block getMoltenDisplayBlock() {
        return moltenDisplayBlock.get();
    }

    public static Optional<FoundryMaterial> fromFluid(Fluid fluid) {
        for (FoundryMaterial material : values()) {

            if (material.getMoltenFluid().isSame(fluid)) {
                return Optional.of(material);
            }
        }

        return Optional.empty();
    }

    public static Optional<FoundryMaterial> fromSerializedName(String name) {
        for (FoundryMaterial material : values()) {
            if (material.getSerializedName().equals(name)) {
                return Optional.of(material);
            }
        }
        return Optional.empty();
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
