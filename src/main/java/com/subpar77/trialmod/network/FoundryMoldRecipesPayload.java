package com.subpar77.trialmod.network;

import com.subpar77.trialmod.TrialMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.List;


public record FoundryMoldRecipesPayload(int containerId, List<ItemStack> recipePreviews, int selectedRecipeIndex)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<FoundryMoldRecipesPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(
                    TrialMod.MODID, "foundry_mold_recipes"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FoundryMoldRecipesPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, FoundryMoldRecipesPayload::containerId,
                    ItemStack.OPTIONAL_LIST_STREAM_CODEC, FoundryMoldRecipesPayload::recipePreviews,
                    ByteBufCodecs.VAR_INT, FoundryMoldRecipesPayload::selectedRecipeIndex,
                    FoundryMoldRecipesPayload::new);

    @Override
    public CustomPacketPayload.Type<FoundryMoldRecipesPayload> type() {
        return TYPE;
    }
}
