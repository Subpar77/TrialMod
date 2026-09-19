package com.subpar77.trialmod.fluid;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.block.ModBlocks;
import com.subpar77.trialmod.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, TrialMod.MODID);

    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, TrialMod.MODID);

    public static final Supplier<FluidType> MOLTEN_COPPER_TYPE = FLUID_TYPES.register("molten_copper", () ->
            new FluidType(FluidType.Properties.create()
                    .lightLevel(12)
                    .density(3000)
                    .viscosity(6000)
                    .temperature(1300)
                    .motionScale(0.002D)
                    .canExtinguish(false)
                    .canDrown(false)
                    .canSwim(false)));

    private static BaseFlowingFluid.Properties moltenCopperProperties() {
        return new BaseFlowingFluid.Properties(MOLTEN_COPPER_TYPE, MOLTEN_COPPER_SOURCE, MOLTEN_COPPER_FLOWING)
                .bucket(ModItems.MOLTEN_COPPER_BUCKET)
                .block(ModBlocks.MOLTEN_COPPER_BLOCK)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2)
                .tickRate(30)
                .explosionResistance(100.0F);
    }

    public static final Supplier<FlowingFluid> MOLTEN_COPPER_SOURCE = FLUIDS.register("molten_copper", () ->
            new BaseFlowingFluid.Source(moltenCopperProperties()));

    public static final Supplier<FlowingFluid> MOLTEN_COPPER_FLOWING = FLUIDS.register("flowing_molten_copper", () ->
            new BaseFlowingFluid.Flowing(moltenCopperProperties()));

    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
    }
}
