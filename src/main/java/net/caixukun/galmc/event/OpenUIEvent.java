package net.caixukun.galmc.event;

import net.caixukun.galmc.ui.CGGalleryScreen;
import net.caixukun.galmc.ui.GalScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OpenUIEvent {
    public static String path = null;
    public static UUID uuid=null;
    public static boolean cg=false;
    public static Map<Screen,Screen> screenScreenMap = new HashMap<>();
    public static int circle = -1;
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.CLIENT){
            if(cg) {
                circle = Minecraft.getInstance().options.guiScale().get();
                Minecraft.getInstance().options.guiScale().set(4);
                Minecraft.getInstance().options.save();
                Minecraft.getInstance().resizeDisplay();
                if(event.player.getUUID()==uuid){
                    Minecraft.getInstance().setScreen(new CGGalleryScreen(uuid));
                    uuid=null;
                    cg=false;
                }
            }else {

                if (path != null && uuid != null && event.player.getUUID() == uuid) {
                    circle = Minecraft.getInstance().options.guiScale().get();
                    Minecraft.getInstance().options.guiScale().set(4);
                    Minecraft.getInstance().options.save();
                    Minecraft.getInstance().resizeDisplay();
                    Minecraft.getInstance().setScreen(new GalScreen(path, uuid));
                    path = null;
                    uuid = null;
                }
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onScreenClosing(ScreenEvent.Closing event) {
        // 检查是否按下了 ESC 键
            if(circle != -1){
                Minecraft.getInstance().options.guiScale().set(circle);
                Minecraft.getInstance().options.save();
                Minecraft.getInstance().resizeDisplay();
                circle = -1;
            }
            Screen currentScreen = event.getScreen();
            try {
                Screen parentScreen = screenScreenMap.get(currentScreen);

                // 如果有父屏幕，返回父屏幕
                if (parentScreen != null) {
                    Minecraft.getInstance().setScreen(parentScreen);
                    event.setCanceled(true); // 取消默认的 ESC 处理
                    screenScreenMap.clear();
                }
            }catch (Exception exception){

            }
    }
    @SubscribeEvent
    public static void onKeyPressed1(ScreenEvent.Opening event) {
        // 检查是否按下了 ESC 键
    }

    public static void openUI(String path,UUID uuid){
        OpenUIEvent.uuid=uuid;
        OpenUIEvent.path=path;
    }

    public static void openCG(UUID uuid){
        OpenUIEvent.uuid=uuid;
        OpenUIEvent.cg=true;
    }

    public static void addScreen(Screen screen1,Screen screen2){
        screenScreenMap.put(screen1,screen2);
    }

    public static void on_close_guiScale(int i){
        circle = i;
    }
}
