package com.noshricardo.dynamictweaker;

import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.fml.util.ObfuscationReflectionHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeInjector {

    public static boolean injectRecipe(MinecraftServer server, ResourceLocation recipeId, RecipeHolder<?> newRecipe ){
        RecipeManager recipeManager = server.getRecipeManager();

//        Old Attempt, kept here for tracking, depending on lag tests for new version I might have to force the Immutable vars in RecipeManager to mutate
//        try {
//
//            Map<RecipeType<?>, Map<ResourceLocation, RecipeHolder<?>>> recipesByType =
//                    ObfuscationReflectionHelper.getPrivateValue(RecipeManager.class, recipeManager, "recipes");
//
//            if (recipesByType != null) {
//                Map<RecipeType<?>, Map<ResourceLocation, RecipeHolder<?>>> mutableTypes = new HashMap<>(recipesByType);
//
//                RecipeType<?> type = newRecipe.value().getType();
//                Map<ResourceLocation, RecipeHolder<?>> recipeMap = new HashMap<>(mutableTypes.getOrDefault(type, Map.of()));
//
//                recipeMap.put(recipeId, newRecipe);
//                mutableTypes.put(type, recipeMap);
//
//                ObfuscationReflectionHelper.setPrivateValue(RecipeManager.class, recipeManager, mutableTypes, "recipes");
//
//                syncRecipesToAllPlayers(server, recipeManager);
//
//            }
//
//        } catch (Exception e) {
//            DynamicTweaker.LOGGER.debug(e.getMessage());
//        }

        try {
            List<RecipeHolder<?>> workingRecipes = new ArrayList<>(recipeManager.getRecipes());

            workingRecipes.removeIf(holder -> holder.id().equals(recipeId));

            workingRecipes.add(newRecipe);

            recipeManager.replaceRecipes(workingRecipes);

            syncRecipesToAllPlayers(server, recipeManager);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void removeRecipe(MinecraftServer server, ResourceLocation recipeId) {
        RecipeManager recipeManager = server.getRecipeManager();

        try {
            List<RecipeHolder<?>> workingRecipes = new ArrayList<>(recipeManager.getRecipes());

            workingRecipes.removeIf(holder -> holder.id().equals(recipeId));

            recipeManager.replaceRecipes(workingRecipes);

            syncRecipesToAllPlayers(server, recipeManager);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void syncRecipesToAllPlayers(MinecraftServer server, RecipeManager recipeManager){

        ClientboundUpdateRecipesPacket syncPacket = new ClientboundUpdateRecipesPacket(recipeManager.getRecipes());


        for (ServerPlayer player : server.getPlayerList().getPlayers()){
            player.connection.send(syncPacket);
        }

    }


}
