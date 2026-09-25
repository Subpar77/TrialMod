package com.subpar77.trialmod.foundry;

import java.util.Optional;

public class FoundryThermal {
    private static final float AMBIENT_TEMPERATURE = 70.0F;
    private static final float COOLING_RATE = 2.0F;

    private FoundryThermal() {}

    public static float calculateNewTemperature(float currentTemperature, Optional<HeatSourceData> heat,
                                                 FoundryTier tier) {
        if (heat.isPresent()) {
            HeatSourceData source = heat.get();
            float targetTemperature = Math.min(source.maxTemperature(), tier.getMaxTemperature());

            return Math.min(currentTemperature + source.heatingRate(), targetTemperature);
        }
        return calculateCooling(currentTemperature);
    }

    public static float calculateCooling(float currentTemperature) {
        return Math.max(currentTemperature - COOLING_RATE, AMBIENT_TEMPERATURE);
    }

    public static boolean isAtAmbient(float temperature) {
        return temperature <= AMBIENT_TEMPERATURE;
    }
}
