package com.noshricardo.dynamictweaker.network;

import com.noshricardo.dynamictweaker.DynamicTweaker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class EditorPayload implements CustomPacketPayload {

    public static final Type<EditorPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(DynamicTweaker.MODID, "editor_payload"));

    public static final EditorPayload INSTANCE = new EditorPayload();

    public static final StreamCodec<FriendlyByteBuf, EditorPayload> CODEC =
            StreamCodec.unit(INSTANCE);



    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
