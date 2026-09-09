package com.noshricardo.dynamictweaker.gui;

import com.noshricardo.dynamictweaker.util.RecipeIndexHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class RecipeSelectScreen extends Screen {

    private final Screen parentScreen;
    private EditBox searchBox;
    private List<Map.Entry<Item, List<RecipeHolder<?>>>> filteredEntries = new ArrayList<>();

    // Scroll tracking
    private double scrollOffset = 0;
    private static final int ENTRY_HEIGHT = 24;

    public RecipeSelectScreen(Screen parentScreen) {
        super(Component.literal("Select Recipe to Edit"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();

        // Search bar styled similar to the reference layout
        this.searchBox = new EditBox(this.font, this.width / 2 - 150, 30, 300, 20, Component.literal("Search recipes..."));
        this.searchBox.setResponder(this::filterRecipes);
        this.addRenderableWidget(this.searchBox);

        // Close/Back button
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), btn ->
                Minecraft.getInstance().setScreen(parentScreen)
        ).bounds(this.width / 2 - 60, this.height - 35, 120, 20).build());

        filterRecipes("");
    }

    private void filterRecipes(String query) {
        String lowerQuery = query.toLowerCase();
        Map<Item, List<RecipeHolder<?>>> allRecipes = RecipeIndexHelper.getOutputMap();

        filteredEntries = allRecipes.entrySet().stream()
                .filter(entry -> entry.getKey().getDescription().getString().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());

        scrollOffset = 0; // Reset scroll on new search
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollOffset = Math.max(0, Math.min(scrollOffset - scrollY * 12, Math.max(0, filteredEntries.size() * ENTRY_HEIGHT - 150)));
        return true;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);

        // Render the list of filtered recipes/outputs
        int startY = 65;
        int listWidth = 300;
        int startX = this.width / 2 - 150;

        guiGraphics.enableScissor(startX, startY, startX + listWidth, startY + 160);

        int renderY = startY - (int) scrollOffset;
        for (Map.Entry<Item, List<RecipeHolder<?>>> entry : filteredEntries) {
            if (renderY >= startY - ENTRY_HEIGHT && renderY <= startY + 160) {
                // Draw background row box
                guiGraphics.fill(startX, renderY, startX + listWidth, renderY + ENTRY_HEIGHT - 2, 0xFF222222);

                // Render item icon & name
                ItemStack stack = new ItemStack(entry.getKey());
                guiGraphics.renderItem(stack, startX + 4, renderY + 4);
                guiGraphics.drawString(this.font, stack.getDisplayName(), startX + 26, renderY + 8, 0xFFFFFF);

                // Show alternative recipe count tag if > 1
                if (entry.getValue().size() > 1) {
                    String altText = "(" + entry.getValue().size() + " recipes)";
                    guiGraphics.drawString(this.font, altText, startX + listWidth - 70, renderY + 8, 0xAAAAAA);
                }
            }
            renderY += ENTRY_HEIGHT;
        }

        guiGraphics.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle clicking rows to select a recipe and open your RecipeEditorScreen
        int startY = 65;
        int startX = this.width / 2 - 150;
        int listWidth = 300;

        if (mouseX >= startX && mouseX <= startX + listWidth && mouseY >= startY && mouseY <= startY + 160) {
            int clickedIndex = (int) ((mouseY - startY + scrollOffset) / ENTRY_HEIGHT);
            if (clickedIndex >= 0 && clickedIndex < filteredEntries.size()) {
                Map.Entry<Item, List<RecipeHolder<?>>> selectedEntry = filteredEntries.get(clickedIndex);

                // If there's multiple recipes, you can open a sub-selector or grab index 0 / let them cycle
                RecipeHolder<?> chosenRecipe = selectedEntry.getValue().get(0);

                UiLayout layout = new UiLayout(
                        chosenRecipe.value().getType().toString().contains("crafting") ?
                                net.minecraft.resources.ResourceLocation.withDefaultNamespace("crafting") :
                                chosenRecipe.id(),
                        "Recipe Editor",
                        chosenRecipe.value().getIngredients().size(),
                        3, // columns
                        false,
                        List.of()
                );

                Minecraft.getInstance().setScreen(new RecipeEditorScreen(layout, chosenRecipe));

                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

}
