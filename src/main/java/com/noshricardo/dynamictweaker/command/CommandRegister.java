package com.noshricardo.dynamictweaker.command;


import com.noshricardo.dynamictweaker.DynamicTweaker;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = DynamicTweaker.MODID)
public class CommandRegister {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event){
        TestRecipeCommand.register(event.getDispatcher());
        RecipeEditorCommand.register(event.getDispatcher());
    }


}
