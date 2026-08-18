package net.caixukun.galmc;

import com.mojang.logging.LogUtils;
import net.caixukun.galmc.event.sync.InformationServer;
import net.caixukun.galmc.init.CommandInit;
import net.caixukun.galmc.init.ItemInit;
import net.caixukun.galmc.resource.GalResourceManger;
import net.caixukun.galmc.event.OpenUIEvent;
import net.caixukun.galmc.event.ExecuteEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Galmc_api.MODID)
public class Galmc_api {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "galmc_api";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "galmc_api" namespace

    public Galmc_api(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        GalResourceManger.handleResourcePack();
        GalResourceManger.getCCg();
        GalResourceManger.getText();
        GalResourceManger.getCgUI();
        ItemInit.ITEMS.register(modEventBus);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(new ExecuteEvent());
        MinecraftForge.EVENT_BUS.register(new OpenUIEvent());
        modEventBus.addListener(this::addCreative);

    }



    private String toJsonArray(List<String> list) {
        return "[" + list.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(",")) + "]";
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    private void registerCommands(RegisterCommandsEvent event) {
        CommandInit.register(event.getDispatcher());
    }
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ItemInit.GUI_TEST);
        }
    }
    // Add the example block item to the building blocks tab
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
