package com.subpar77.trialmod.foundry;

import com.subpar77.trialmod.TrialMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoundryBasinSavedData extends SavedData {

    private static final String DATA_NAME = "trial_mod_foundry_basins";

    private final Map<Long, FoundryBasinState> basins = new HashMap<>();

    public FoundryBasinState getOrCreate(BlockPos basinKey) {
        long key = basinKey.asLong();

        FoundryBasinState state = basins.get(key);

        if (state == null) {
            state = new FoundryBasinState(70.0F);
            basins.put(key, state);
            setDirty();
        }

        return state;
    }

    public void setTemperature(BlockPos basinKey, float temperature) {
        FoundryBasinState state = getOrCreate(basinKey);
        state.setTemperature(temperature);

        setDirty();
    }

    public float getTemperature(BlockPos basinKey) {
        return getOrCreate(basinKey).getTemperature();
    }

    private static final SavedData.Factory<FoundryBasinSavedData> FACTORY = new SavedData.Factory<>(
            FoundryBasinSavedData::new, FoundryBasinSavedData::load);

    public static FoundryBasinSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public List<BlockPos> getBasinKeys() {
        return basins.keySet().stream().map(BlockPos::of).toList();
    }

    public void removeBasin(BlockPos basinKey) {
        if (basins.remove(basinKey.asLong()) != null) {
            setDirty();
            TrialMod.LOGGER.info("Removed basin key: " +basinKey);
        }


    }

    public static FoundryBasinSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        FoundryBasinSavedData data = new FoundryBasinSavedData();

        ListTag basinList = tag.getList("Basins", Tag.TAG_COMPOUND);

        for (Tag element : basinList) {
            CompoundTag basinTag = (CompoundTag) element;

            long key = basinTag.getLong("Key");
            float temperature = basinTag.getFloat("Temperature");

            data.basins.put(key, new FoundryBasinState(temperature));
        }

        return data;
    }


    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {

        ListTag basinList = new ListTag();

        for (Map.Entry<Long, FoundryBasinState> entry : basins.entrySet()) {
            CompoundTag basinTag = new CompoundTag();

            basinTag.putLong("Key", entry.getKey());
            basinTag.putFloat("Temperature", entry.getValue().getTemperature());

            basinList.add(basinTag);
        }

        tag.put("Basins", basinList);

        return tag;
    }
}
