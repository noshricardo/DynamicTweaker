package com.noshricardo.dynamictweaker.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class RecipeEditorScreen extends Screen {

    private final UiLayout layout;

    private final List<EditBox> inputFields = new ArrayList<>();



    public RecipeEditorScreen(UiLayout layout) {
        super(Component.literal("Recipe Editor"));
        this.layout = layout;
    }

    @Override
    protected void init() {
        super.init();
        inputFields.clear();

        int startY = 50;
        int startX = 20;
        int row = 0;
        int collum = 0;

        for (int i = 0; i < layout.inputSlotsCount(); i++) {
            int slotIndex = i;

            this.addRenderableWidget(Button.builder(
                    Component.literal("Slot #" + (slotIndex + 1)),
                    button -> openItemSearchOverlay(slotIndex)
            ).bounds(startX + (collum * 55), startY + (row * 25), 50, 20).build());
            collum++;
            if(collum >= layout.columns()){
                row++;
                collum = 0;
            }
        }

        int fieldY = 50;
        for (String fieldName : layout.extraFields()) {
            EditBox box = new EditBox(this.font, 160, fieldY, 100, 20, Component.literal(fieldName));
            box.setHint(Component.literal(fieldName));
            this.addRenderableWidget(box);
            inputFields.add(box);
            fieldY += 24;
        }

        this.addRenderableWidget(Button.builder(Component.literal("Save and Inject"), button -> {
            compileAndSendRecipe();
        }).bounds(this.width/2-60, this.height -30, 120, 20).build());

    }

    private void openItemSearchOverlay(int slotIndex){

    }

    private void compileAndSendRecipe(){

    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick){

        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

    }

    @Override
    public boolean isPauseScreen(){
        return false;
    }


}
