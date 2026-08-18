package net.caixukun.galmc.ui.character;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.caixukun.galmc.Galmc_api;
import net.caixukun.galmc.resource.GalResourceManger;
import net.caixukun.galmc.ui.GalScreen;
import net.caixukun.your_wife.render.character_render.CharacterMethods;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Character {
    public static final Logger LOGGER = LogUtils.getLogger();
    public Character(String resource){
        System.out.println(resource);
        if(no_fuck(resource)) {
            try {
                ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(Galmc_api.MODID, resource);
                this.id = resource;

                readJson(Minecraft.getInstance().getResourceManager(), resourceLocation);
            } catch (ResourceLocationException e) {
                LOGGER.error("资源路径不对且不符合格式", e);
                disabled = true;
            }
        }else {
            LOGGER.error("资源路径不对且不符合格式{}", resource);
            disabled = true;
        }


    }
    public final List<TextEntry> TEXTS = new ArrayList<>();
    public boolean disabled = false;
    public String id;
    //将会由json读取数据
    private static final Gson GSON = new GsonBuilder().create();
    public int pointer = 0;
    public int max = 0;
    public boolean init(){
        if(TEXTS.isEmpty()) return false;
        else return !disabled;
    }

    public String type;
    public String render_execute = null;
    public NextText next = null;
    public JsonObject music = null;
    public List<ResourceLocation> resources = new ArrayList<>();
    public End_Execute execute;

    public CharacterMethods characterMethods = new CharacterMethods();
    private boolean no_fuck(String s){
        for (String a : GalResourceManger.getText()){
            if(Objects.equals(a, s)) return true;
        }
        for (String a : GalResourceManger.getCCg()){
            if(Objects.equals(a, s)) return true;
        }
        return false;
    }
    private void readJson(ResourceManager resourceManager,ResourceLocation location) {

        // 使用 Minecraft 的 JSON 解析
        resourceManager.getResource(location).ifPresent(resource -> {
            try (InputStream stream = resource.open()) {
                JsonElement json = JsonParser.parseReader(
                        new InputStreamReader(stream, StandardCharsets.UTF_8)
                );
                JsonObject jsonObject = GSON.fromJson(json, JsonObject.class);
                // 转换为你的数据类
                this.type = jsonObject.get("type").getAsString();
                this.render_execute = jsonObject.get("render_execute").getAsString();
                this.music = jsonObject.getAsJsonObject("start_music");
                this.next = new NextText(
                        jsonObject.getAsJsonObject("next")
                );
                this.execute = new End_Execute(jsonObject.getAsJsonObject("execute"));
                this.max = jsonObject.getAsJsonArray("data").size();
                if(!is_cg()) {
                    for (JsonElement product : jsonObject.getAsJsonArray("data")) {
                        JsonObject item = product.getAsJsonObject();
                        TEXTS.add(
                                new TextEntry(
                                        item.get("text").getAsString(),
                                        item.getAsJsonObject("character").get("image").getAsString(),
                                        item.get("background").getAsString(),
                                        item.get("sound").getAsString(),
                                        item.getAsJsonObject("character").get("x").getAsInt(),
                                        item.getAsJsonObject("character").get("y").getAsInt(),
                                        item.getAsJsonObject("character").get("image_x").getAsInt(),
                                        item.getAsJsonObject("character").get("image_y").getAsInt(),
                                        item.getAsJsonObject("render_execute")
                                )
                        );
                    }
                }else {
                    for (JsonElement product : jsonObject.getAsJsonArray("data")) {
                        JsonObject item = product.getAsJsonObject();
                        TEXTS.add(
                                new TextEntry(
                                        null,
                                        "null",
                                        item.get("background").getAsString(),
                                        item.get("sound").getAsString(),
                                        0, 0,item.getAsJsonObject("character").get("image_x").getAsInt(),
                                        item.getAsJsonObject("character").get("image_y").getAsInt(),
                                        item.getAsJsonObject("render_execute")
                                )
                        );
                    }
                }
                for (JsonElement product :jsonObject.getAsJsonArray("resources")) {
                    resources.add(
                            ResourceLocation.fromNamespaceAndPath(Galmc_api.MODID,
                                    product.getAsString()
                            )
                    );
                }

            } catch (IOException e) {
                this.disabled=true;
                LOGGER.error("初始化错误",e);
            } catch (NullPointerException e){
                this.disabled=true;
                LOGGER.error("初始化错误",e);
            }
        });
    }

    public void render(GuiGraphics guiGraphics, GalScreen galScreen){
        if(!this.disabled) {
            if (Objects.equals(this.render_execute, "null")) {
                if (is_cg()) {
                    guiGraphics.fill(0, 0, galScreen.width, galScreen.height, 0xFF000000);
                    int ix = this.TEXTS.get(this.pointer).ix;
                    int iy = this.TEXTS.get(this.pointer).iy;
                    int wx = galScreen.width;
                    int wy = galScreen.height;
                    double scaleX = (double) wx / ix;
                    double scaleY = (double) wy / iy;
                    double scale = Math.min(scaleX, scaleY);   // 取较小缩放，保证完整显示

                    int drawWidth = (int) Math.round(ix * scale);
                    int drawHeight = (int) Math.round(iy * scale);

                    int x = (wx - drawWidth) / 2;
                    int y = (wy - drawHeight) / 2;
                    guiGraphics.blit(
                            this.TEXTS.get(this.pointer).background,
                            x, y, // 位置
                            0, 0,           // 纹理坐标
                            drawWidth,drawHeight,  // 尺寸
                            drawWidth,drawHeight       // 纹理尺寸
                    );
                } else this.TEXTS.get(this.pointer).render(guiGraphics, galScreen);
            } else {
                //characterMethods.execute(guiGraphics, this.render_execute, TEXTS, galScreen);
            }
        }
    }

    public boolean is_cg(){
        return Objects.equals(this.type, "cg");
    }

    public NextText next(){
        if(this.render_execute==null) this.render_execute="null";
        if(Objects.equals(this.render_execute, "null")) {
            if (this.pointer == max-1) {
                return this.next;
            } else {
                this.pointer++;
                return null;
            }
        }else {
            if (!characterMethods.next()) return this.next;
            return null;
        }
    }
    public boolean before(){
        if(this.pointer == 0){
            return false;
        }else {
            this.pointer--;
            return true;
        }
    }

    public String getSound(){
        return this.TEXTS.get(this.pointer).sound;
    }

    public boolean is_sound(){
        return this.TEXTS.get(this.pointer).is_sound();
    }

    public TextEntry get(){
        return this.TEXTS.get(this.pointer);
    }


}
