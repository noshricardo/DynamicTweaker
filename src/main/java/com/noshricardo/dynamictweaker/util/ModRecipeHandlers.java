package com.noshricardo.dynamictweaker.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ModRecipeHandlers {

    public interface RecipeHandlerAdapter {
        List<ItemStack> extractInputs(Recipe<?> recipe);
        Recipe<?> saveInputs(Recipe<?> original, List<ItemStack> newInputs);
    }

    private static final Map<Class<?>, RecipeHandlerAdapter> HANDLERS = new HashMap<>();

    public static void register(Class<?> recipeClass, RecipeHandlerAdapter adapter) {
        HANDLERS.put(recipeClass, adapter);
    }

    public static RecipeHandlerAdapter getHandler(Recipe<?> recipe) {
        Class<?> clazz = recipe.getClass();
        // Check exact match or superclasses/interfaces
        while (clazz != Object.class && clazz != null) {
            if (HANDLERS.containsKey(clazz)) {
                return HANDLERS.get(clazz);
            }
            for (Class<?> inf : clazz.getInterfaces()) {
                if (HANDLERS.containsKey(inf)) {
                    return HANDLERS.get(inf);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    // Example registration for Mekanism or generic multi-input processing machines
    public static void registerDefaults() {
        // Generic fallback or specific Mekanism recipe adapters can be registered here.
        // You can register custom handlers dynamically for any modded machine class type.
        
    }
}