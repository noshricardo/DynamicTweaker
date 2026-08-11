package com.noshricardo.dynamictweaker.gui;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record UiLayout(
        ResourceLocation recipeTypeId,
        String displayName,
        int inputSlotsCount,
        int columns,
        boolean supportsFluids,
        List<String> extraFields
) {

}
