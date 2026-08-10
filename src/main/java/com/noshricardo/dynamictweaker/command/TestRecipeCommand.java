package com.noshricardo.dynamictweaker.command;

import com.mojang.brigadier.CommandDispatcher;
import com.noshricardo.dynamictweaker.RecipeInjector;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public class TestRecipeCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("testrecipeinject")
                .requires(source -> source.hasPermission(4)) // Require OP status
                .executes(context -> {
                    CommandSourceStack source = context.getSource();

                    // 1. Create a unique identifier for the custom recipe
                    ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath("dynamictweaker", "test_diamond_dirt_to_netherite");

                    // 2. Build ingredients list (1 Diamond + 1 Dirt)
                    NonNullList<Ingredient> ingredients = NonNullList.create();
                    ingredients.add(Ingredient.of(Items.DIAMOND));
                    ingredients.add(Ingredient.of(Items.DIRT));

                    // 3. Define the resulting output (1 Netherite Ingot)
                    ItemStack outputStack = new ItemStack(Items.NETHERITE_INGOT);

                    // 4. Construct a Shapeless Crafting Recipe
                    // Parameters: Group name string, Crafting Book Category, Output ItemStack, Input Ingredients list
                    CraftingBookCategory category = CraftingBookCategory.MISC;
                    ShapelessRecipe testRecipe = new ShapelessRecipe(
                            "",
                            category,
                            outputStack,
                            ingredients
                    );

                    // 5. Wrap inside a RecipeHolder container
                    RecipeHolder<ShapelessRecipe> recipeHolder = new RecipeHolder<>(recipeId, testRecipe);

                    // 6. Inject directly into the server's live memory map & push packet to clients
                    RecipeInjector.injectRecipe(source.getServer(), recipeId, recipeHolder);

                    // 7. Feedback notification in chat
                    source.sendSuccess(() -> Component.literal("Successfully injected test recipe check your crafting table."), true);

                    return 1;
                })
        );
    }

}
