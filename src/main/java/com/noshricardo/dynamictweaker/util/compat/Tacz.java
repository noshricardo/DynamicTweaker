package com.noshricardo.dynamictweaker.util.compat;

import com.noshricardo.dynamictweaker.util.ModRecipeHandlers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;

public class Tacz implements ModRecipeHandlers.RecipeHandlerAdapter {
    @Override
    public List<ItemStack> extractInputs(Recipe<?> recipe) {
        return List.of();
    }

    @Override
    public Recipe<?> saveInputs(Recipe<?> original, List<ItemStack> newInputs) {
        return null;
    }
}
