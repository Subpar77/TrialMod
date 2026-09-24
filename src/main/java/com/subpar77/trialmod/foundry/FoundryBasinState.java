package com.subpar77.trialmod.foundry;

import com.subpar77.trialmod.foundry.material.FoundryMaterial;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class FoundryBasinState {

    private float temperature;

    @Nullable
    private FoundryMaterial material;

    private int amountMb;
    private int moltenAmountMb;
    private int brokenTicks;

    private final Set<BlockPos> interior = new HashSet<>();

    public FoundryBasinState(float temperature) {
        this(temperature, null, 0, 0, Set.of());
    }

    public FoundryBasinState(float temperature, @Nullable FoundryMaterial material, int amountMb, int moltenAmountMb) {
        this(temperature, material, amountMb, moltenAmountMb, Set.of());
    }

    public FoundryBasinState(float temperature, @Nullable FoundryMaterial material, int amountMb, int moltenAmountMb,
                             Set<BlockPos> interior) {
         this(temperature, material, amountMb, moltenAmountMb, interior, 0);
    }

    public FoundryBasinState(float temperature, @Nullable FoundryMaterial material, int amountMb, int moltenAmountMb,
                             Set<BlockPos> interior, int brokenTicks) {
         this.temperature = temperature;
         this.material = material;
         this.amountMb = amountMb;
         this.moltenAmountMb = moltenAmountMb;
         this.brokenTicks = Math.max(0, brokenTicks);
         setInterior(interior);

    }

    public Set<BlockPos> getInterior() {
         return Set.copyOf(interior);
    }

    public void setInterior(Set<BlockPos> interior) {
         this.interior.clear();

         for(BlockPos pos : interior) {
             this.interior.add(pos.immutable());
         }
    }

    public int getBrokenTicks() {
        return brokenTicks;
    }

    public void setBrokenTicks(int brokenTicks) {
        this.brokenTicks = Math.max(0, brokenTicks);
    }

    public float getTemperature() {
        return  temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public @Nullable FoundryMaterial getMaterial() {
        return material;
    }

    public void setMaterial(@Nullable FoundryMaterial material) {
        this.material = material;
    }

    public int getAmountMb() {
        return amountMb;
    }

    public void setAmountMb(int amountMb) {
        this.amountMb = amountMb;
    }

    public int getMoltenAmountMb() {
        return moltenAmountMb;
    }

    public void setMoltenAmountMb(int moltenAmountMb) {
        this.moltenAmountMb = moltenAmountMb;
    }

    public int getSolidAmountMb() {
        return amountMb - moltenAmountMb;
    }

}
