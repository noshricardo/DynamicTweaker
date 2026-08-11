package com.noshricardo.dynamictweaker;

import com.noshricardo.dynamictweaker.gui.UiLayout;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LayoutRegistry {

    private static final Map<ResourceLocation, UiLayout> LAYOUTS = new HashMap<>();

    public static void registerDefaults(){
        register(new UiLayout(
                ResourceLocation.withDefaultNamespace("crafting_shaped"),

                "Shaped Crafting", 9, 3, false, List.of()
        ));
        register(new UiLayout(
                ResourceLocation.withDefaultNamespace("crafting_shapeless"),
                "Shapeless Crafting", 9, 3, false, List.of()
        ));
    }

    public static void register(UiLayout layout){
        LAYOUTS.put(layout.recipeTypeId(), layout);
    }

    public static UiLayout get(ResourceLocation id) {
        return LAYOUTS.get(id);
    }

}
