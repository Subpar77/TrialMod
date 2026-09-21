package com.subpar77.trialmod.foundry;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.foundry.material.FoundryItemMetling;
import com.subpar77.trialmod.foundry.material.FoundryMelting;
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

                float currentTemperature =
                        savedData.getTemperature(basinKey);

                float newTemperature =
                        FoundryThermal.calculateCooling(currentTemperature);

                if (FoundryThermal.isAtAmbient(newTemperature)) {
                    savedData.removeBasin(basinKey);
                } else {
                    savedData.setTemperature(basinKey, newTemperature);
                }

                continue;
            }

            Optional<HeatSourceData> heat = FoundryHeat.inspect(level, interior.get());
            float currentTemperature = savedData.getTemperature(basinKey);
            float newTemperature = FoundryThermal.calculateNewTemperature(currentTemperature, heat, FoundryTier.STONE);

            savedData.setTemperature(basinKey, newTemperature);
            FoundryItemMetling.process(level, basinKey, interior.get(), newTemperature);

            FoundrySolidification.solidify(level, interior.get(), newTemperature);
            FoundryMelting.melt(level, interior.get(), newTemperature);
        }
    }
}
