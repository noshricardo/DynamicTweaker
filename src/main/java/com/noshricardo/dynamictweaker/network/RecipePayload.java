package com.noshricardo.dynamictweaker.network;

import com.noshricardo.dynamictweaker.DynamicTweaker;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

public record RecipePayload(
        ResourceLocation recipeId,
        RecipeHolder<?> recipeHolder
) implements CustomPacketPayload {

    public static final Type<RecipePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(DynamicTweaker.MODID, "recipe"));


    public static final StreamCodec<RegistryFriendlyByteBuf, RecipePayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                ResourceLocation.STREAM_CODEC.encode(buf, payload.recipeId);
                RecipeHolder.STREAM_CODEC.encode(buf, payload.recipeHolder);
            },
            buf -> new RecipePayload(
                ResourceLocation.STREAM_CODEC.decode(buf),
                RecipeHolder.STREAM_CODEC.decode(buf)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
