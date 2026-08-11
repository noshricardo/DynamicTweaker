package com.noshricardo.dynamictweaker.network;

import com.noshricardo.dynamictweaker.LayoutRegistry;
import com.noshricardo.dynamictweaker.gui.RecipeEditorScreen;
import com.noshricardo.dynamictweaker.gui.RecipeSelectScreen;
import com.noshricardo.dynamictweaker.util.RecipeIndexHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPacketHandler {

    public static void handleEditor(final EditorPayload payload, final IPayloadContext context){

        context.enqueueWork(() -> {
            //Minecraft.getInstance().setScreen(new RecipeEditorScreen(LayoutRegistry.get(ResourceLocation.withDefaultNamespace("crafting_shaped"))));
            RecipeIndexHelper.buildIndex(Minecraft.getInstance().getConnection().getRecipeManager());
            Minecraft.getInstance().setScreen(new RecipeSelectScreen(null));
        });

    }

}
