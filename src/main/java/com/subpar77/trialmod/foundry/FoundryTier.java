package com.subpar77.trialmod.foundry;

public enum FoundryTier {
    STONE(2800.0F);

    private final float maxTemperature;

    FoundryTier(float maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public float getMaxTemperature() {
        return maxTemperature;
    }
}
