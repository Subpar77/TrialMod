package com.subpar77.trialmod.foundry;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.foundry.material.FoundryMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

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

    public void setContents(BlockPos basinKey, @Nullable FoundryMaterial material, int amoundMb) {
        if (amoundMb <0) {
            throw new IllegalArgumentException("Foundry basin amount cannot be negative");
        }

        FoundryBasinState state = getOrCreate(basinKey);
        if(amoundMb == 0) {
            state.setMaterial(null);
            state.setAmountMb(0);
        } else {
            if (material == null) {
                throw new IllegalArgumentException("Foundry basin with material amount must have a material");
            }

            state.setMaterial(material);
            state.setAmountMb(amoundMb);
        }
        setDirty();
    }

    public @Nullable FoundryMaterial getMaterial(BlockPos basinKey) {
        return getOrCreate(basinKey).getMaterial();
    }

    public int getAmountMb(BlockPos basinKey) {
        return getOrCreate(basinKey).getAmountMb();
    }

    public int addMaterial(BlockPos basinKey, FoundryMaterial material, int amoubtMb, int capacityMb) {
        if(amoubtMb <= 0) {
            return 0;
        }

        FoundryBasinState state = getOrCreate(basinKey);
        FoundryMaterial currentMaterial = state.getMaterial();

        if (currentMaterial != null && currentMaterial != material) {
            return  0;
        }

        int availableMb = Math.max(0, capacityMb - state.getAmountMb());
        int acceptedMb = Math.min(amoubtMb, availableMb);

        if (acceptedMb <= 0) {
            return 0;
        }

        if (currentMaterial == null) {
            state.setMaterial(material);
        }

        state.setAmountMb(state.getAmountMb() + acceptedMb);

        setDirty();

        return  acceptedMb;
    }

    public static FoundryBasinSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        FoundryBasinSavedData data = new FoundryBasinSavedData();

        ListTag basinList = tag.getList("Basins", Tag.TAG_COMPOUND);

        for (Tag element : basinList) {
            CompoundTag basinTag = (CompoundTag) element;

            long key = basinTag.getLong("Key");
            float temperature = basinTag.getFloat("Temperature");
            int amountMb = basinTag.getInt("AmountMb");
            FoundryMaterial material = null;

            if (basinTag.contains("Material", Tag.TAG_STRING)) {
                String materialName = basinTag.getString("Material");

                material = FoundryMaterial.fromSerializedName(materialName).orElse(null);
            }

            if (material == null || amountMb <= 0) {
                material = null;
                amountMb = 0;
            }

            data.basins.put(key, new FoundryBasinState(temperature, material, amountMb));
        }

        return data;
    }


    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {

        ListTag basinList = new ListTag();

        for (Map.Entry<Long, FoundryBasinState> entry : basins.entrySet()) {
            CompoundTag basinTag = new CompoundTag();
            FoundryBasinState state = entry.getValue();

            basinTag.putLong("Key", entry.getKey());
            basinTag.putFloat("Temperature", state.getTemperature());
            basinTag.putInt("AmountMb", state.getAmountMb());

            if (state.getMaterial() != null) {
                basinTag.putString("Material", state.getMaterial().getSerializedName());
            }

            basinList.add(basinTag);
        }

        tag.put("Basins", basinList);

        return tag;
    }
}
