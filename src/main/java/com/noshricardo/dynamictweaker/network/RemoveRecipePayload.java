package com.noshricardo.dynamictweaker.network;

import com.noshricardo.dynamictweaker.DynamicTweaker;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RemoveRecipePayload(ResourceLocation recipeId) implements CustomPacketPayload {

    public static final Type<RemoveRecipePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(DynamicTweaker.MODID, "remove_recipe"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveRecipePayload> CODEC = StreamCodec.of(
            (buf, payload) -> ResourceLocation.STREAM_CODEC.encode(buf, payload.recipeId),
            buf -> new RemoveRecipePayload(ResourceLocation.STREAM_CODEC.decode(buf))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}