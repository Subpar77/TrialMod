package com.subpar77.trialmod.foundry.material;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.foundry.FoundryBasin;
import com.subpar77.trialmod.foundry.FoundryBasinSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.swing.text.html.HTMLDocument;
import java.util.Optional;
import java.util.Set;

public class FoundryPhysicalSolidMelting {
    private FoundryPhysicalSolidMelting() {}

    public static void process(ServerLevel level, BlockPos basinKey, Set<BlockPos> interior, float temperature) {

        FoundryBasinSavedData savedData = FoundryBasinSavedData.get(level);

        for(BlockPos pos : interior) {
            BlockState state = level.getBlockState(pos);
            Optional<FoundrySolidForm> formResult = FoundryMaterial.fromSolidifiedState(state);

            if(formResult.isEmpty()) {
                continue;
            }

            FoundrySolidForm form = formResult.get();
            FoundryMaterial material = form.material();

            if(temperature < material.getMeltingTemperature()) {
                continue;
            }

            boolean removed = level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

            if(!removed) {
                continue;
            }

            int stateCapacityMb = FoundryBasin.getStateCapacityMb(level, interior);
            boolean accepted = savedData.tryAddMoltenMaterial(basinKey, material, form.amountMb(), stateCapacityMb);

            if(!accepted) {
                level.setBlock(pos, state, Block.UPDATE_ALL);
                TrialMod.LOGGER.warn(
                        "[Foundry] Could not melt physical {} at {} in basin {}; restored block.",
                        material.getSerializedName(), pos, basinKey
                );

                continue;
            }

            TrialMod.LOGGER.info(
                    "[Foundry] Melted physical {} mB {} at {} into basin {}. "
                    + "Stored={} mB, molten={} mB.",
                    form.amountMb(), material.getSerializedName(), pos, basinKey, savedData.getAmountMb(basinKey),
                    savedData.getMoltenAmountMb(basinKey)
            );

            return;
        }
    }
}
