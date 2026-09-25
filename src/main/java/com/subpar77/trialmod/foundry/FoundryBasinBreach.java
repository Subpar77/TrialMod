package com.subpar77.trialmod.foundry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.HashSet;
import java.util.Set;

public class FoundryBasinBreach {
    private FoundryBasinBreach() {}

    public record Result(Set<BlockPos> wallBreaches, Set<BlockPos> floorBreaches) {
        public Result {
            wallBreaches = Set.copyOf(wallBreaches);
            floorBreaches = Set.copyOf(floorBreaches);
        }

        public boolean isBreached() {
            return !wallBreaches().isEmpty() || !floorBreaches().isEmpty();
        }
    }

    public static Result inspect(ServerLevel level, Set<BlockPos> interior) {

        Set<BlockPos> wallBreaches = new HashSet<>();
        Set<BlockPos> floorBreaches = new HashSet<>();

        for(BlockPos interiorPos : interior) {
            BlockPos floorPos = interiorPos.below();

            if(!level.getBlockState(floorPos).is(ModBlockTags.VALID_FOUNDRY_BLOCKS)) {
                floorBreaches.add(floorPos.immutable());
            }



            for(Direction direction : new Direction[] {
                    Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {

                BlockPos neighborPos = interiorPos.relative(direction);

                if(interior.contains(neighborPos)) {
                    continue;
                }

                if(!level.getBlockState(neighborPos).is(ModBlockTags.VALID_FOUNDRY_BLOCKS)) {

                    wallBreaches.add(neighborPos.immutable());
                }
            }
        }

        return new Result(wallBreaches, floorBreaches);
    }

}
