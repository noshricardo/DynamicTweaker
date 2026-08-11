package com.noshricardo.dynamictweaker.gui;

import com.noshricardo.dynamictweaker.network.RecipePayload;
import com.noshricardo.dynamictweaker.network.RemoveRecipePayload;
import com.noshricardo.dynamictweaker.util.ModRecipeHandlers;
import com.noshricardo.dynamictweaker.util.RecipeIndexHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import java.util.ArrayList;
import java.util.List;

public class RecipeEditorScreen extends Screen {

    private final UiLayout layout;
    private List<RecipeHolder<?>> availableRecipes;
    private int currentRecipeIndex = 0;
    private RecipeHolder<?> currentRecipe;

    private final List<ItemStack> currentInputs = new ArrayList<>();
    private final List<SlotBounds> slotBoundsList = new ArrayList<>();

    public RecipeEditorScreen(UiLayout layout, RecipeHolder<?> initialRecipe) {
        super(Component.literal("Recipe Editor"));
        this.layout = layout;

        ItemStack resultStack = ItemStack.EMPTY;
        try {
            resultStack = initialRecipe.value().getResultItem(Minecraft.getInstance().level.registryAccess());
        } catch (Exception ignored) {}

        if (resultStack.isEmpty()) {
            resultStack = getModdedRecipeOutput(initialRecipe.value());
        }

        this.availableRecipes = RecipeIndexHelper.getOutputMap().getOrDefault(resultStack.getItem(), List.of(initialRecipe));

        this.currentRecipeIndex = 0;
        for (int i = 0; i < availableRecipes.size(); i++) {
            if (availableRecipes.get(i).id().equals(initialRecipe.id())) {
                this.currentRecipeIndex = i;
                break;
            }
        }

        loadRecipeData(availableRecipes.get(currentRecipeIndex));
    }

    private void loadRecipeData(RecipeHolder<?> recipe) {
        this.currentRecipe = recipe;
        currentInputs.clear();
        Recipe<?> value = recipe.value();

        List<ItemStack> extractedStacks = new ArrayList<>();

        if (value instanceof ShapedRecipe shaped) {
            for (int i = 0; i < 9; i++) extractedStacks.add(ItemStack.EMPTY);
            List<Ingredient> ingredients = shaped.getIngredients();
            int width = shaped.getWidth();
            int height = shaped.getHeight();

            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    int ingIndex = row * width + col;
                    if (ingIndex < ingredients.size()) {
                        Ingredient ing = ingredients.get(ingIndex);
                        if (ing != null && !ing.isEmpty()) {
                            ItemStack[] matching = ing.getItems();
                            if (matching.length > 0) {
                                extractedStacks.set(row * 3 + col, matching[0].copy());
                            }
                        }
                    }
                }
            }
        } else if (value instanceof ShapelessRecipe shapeless) {
            for (Ingredient ing : shapeless.getIngredients()) {
                if (ing != null && !ing.isEmpty() && ing.getItems().length > 0) {
                    extractedStacks.add(ing.getItems()[0].copy());
                }
            }
        } else {
            // Check registered modded handlers
            var handler = ModRecipeHandlers.getHandler(value);
            if (handler != null) {
                extractedStacks.addAll(handler.extractInputs(value));
            } else {
                // Fallback attempt via standard ingredient interfaces
                try {
                    for (Ingredient ingredient : value.getIngredients()) {
                        if (ingredient != null && !ingredient.isEmpty() && ingredient.getItems().length > 0) {
                            extractedStacks.add(ingredient.getItems()[0].copy());
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        // Fill out 9 matrix slots
        for (int i = 0; i < 9; i++) {
            if (i < extractedStacks.size() && extractedStacks.get(i) != null) {
                currentInputs.add(extractedStacks.get(i));
            } else {
                currentInputs.add(ItemStack.EMPTY);
            }
        }
    }

    private List<ItemStack> getModdedInputs(Recipe<?> recipe) {
        List<ItemStack> stacks = new ArrayList<>();
        try {
            for (var method : recipe.getClass().getMethods()) {
                String name = method.getName().toLowerCase();
                if ((name.contains("input") || name.contains("item") || name.contains("gas") || name.contains("chemical"))
                        && method.getParameterCount() == 0) {
                    Object result = method.invoke(recipe);
                    if (result instanceof ItemStack stack && !stack.isEmpty()) {
                        stacks.add(stack.copy());
                    } else if (result instanceof Ingredient ing && !ing.isEmpty()) {
                        ItemStack[] items = ing.getItems();
                        if (items.length > 0) stacks.add(items[0].copy());
                    } else if (result != null && result.getClass().getName().toLowerCase().contains("stack")) {
                        // Handle Mekanism ChemicalStack / GasStack proxy display items if available via reflection
                        try {
                            var getStackMethod = result.getClass().getMethod("getItemStack");
                            if (getStackMethod.invoke(result) instanceof ItemStack st && !st.isEmpty()) {
                                stacks.add(st.copy());
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Exception ignored) {}
        return stacks;
    }

    private static ItemStack getModdedRecipeOutput(Recipe<?> recipe) {
        try {
            for (var method : recipe.getClass().getMethods()) {
                String name = method.getName().toLowerCase();
                if ((name.contains("output") || name.contains("result")) && method.getParameterCount() == 0) {
                    Object result = method.invoke(recipe);
                    if (result instanceof ItemStack st && !st.isEmpty()) return st.copy();
                    try {
                        var getStackMethod = result.getClass().getMethod("getItemStack");
                        if (getStackMethod.invoke(result) instanceof ItemStack st && !st.isEmpty()) return st.copy();
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {}
        return ItemStack.EMPTY;
    }

    @Override
    protected void init() {
        super.init();
        slotBoundsList.clear();

        int startY = 70;
        int startX = 40;
        int slotSize = 22;

        for (int i = 0; i < 9; i++) {
            int row = i / 3;
            int col = i % 3;
            int x = startX + (col * (slotSize + 4));
            int y = startY + (row * (slotSize + 4));
            slotBoundsList.add(new SlotBounds(x, y, slotSize, slotSize, i));
        }

        if (availableRecipes.size() > 1) {
            this.addRenderableWidget(Button.builder(Component.literal("< Prev Alt"), btn -> {
                currentRecipeIndex = (currentRecipeIndex - 1 + availableRecipes.size()) % availableRecipes.size();
                loadRecipeData(availableRecipes.get(currentRecipeIndex));
                this.init();
            }).bounds(startX, startY + 80, 75, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("Next Alt >"), btn -> {
                currentRecipeIndex = (currentRecipeIndex + 1) % availableRecipes.size();
                loadRecipeData(availableRecipes.get(currentRecipeIndex));
                this.init();
            }).bounds(startX + 80, startY + 80, 75, 20).build());
        }

        this.addRenderableWidget(Button.builder(Component.literal("Save and Inject"), button -> {
            compileAndSendRecipe();
        }).bounds(this.width / 2 - 60, this.height - 35, 120, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Delete Recipe"), btn -> {
            if (currentRecipe != null) {
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                        new RemoveRecipePayload(currentRecipe.id())
                );
                Minecraft.getInstance().setScreen(null);
            }
        }).bounds(startX, startY + 110, 75, 20).build());

        // Add New Recipe Button (Clones current as a template with a fresh ID)
        this.addRenderableWidget(Button.builder(Component.literal("+ New Recipe"), btn -> {
            createNewRecipeVariant();
        }).bounds(startX + 80, startY + 110, 75, 20).build());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (SlotBounds slot : slotBoundsList) {
            if (mouseX >= slot.x && mouseX <= slot.x + slot.width && mouseY >= slot.y && mouseY <= slot.y + slot.height) {
                openItemSearchOverlay(slot.index);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void openItemSearchOverlay(int slotIndex) {
        Minecraft.getInstance().setScreen(new ItemSearchScreen(this, selectedStack -> {
            currentInputs.set(slotIndex, selectedStack);
        }));
    }

    private void createNewRecipeVariant() {
        ResourceLocation newId = ResourceLocation.fromNamespaceAndPath(
                "dynamictweaker",
                "custom_recipe_" + System.currentTimeMillis()
        );



        // Provide at least one default item so the recipe isn't blank on creation
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(Ingredient.of(net.minecraft.world.item.Items.OAK_PLANKS));
        for (int i = 1; i < 9; i++) ingredients.add(Ingredient.EMPTY);

        ShapelessRecipe newRecipeValue = new ShapelessRecipe(
                "dynamictweaker_custom",
                net.minecraft.world.item.crafting.CraftingBookCategory.MISC,
                currentRecipe.value().getResultItem(Minecraft.getInstance().level.registryAccess()),
                ingredients
        );

        RecipeHolder<?> newHolder = new RecipeHolder<>(newId, newRecipeValue);
        currentRecipe = newHolder;
        compileAndSendRecipe();

        // Send payload to the server
        //net.neoforged.neoforge.network.PacketDistributor.sendToServer(new RecipePayload(newId, newHolder));

        Minecraft.getInstance().setScreen(null);
    }

    private void compileAndSendRecipe() {
        Recipe<?> originalValue = currentRecipe.value();
        Recipe<?> updatedValue;

        try {
            if (originalValue instanceof ShapedRecipe shaped) {
                // [Shaped compilation logic remains unchanged]
                int minCol = 3, maxCol = -1, minRow = 3, maxRow = -1;
                for (int r = 0; r < 3; r++) {
                    for (int c = 0; c < 3; c++) {
                        if (!currentInputs.get(r * 3 + c).isEmpty()) {
                            minCol = Math.min(minCol, c); maxCol = Math.max(maxCol, c);
                            minRow = Math.min(minRow, r); maxRow = Math.max(maxRow, r);
                        }
                    }
                }
                if (maxCol == -1) { minCol = 0; maxCol = 0; minRow = 0; maxRow = 0; }

                List<String> patternRows = new ArrayList<>();
                java.util.Map<Character, Ingredient> keyMap = new java.util.HashMap<>();
                char nextKey = 'A';

                for (int r = minRow; r <= maxRow; r++) {
                    StringBuilder rowStr = new StringBuilder();
                    for (int c = minCol; c <= maxCol; c++) {
                        ItemStack stack = currentInputs.get(r * 3 + c);
                        if (stack.isEmpty()) {
                            rowStr.append(' ');
                        } else {
                            char matchedKey = ' ';
                            for (var entry : keyMap.entrySet()) {
                                if (entry.getValue().test(stack)) { matchedKey = entry.getKey(); break; }
                            }
                            if (matchedKey == ' ') {
                                matchedKey = nextKey++;
                                keyMap.put(matchedKey, Ingredient.of(stack));
                            }
                            rowStr.append(matchedKey);
                        }
                    }
                    patternRows.add(rowStr.toString());
                }

                ShapedRecipePattern pattern = ShapedRecipePattern.of(keyMap, patternRows.toArray(new String[0]));
                updatedValue = new ShapedRecipe(shaped.getGroup(), shaped.category(), pattern, shaped.getResultItem(Minecraft.getInstance().level.registryAccess()));

            } else if (originalValue instanceof ShapelessRecipe shapeless) {
                NonNullList<Ingredient> newIngredients = NonNullList.create();
                for (int i = 0; i < 9; i++) {
                    ItemStack stack = currentInputs.get(i);
                    if (!stack.isEmpty()) newIngredients.add(Ingredient.of(stack));
                }
                updatedValue = new ShapelessRecipe(shapeless.getGroup(), shapeless.category(), shapeless.getResultItem(Minecraft.getInstance().level.registryAccess()), newIngredients);
            } else {
                // Route through registered mod handlers
                var handler = ModRecipeHandlers.getHandler(originalValue);
                if (handler != null) {
                    updatedValue = handler.saveInputs(originalValue, currentInputs);
                } else {
                    updatedValue = originalValue;
                }
            }
        } catch (Exception e) {
            updatedValue = originalValue;
        }

        RecipeHolder<?> updatedHolder = new RecipeHolder<>(currentRecipe.id(), updatedValue);
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new RecipePayload(currentRecipe.id(), updatedHolder));
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawCenteredString(this.font, "Editing Recipe: " + currentRecipe.id().getPath(), this.width / 2, 20, 0xFFFFFF);
        if (availableRecipes.size() > 1) {
            guiGraphics.drawCenteredString(this.font, "Recipe Variant: " + (currentRecipeIndex + 1) + " / " + availableRecipes.size(), this.width / 2, 35, 0xAAAAAA);
        }

        for (SlotBounds slot : slotBoundsList) {
            guiGraphics.fill(slot.x, slot.y, slot.x + slot.width, slot.y + slot.height, 0xFF8B8B8B);
            guiGraphics.fill(slot.x + 1, slot.y + 1, slot.x + slot.width - 1, slot.y + slot.height - 1, 0xFF373737);

            ItemStack stack = currentInputs.get(slot.index);
            if (!stack.isEmpty()) {
                guiGraphics.renderItem(stack, slot.x + 3, slot.y + 3);
                guiGraphics.renderItemDecorations(this.font, stack, slot.x + 3, slot.y + 3);
            }

            if (mouseX >= slot.x && mouseX <= slot.x + slot.width && mouseY >= slot.y && mouseY <= slot.y + slot.height) {
                if (!stack.isEmpty()) {
                    guiGraphics.renderTooltip(this.font, stack, (int) mouseX, (int) mouseY);
                } else {
                    guiGraphics.renderTooltip(this.font, Component.literal("Slot #" + (slot.index + 1) + " (Empty)"), (int) mouseX, (int) mouseY);
                }
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record SlotBounds(int x, int y, int width, int height, int index) {}
}