package com.noshricardo.dynamictweaker;

import com.mojang.logging.LogUtils;
import com.noshricardo.dynamictweaker.network.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(DynamicTweaker.MODID)
public class DynamicTweaker {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "dynamictweaker";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();


    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public DynamicTweaker(IEventBus modEventBus, ModContainer modContainer) {

        modEventBus.addListener(this::registerPackets);

        LayoutRegistry.registerDefaults();

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (DynamicTweaker) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        //NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void registerPackets(final RegisterPayloadHandlersEvent event){
        final PayloadRegistrar registrar = event.registrar("1.0.0");


        registrar.playToServer(
                RecipePayload.TYPE,
                RecipePayload.CODEC,
                (payload, context) -> ServerPacketHandler.HandleRecipe(payload, context)
        );

        registrar.playToServer(
                RemoveRecipePayload.TYPE,
                RemoveRecipePayload.CODEC,
                (payload, context) -> ServerPacketHandler.HandleRemoveRecipe(payload, context)
        );

        registrar.playToClient(
                EditorPayload.TYPE,
                EditorPayload.CODEC,
                (payload, context) -> ClientPacketHandler.handleEditor(payload, context)
        );


    }



}
