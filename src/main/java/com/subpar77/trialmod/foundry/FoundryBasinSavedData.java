package com.subpar77.trialmod.foundry;

import com.subpar77.trialmod.TrialMod;
import com.subpar77.trialmod.foundry.material.FoundryMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FoundryBasinSavedData extends SavedData {

    private static final String DATA_NAME = "trial_mod_foundry_basins";

    private final Map<Long, FoundryBasinState> basins = new HashMap<>();

    public boolean registerBasin(BlockPos basinKey, Set<BlockPos> interior) {
        long key = basinKey.asLong();
        FoundryBasinState state = basins.get(key);

        if (state != null) {
            if(!state.getInterior().equals(interior)) {
                state.setInterior(interior);
                setDirty();
            }

            return false;
        }

        state = new FoundryBasinState(70.0F);
        state.setInterior(interior);

        basins.put(key, state);
        setDirty();

        TrialMod.LOGGER.info(
                "[Foundry] Registered basin at {} with {} interior cells.",
                basinKey, interior.size()
        );

        return true;
    }

    public int getBrokenTicks(BlockPos basinKey) {
        FoundryBasinState state = basins.get(basinKey.asLong());

        return state != null ? state.getBrokenTicks() : 0;
    }

    public void setBrokenTicks(BlockPos basinKey, int brokenTicks) {
        FoundryBasinState state = basins.get(basinKey.asLong());

        if(state == null) {
            return;
        }

        int newValue = Math.max(0, brokenTicks);

        if(state.getBrokenTicks() == newValue) {
            return;
        }

        state.setBrokenTicks(newValue);
        setDirty();
    }

    public Set<BlockPos> getInterior(BlockPos basinKey) {
        FoundryBasinState state = basins.get(basinKey.asLong());

        return state != null ? state.getInterior() : Set.of();
    }

    public boolean isRegistered(BlockPos basinKey) {
        return basins.containsKey(basinKey.asLong());
    }

    public void setTemperature(BlockPos basinKey, float temperature) {
        FoundryBasinState state = basins.get(basinKey.asLong());

        if(state == null) {
            return;
        }

        state.setTemperature(temperature);
        setDirty();
    }

    public float getTemperature(BlockPos basinKey) {
        FoundryBasinState state = basins.get(basinKey.asLong());

        return state != null ? state.getTemperature() : 70.0F;
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
            TrialMod.LOGGER.info("Removed basin key: {}",
                    basinKey);
        }
    }

    public @Nullable FoundryMaterial getMaterial(BlockPos basinKey) {
        FoundryBasinState state = basins.get(basinKey.asLong());

        return state != null ? state.getMaterial() : null;
    }

    public int getAmountMb(BlockPos basinKey) {
        FoundryBasinState state = basins.get(basinKey.asLong());

        return state != null ? state.getAmountMb() : 0;
    }

    public boolean tryAddMoltenMaterial(BlockPos basinKey, FoundryMaterial material, int amountMb, int capacityMb) {
        if (amountMb <= 0) {
            return false;
        }

        FoundryBasinState state = basins.get(basinKey.asLong());

        if(state == null) {
            return false;
        }

        FoundryMaterial currentMaterial = state.getMaterial();

        if (currentMaterial != null && currentMaterial != material) {
            TrialMod.LOGGER.debug(
                    "[Foundry] Basin {} rejected {} because it contains {}.",
                    basinKey, material.getSerializedName(), currentMaterial.getSerializedName()
            );
            return false;
        }

        int availableMb = capacityMb - state.getAmountMb();

        if (availableMb < amountMb) {
            TrialMod.LOGGER.debug(
                    "[Foundry] Basin {} rejected {} mB {}. Available capacity: {} mB.",
                    basinKey, amountMb, material.getSerializedName(), availableMb
            );
            return false;
        }

        if (currentMaterial == null) {
            state.setMaterial(material);
        }

        state.setAmountMb(state.getAmountMb() + amountMb);
        state.setMoltenAmountMb(state.getMoltenAmountMb() + amountMb);

        setDirty();

        return true;
    }

    public int solidifyMaterial(BlockPos basinKey, int amountMb) {
        if(amountMb <= 0) {
            return 0;
        }

        FoundryBasinState state = basins.get(basinKey.asLong());

        if(state == null) {
            return 0;
        }

        int convertedMb = Math.min(amountMb, state.getMoltenAmountMb());

        if(convertedMb <= 0) {
            return 0;
        }

        state.setMoltenAmountMb(state.getMoltenAmountMb() - convertedMb);

        setDirty();

        return convertedMb;
    }

    public int meltMaterial(BlockPos basinKey, int amountMb) {
        if(amountMb <= 0) {
            return 0;
        }

        FoundryBasinState state = basins.get(basinKey.asLong());

        if(state == null) {
            return 0;
        }

        int solidAmountMb = state.getSolidAmountMb();
        int convertedMb = Math.min(amountMb, solidAmountMb);

        if(convertedMb <= 0) {
            return 0;
        }

        state.setMoltenAmountMb(state.getMoltenAmountMb() + convertedMb);

        setDirty();

        return convertedMb;
    }

    public boolean tryRemoveMoltenMaterial(BlockPos basinKey, int amountMb) {
        if(amountMb <= 0) {
            return false;
        }

        FoundryBasinState state = basins.get(basinKey.asLong());

        if(state == null || state.getMoltenAmountMb() < amountMb || state.getMaterial() == null) {
            return false;
        }

        state.setAmountMb(state.getAmountMb() - amountMb);
        state.setMoltenAmountMb(state.getMoltenAmountMb() - amountMb);

        if(state.getAmountMb() == 0) {
            state.setMaterial(null);
            state.setMoltenAmountMb(0);
        }

        setDirty();

        return true;
    }

    public boolean tryRemoveMaterial(BlockPos basinKey, int amountMb) {

        if (amountMb <= 0) {
            return false;
        }

        FoundryBasinState state = basins.get(basinKey.asLong());

        if (state == null || state.getMaterial() == null || state.getAmountMb() < amountMb) {
            return false;
        }

        int moltenRemoved = Math.min(amountMb, state.getMoltenAmountMb());

        state.setMoltenAmountMb(state.getMoltenAmountMb() - moltenRemoved);

        state.setAmountMb(state.getAmountMb() - amountMb);

        if (state.getAmountMb() == 0) {
            state.setMaterial(null);
            state.setMoltenAmountMb(0);
        }

        setDirty();

        return true;
    }

    public int getMoltenAmountMb(BlockPos basinKey) {
        FoundryBasinState state = basins.get(basinKey.asLong());

        return state != null ? state.getMoltenAmountMb() : 0;
    }

    public int getSolidAmountMb(BlockPos basinKey) {
        FoundryBasinState state = basins.get(basinKey.asLong());

        return state != null ? state.getSolidAmountMb() : 0;
    }

    public static FoundryBasinSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        FoundryBasinSavedData data = new FoundryBasinSavedData();

        ListTag basinList = tag.getList("Basins", Tag.TAG_COMPOUND);

        for (Tag element : basinList) {
            CompoundTag basinTag = (CompoundTag) element;

            long key = basinTag.getLong("Key");
            float temperature = basinTag.getFloat("Temperature");
            int amountMb = basinTag.getInt("AmountMb");
            int moltenAmountMb;

            if(basinTag.contains("MoltenAmountMb", Tag.TAG_INT)) {
                moltenAmountMb = basinTag.getInt("MoltenAmountMb");
            } else {
                moltenAmountMb = amountMb;
            }

            FoundryMaterial material = null;

            if (basinTag.contains("Material", Tag.TAG_STRING)) {
                String materialName = basinTag.getString("Material");

                Optional<FoundryMaterial> loadedMaterial = FoundryMaterial.fromSerializedName(materialName);

                if(loadedMaterial.isPresent()) {
                    material = loadedMaterial.get();
                } else {
                    TrialMod.LOGGER.warn(
                            "[Foundry] Unknown saved material '{}' at basin {}. Clearing contents.",
                            materialName, BlockPos.of(key));
                }
            }

            if (material == null || amountMb <= 0) {
                material = null;
                amountMb = 0;
                moltenAmountMb = 0;
            }

            moltenAmountMb = Math.max(0, Math.min(moltenAmountMb, amountMb));

            Set<BlockPos> interior = new HashSet<>();
            for(long packedPos : basinTag.getLongArray("Interior")) {
                interior.add(BlockPos.of(packedPos));
            }

            int brokenTicks = basinTag.contains("BrokenTicks", Tag.TAG_INT) ?
                    basinTag.getInt("BrokenTicks") : 0;

            data.basins.put(key, new FoundryBasinState(temperature, material, amountMb, moltenAmountMb, interior,
                    brokenTicks));
        }

        return data;
    }

    public boolean tryRemoveSolidMaterial(BlockPos basinKey, int amountMb) {

        if(amountMb <= 0) {
            return false;
        }

        FoundryBasinState state = basins.get(basinKey.asLong());

        if(state == null || state.getSolidAmountMb() < amountMb || state.getMaterial() == null) {
            return false;
        }

        state.setAmountMb(state.getAmountMb() - amountMb);

        if(state.getAmountMb() == 0) {
            state.setMaterial(null);
            state.setMoltenAmountMb(0);
        }

        setDirty();

        return true;
    }


    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {

        ListTag basinList = new ListTag();

        for (Map.Entry<Long, FoundryBasinState> entry : basins.entrySet()) {
            CompoundTag basinTag = new CompoundTag();
            FoundryBasinState state = entry.getValue();
            long[] interiorPositions = state.getInterior().stream().mapToLong(pos -> pos.asLong())
                            .toArray();

            basinTag.putLong("Key", entry.getKey());
            basinTag.putFloat("Temperature", state.getTemperature());
            basinTag.putInt("AmountMb", state.getAmountMb());
            basinTag.putInt("MoltenAmountMb", state.getMoltenAmountMb());
            basinTag.putLongArray("Interior", interiorPositions);
            basinTag.putInt("BrokenTicks", state.getBrokenTicks());

            if (state.getMaterial() != null) {
                basinTag.putString("Material", state.getMaterial().getSerializedName());
            }

            basinList.add(basinTag);
        }

        tag.put("Basins", basinList);


        return tag;
    }
}
