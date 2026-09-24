package com.subpar77.trialmod.foundry.material;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.Optional;

public class FoundryMaterialForms {
    private FoundryMaterialForms() {}

    public static final int BLOCK_MB = 1000;
    public static final int SLAB_MB = 500;
    public static final int CLUMP_MB = 250;
    public static final int NUGGET_MB = 50;


/*    public static Optional<FoundrySolidForm> fromBlockState(
            BlockState state) {

        for (FoundryMaterial material : FoundryMaterial.values()) {

            if (state.is(material.getSolidifiedBlock())) {
                return Optional.of(new FoundrySolidForm(material, BLOCK_MB));
            }

            if (state.is(material.getSolidifiedSlab())) {

                SlabType slabType = state.getValue(SlabBlock.TYPE);

                int amountMb = slabType == SlabType.DOUBLE ? BLOCK_MB : SLAB_MB;

                return Optional.of(new FoundrySolidForm(material, amountMb));
            }
        }

        return Optional.empty();
    }*/

    public static Optional<FoundrySolidForm> getSolidForm(FoundryMaterial material, BlockState state) {
        if (state.is(material.getSolidifiedBlock())) {
            return Optional.of(new FoundrySolidForm(material, BLOCK_MB));
        }

        if (state.is(material.getSolidifiedSlab())) {
            SlabType slabType = state.getValue(SlabBlock.TYPE);

            int amountMb = slabType == SlabType.DOUBLE ? BLOCK_MB : SLAB_MB;

            return Optional.of(new FoundrySolidForm(material, amountMb));
        }
        return Optional.empty();
    }

    public static Optional<FoundrySolidForm> fromSolidifiedState(BlockState state) {

        for(FoundryMaterial material : FoundryMaterial.values()) {
            Optional<FoundrySolidForm> form = getSolidForm(material, state);

            if(form.isPresent()) {
                return form;
            }
        }
        return Optional.empty();
    }
}
