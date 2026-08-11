package com.noshricardo.dynamictweaker.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RecipeIndexHelper {

    private static final Map<Item, List<RecipeHolder<?>>> OUTPUT_MAP = new LinkedHashMap<>();

    public static void buildIndex(RecipeManager recipeManager) {
        OUTPUT_MAP.clear();
        for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
            HolderLookup.Provider registries = Minecraft.getInstance().level.registryAccess();
            ItemStack result = holder.value().getResultItem(registries);
            if (!result.isEmpty()) {
                OUTPUT_MAP.computeIfAbsent(result.getItem(), k -> new ArrayList<>()).add(holder);
            }
        }
    }

    public static Map<Item, List<RecipeHolder<?>>> getOutputMap() {
        return OUTPUT_MAP;
    }

}
