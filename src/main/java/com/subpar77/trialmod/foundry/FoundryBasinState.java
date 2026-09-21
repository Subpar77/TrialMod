package com.subpar77.trialmod.foundry;

import com.subpar77.trialmod.foundry.material.FoundryMaterial;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public class FoundryBasinState {

    private float temperature;

    @Nullable
    private FoundryMaterial material;

    private int amountMb;

    public FoundryBasinState(float temperature) {
        this(temperature, null, 0);
    }

    public FoundryBasinState(float temperature, @Nullable FoundryMaterial material, int amountMb) {
        this.temperature = temperature;
        this.material = material;
        this.amountMb = amountMb;
    }

    public float getTemperature() {
        return temperature;
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

}
