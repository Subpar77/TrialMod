package com.subpar77.trialmod.foundry;

import com.subpar77.trialmod.foundry.material.FoundryMaterial;
import net.minecraft.util.StringRepresentable;

public enum FoundryChannelVisual implements StringRepresentable {
    NONE("none"),
    COPPER("copper");

    private final String serializedName;

    FoundryChannelVisual(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static FoundryChannelVisual fromMaterial(FoundryMaterial material) {
        return switch (material) {
            case COPPER -> COPPER;
        };
    }
}
