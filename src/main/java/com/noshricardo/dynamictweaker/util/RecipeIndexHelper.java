package com.noshricardo.dynamictweaker.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RecipeIndexHelper {

    private static final Map<Item, List<RecipeHolder<?>>> OUTPUT_MAP = new LinkedHashMap<>();

    public static void buildIndex(RecipeManager recipeManager) {
        OUTPUT_MAP.clear();
        for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
            Recipe<?> recipe = holder.value();

            // Check if it's a vanilla supported crafting type OR has a registered custom modded handler
            boolean isVanillaSupported = (recipe instanceof ShapedRecipe) ||
                    (recipe instanceof ShapelessRecipe) ||
                    (recipe instanceof StonecutterRecipe);

            boolean hasCustomHandler = ModRecipeHandlers.getHandler(recipe) != null;

            // Hide completely unsupported types to prevent crashes
            if (!isVanillaSupported && !hasCustomHandler) {
                continue;
            }

            HolderLookup.Provider registries = Minecraft.getInstance().level.registryAccess();
            ItemStack result = ItemStack.EMPTY;

            try {
                result = recipe.getResultItem(registries);
            } catch (Exception ignored) {}

            // Fallback for custom handlers/modded outputs if result item is empty
            if (result.isEmpty()) {
                result = getModdedRecipeOutput(recipe);
            }

            if (!result.isEmpty() && !result.getItem().equals(net.minecraft.world.item.Items.AIR)) {
                OUTPUT_MAP.computeIfAbsent(result.getItem(), k -> new ArrayList<>()).add(holder);
            }
        }
    }

    private static ItemStack getModdedRecipeOutput(Recipe<?> recipe) {
        try {
            for (var method : recipe.getClass().getMethods()) {
                String name = method.getName().toLowerCase();
                if ((name.contains("output") || name.contains("result")) && method.getParameterCount() == 0) {
                    Object res = method.invoke(recipe);
                    if (res instanceof ItemStack st && !st.isEmpty()) {
                        return st.copy();
                    }
                    try {
                        var getStack = res.getClass().getMethod("getItemStack");
                        if (getStack.invoke(res) instanceof ItemStack st && !st.isEmpty()) {
                            return st.copy();
                        }
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {}
        return ItemStack.EMPTY;
    }

    public static Map<Item, List<RecipeHolder<?>>> getOutputMap() {
        return OUTPUT_MAP;
    }
}