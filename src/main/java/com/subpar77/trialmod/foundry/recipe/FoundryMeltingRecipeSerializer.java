package com.subpar77.trialmod.foundry.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.subpar77.trialmod.foundry.material.FoundryMaterial;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class FoundryMeltingRecipeSerializer implements RecipeSerializer<FoundryMeltingRecipe> {

    public static final MapCodec<FoundryMeltingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(FoundryMeltingRecipe::getIngredient),
                    FoundryMaterial.CODEC.fieldOf("material").forGetter(FoundryMeltingRecipe::getMaterial),
                    Codec.intRange(1, Integer.MAX_VALUE).fieldOf("amountMb").forGetter(FoundryMeltingRecipe::getAmountMb))
                    .apply(instance, FoundryMeltingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FoundryMeltingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,FoundryMeltingRecipe::getIngredient,
            ByteBufCodecs.fromCodec(FoundryMaterial.CODEC), FoundryMeltingRecipe::getMaterial,
            ByteBufCodecs.VAR_INT, FoundryMeltingRecipe::getAmountMb, FoundryMeltingRecipe::new);

    @Override
    public MapCodec<FoundryMeltingRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, FoundryMeltingRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
