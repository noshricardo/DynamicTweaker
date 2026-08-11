package com.noshricardo.dynamictweaker.network;

import com.noshricardo.dynamictweaker.RecipeInjector;
import com.noshricardo.dynamictweaker.util.DatapackRecipeWriter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPacketHandler {

    public static void HandleRecipe(final RecipePayload payload, final IPayloadContext context){
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (!player.hasPermissions(4)) return;

            MinecraftServer server = player.getServer();

            RecipeHolder<?> recipeHolder = payload.recipeHolder();
            ResourceLocation recipeId = payload.recipeId();

            RecipeInjector.injectRecipe(server, recipeId, recipeHolder);

            DatapackRecipeWriter.saveRecipeToDatapack(server, recipeHolder.id(), recipeHolder.value());

            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                p.getRecipeBook().sendInitialRecipeBook(p);
            }
        }
        );
    }

    public static void HandleRemoveRecipe(final RemoveRecipePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (!player.hasPermissions(4)) return;

            MinecraftServer server = player.getServer();
            ResourceLocation recipeId = payload.recipeId();

            // Remove the recipe from the server's RecipeManager map
            RecipeInjector.removeRecipe(server, recipeId);

            DatapackRecipeWriter.removeRecipeFromDatapack(server, recipeId);

            // Sync recipe book updates to players
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                p.getRecipeBook().sendInitialRecipeBook(p);
            }
        });
    }

}
