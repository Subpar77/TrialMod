package com.subpar77.trialmod.foundry.material;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.foundry.FoundryBasin;
import com.subpar77.trialmod.foundry.FoundryBasinSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class FoundryPhaseTransitions {
    private FoundryPhaseTransitions() {}

    public static void update(ServerLevel level, BlockPos basinKey, float temperature) {
        FoundryBasinSavedData savedData = FoundryBasinSavedData.get(level);
        FoundryMaterial material = savedData.getMaterial(basinKey);

        if(material == null) {
            return;
        }

        if(temperature >= material.getMeltingTemperature()) {
            int meltedMb = savedData.meltMaterial(basinKey, FoundryBasin.MB_PER_BUCKET);

            if (meltedMb > 0) {
                TrialMod.LOGGER.info(
                        "[Foundry] Melted {} mB {} at basin {}. Molten: {} / {} mB.",
                        meltedMb, material.getSerializedName(), basinKey, savedData.getMoltenAmountMb(basinKey),
                        savedData.getAmountMb(basinKey));
            }
            return;
        }

        if(temperature <= material.getSolidificationTemperature()) {
            int solidifiedMb = savedData.solidifyMaterial(basinKey, FoundryBasin.MB_PER_BUCKET);

            if(solidifiedMb > 0) {
                TrialMod.LOGGER.info(
                "[Foundry] Solidified {} mB {} at basin {}. Molten: {} / {} mB.",
                solidifiedMb, material.getSerializedName(), basinKey, savedData.getMoltenAmountMb(basinKey),
                savedData.getAmountMb(basinKey));
            }
        }
    }
}
