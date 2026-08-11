package com.noshricardo.dynamictweaker.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ItemSearchScreen extends Screen {

    private final Screen parentScreen;
    private final Consumer<ItemStack> onItemSelected;
    private EditBox searchBox;
    private List<Item> filteredItems = new ArrayList<>();

    private double scrollOffset = 0;
    private static final int SLOT_SIZE = 20;
    private static final int COLUMNS = 9;

    public ItemSearchScreen(Screen parentScreen, Consumer<ItemStack> onItemSelected) {
        super(Component.literal("Select Item"));
        this.parentScreen = parentScreen;
        this.onItemSelected = onItemSelected;
    }

    @Override
    protected void init() {
        super.init();

        int panelWidth = COLUMNS * (SLOT_SIZE + 4) + 16;
        int startX = (this.width - panelWidth) / 2;

        this.searchBox = new EditBox(this.font, startX + 8, 30, panelWidth - 16, 20, Component.literal("Search items..."));
        this.searchBox.setResponder(this::filterItems);
        this.addRenderableWidget(this.searchBox);

        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), btn ->
                Minecraft.getInstance().setScreen(parentScreen)
        ).bounds(this.width / 2 - 60, this.height - 35, 120, 20).build());

        filterItems("");
    }

    private void filterItems(String query) {
        String lowerQuery = query.toLowerCase();
        filteredItems = BuiltInRegistries.ITEM.stream()
                .filter(item -> new ItemStack(item).getDisplayName().getString().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
        scrollOffset = 0;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxRows = (int) Math.ceil((double) filteredItems.size() / COLUMNS);
        double maxScroll = Math.max(0, (maxRows * (SLOT_SIZE + 4)) - 140);
        scrollOffset = Math.max(0, Math.min(scrollOffset - scrollY * 12, maxScroll));
        return true;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);

        int panelWidth = COLUMNS * (SLOT_SIZE + 4) + 16;
        int startX = (this.width - panelWidth) / 2;
        int startY = 60;
        int listHeight = 140;

        guiGraphics.enableScissor(startX, startY, startX + panelWidth, startY + listHeight);

        int renderY = startY - (int) scrollOffset;
        int renderX = startX + 8;
        int col = 0;

        for (Item item : filteredItems) {
            int x = renderX + (col * (SLOT_SIZE + 4));
            int y = renderY;

            if (y >= startY - SLOT_SIZE && y <= startY + listHeight) {
                // Draw slot background
                guiGraphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF373737);
                ItemStack stack = new ItemStack(item);
                guiGraphics.renderItem(stack, x + 2, y + 2);

                // Hover check
                if (mouseX >= x && mouseX <= x + SLOT_SIZE && mouseY >= y && mouseY <= y + SLOT_SIZE) {
                    guiGraphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0x55FFFFFF);
                    guiGraphics.renderTooltip(this.font, stack, (int) mouseX, (int) mouseY);
                }
            }

            col++;
            if (col >= COLUMNS) {
                col = 0;
                renderY += SLOT_SIZE + 4;
            }
        }

        guiGraphics.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int panelWidth = COLUMNS * (SLOT_SIZE + 4) + 16;
        int startX = (this.width - panelWidth) / 2;
        int startY = 60;
        int listHeight = 140;

        if (mouseX >= startX && mouseX <= startX + panelWidth && mouseY >= startY && mouseY <= startY + listHeight) {
            int relativeX = (int) (mouseX - (startX + 8));
            int relativeY = (int) (mouseY - startY + scrollOffset);

            int col = relativeX / (SLOT_SIZE + 4);
            int row = relativeY / (SLOT_SIZE + 4);
            int index = (row * COLUMNS) + col;

            if (col >= 0 && col < COLUMNS && index >= 0 && index < filteredItems.size()) {
                Item selected = filteredItems.get(index);
                onItemSelected.accept(new ItemStack(selected));
                Minecraft.getInstance().setScreen(parentScreen);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}