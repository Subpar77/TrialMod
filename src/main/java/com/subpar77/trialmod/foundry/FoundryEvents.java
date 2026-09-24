package com.subpar77.trialmod.foundry;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.foundry.material.FoundryItemMelting;
import com.subpar77.trialmod.foundry.material.FoundryPhaseTransitions;
import com.subpar77.trialmod.foundry.material.FoundryPhysicalSolidMelting;
import com.subpar77.trialmod.foundry.material.FoundrySolidification;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.Optional;
import java.util.Set;

@EventBusSubscriber(modid = TrialMod.MODID)
public final class FoundryEvents {

    private FoundryEvents() {}

    public static final int BASIN_FAILURE_TICKS = 20 * 30;

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        if (level.getGameTime() % 20 != 0) {
            return;
        }
        updateFoundries(level);
    }


    private static void updateFoundries(ServerLevel level) {
        FoundryBasinSavedData savedData = FoundryBasinSavedData.get(level);

        for(BlockPos basinKey : savedData.getBasinKeys()) {
            Optional<Set<BlockPos>> interior = FoundryStructure.findBasin(level, basinKey);

            if (interior.isEmpty()) {

                Set<BlockPos> rememberedInterior = savedData.getInterior(basinKey);
                FoundryBasinBreach.Result breach = FoundryBasinBreach.inspect(level, rememberedInterior);

                if(breach.isBreached()) {
                    int brokenTicks = savedData.getBrokenTicks(basinKey) + 20;
                    savedData.setBrokenTicks(basinKey, brokenTicks);

                    FoundryBasinSpill.attemptSpill(level, basinKey, rememberedInterior, breach);

                    if(savedData.getAmountMb(basinKey) == 0) {
                        TrialMod.LOGGER.info(
                                "[Foundry] Deregistering empty breached basin {}.",
                                basinKey
                        );

                        savedData.removeBasin(basinKey);
                        continue;
                    }

                    if(savedData.getBrokenTicks(basinKey) >= BASIN_FAILURE_TICKS) {

                        boolean finalized = FoundryBasinFinalization.finalizeBasin(level, basinKey, rememberedInterior);

                            if(finalized) {
                                TrialMod.LOGGER.info(
                                        "[Foundry] Deregistering finalized basin {}.",
                                        basinKey
                                );

                                savedData.removeBasin(basinKey);
                                continue;
                            }
                    }

//                    TrialMod.LOGGER.debug(
//                            "[Foundry] Basin {} breached for {} ticks ({} seconds). Walls={}, Floors={}",
//                            basinKey, brokenTicks, brokenTicks / 20.0F, breach.wallBreaches(), breach.floorBreaches()
//                    );
                } else {
                    savedData.setBrokenTicks(basinKey, 0);
                }

                float currentTemperature =
                        savedData.getTemperature(basinKey);

                float newTemperature =
                        FoundryThermal.calculateCooling(currentTemperature);

                savedData.setTemperature(basinKey, newTemperature);

                continue;
            }

            if(savedData.getBrokenTicks(basinKey) > 0) {
                TrialMod.LOGGER.info(
                        "[Foundry] Basin {} repaired after {} ticks.",
                        basinKey, savedData.getBrokenTicks(basinKey)
                );

                savedData.setBrokenTicks(basinKey, 0);
            }

            Optional<HeatSourceData> heat = FoundryHeat.inspect(level, interior.get());
            float currentTemperature = savedData.getTemperature(basinKey);
            float newTemperature = FoundryThermal.calculateNewTemperature(currentTemperature, heat, FoundryTier.STONE);
            savedData.setTemperature(basinKey, newTemperature);

            FoundryItemMelting.process(level, basinKey, interior.get(), newTemperature);
            FoundryPhysicalSolidMelting.process(level, basinKey, interior.get(), newTemperature);
            FoundryPhaseTransitions.update(level, basinKey, newTemperature);
            FoundrySolidification.process(level, basinKey, interior.get());
            FoundryBasinVisuals.update(level, basinKey, interior.get());
        }
    }
}
