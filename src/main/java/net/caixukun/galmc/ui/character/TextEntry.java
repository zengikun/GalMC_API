package net.caixukun.galmc.ui.character;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.caixukun.galmc.Galmc_api;
import net.caixukun.galmc.ui.GalScreen;
import net.caixukun.your_wife.render.text_render.TextMethods;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.util.Objects;

public class TextEntry {
    public static final Logger LOGGER = LogUtils.getLogger();
    public int id;
    public String text;
    public ResourceLocation character;
    public ResourceLocation background;
    public String sound;
    public int x;
    public int y;
    public int ix;
    public int iy;
    public float circle;
    public JsonObject render_execute;
    private int startX;
    private int startY;
    private int currentTime = 0;
    public TextMethods textMethods = new TextMethods();

    public TextEntry(String t,String c,String b,String s,int x,int y,int ix,int iy,JsonObject render_execute){
        this.text=t;
        if(!Objects.equals(b, "null")) this.background=ResourceLocation.fromNamespaceAndPath(Galmc_api.MODID,b);
        else this.background = ResourceLocation.fromNamespaceAndPath(Galmc_api.MODID,"texture/character/air.png");
        if(!Objects.equals(c, "null")) this.character=ResourceLocation.fromNamespaceAndPath(Galmc_api.MODID,c);
        else this.character = ResourceLocation.fromNamespaceAndPath(Galmc_api.MODID,"texture/character/air.png");
        this.sound = s;
        this.x=x;
        this.y=y;
        this.ix = ix;
        this.iy = iy;
        this.render_execute = render_execute;
    }
    int tick=0;
    public void render(GuiGraphics guiGraphics, GalScreen galScreen){
        try {
            if (Objects.equals(render_execute.get("type").getAsString(), "java")) {
                textMethods.execute(guiGraphics, render_execute.get("data").getAsString(), this, galScreen);
            } else if (Objects.equals(render_execute.get("type").getAsString(), "normal")) {
                int screenWidth = galScreen.width;
                int screenHeight = galScreen.height;
                //渲染背景
                galScreen.renderContain(guiGraphics, screenWidth, screenHeight, background);
                //渲染人物
                this.render_character(guiGraphics, galScreen);
                //渲染文本
                if (galScreen.rendtext) this.render_text(guiGraphics, galScreen);
            } else if (Objects.equals(render_execute.get("type").getAsString(), "pingyi")) {
                int screenWidth = galScreen.width;
                int screenHeight = galScreen.height;
                //渲染背景
                galScreen.renderContain(guiGraphics, screenWidth, screenHeight, background);
                //渲染人物
                this.render_character(guiGraphics, galScreen);
                //平移
                this.move(
                        render_execute.get("data").getAsJsonObject().get("new_x").getAsInt(),
                        render_execute.get("data").getAsJsonObject().get("new_y").getAsInt(),
                        render_execute.get("data").getAsJsonObject().get("time").getAsInt()
                );
                //渲染UI
                galScreen.renderUI(guiGraphics);
                //渲染文本
                if (galScreen.rendtext) this.render_text(guiGraphics, galScreen);


            } else if (Objects.equals(render_execute.get("type").getAsString(), "two_people")) {
                int screenWidth = galScreen.width;
                int screenHeight = galScreen.height;
                //渲染背景
                galScreen.renderContain(guiGraphics, screenWidth, screenHeight, background);
                //渲染人物
                this.render_character(guiGraphics, galScreen);
                //渲染第二人
                ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(Galmc_api.MODID,
                        render_execute.get("data").getAsJsonObject().get("image").getAsString());
                int n_x = render_execute.get("data").getAsJsonObject().get("x").getAsInt();
                int n_y = render_execute.get("data").getAsJsonObject().get("y").getAsInt();
                int n_ix = render_execute.get("data").getAsJsonObject().get("image_x").getAsInt();
                int n_iy = render_execute.get("data").getAsJsonObject().get("image_y").getAsInt();
                guiGraphics.blit(resourceLocation,
                        galScreen.getX(n_x,true), galScreen.getY(n_y,true), // 位置
                        0, 0,           // 纹理坐标
                        galScreen.getX(n_ix,false), galScreen.getY(n_iy,false),  // 尺寸
                        galScreen.getX(n_ix,false), galScreen.getY(n_iy,false)        // 纹理尺寸
                );
                //渲染UI
                galScreen.renderUI(guiGraphics);
                //渲染文本
                if (galScreen.rendtext) this.render_text(guiGraphics, galScreen);

            }
        }catch (NullPointerException e){
            LOGGER.error(text+":特殊渲染失败或者资源文件格式有误",e);
            int screenWidth = galScreen.width;
            int screenHeight = galScreen.height;
            //渲染背景
            galScreen.renderContain(guiGraphics, screenWidth, screenHeight, background);
            //渲染人物
            this.render_character(guiGraphics, galScreen);
            //渲染文本
            if (galScreen.rendtext) this.render_text(guiGraphics, galScreen);

        }
    }
    String rs = "";
    int p=0;
    int p1=30;
    private void render_text(GuiGraphics guiGraphics, GalScreen galScreen){
        if(!Objects.equals(text, "null")) {
            galScreen.renderUI(guiGraphics);
            String[] parts = text.replace("：", ":").split(":", 2);
            String a = "";
            String b = "";
            try {
                b = parts[1];
                if (p == galScreen.text_speed) {
                    if (rs.length() < parts[1].length()) rs = rs + parts[1].charAt(rs.length());
                    p = 0;

                }
                p++;
                a = parts[0];
            } catch (ArrayIndexOutOfBoundsException e) {
                rs = text;
                a = "";
            }
            if (galScreen.fonta != null) {
                // 渲染剧情文本
                guiGraphics.drawString(
                        galScreen.fonta,
                        Component.literal(rs),
                        galScreen.getX(120,true), galScreen.getY(900,true),
                        0xFFFFFF, // 白色
                        true     // 是否带阴影
                );
                if (Objects.equals(rs, b)) {
                    p1--;
                    if (galScreen.auto && p1 <= 0) galScreen.next();
                }
            } else {
                LOGGER.error("字体加载错误，请重试");
            }

            if (Objects.equals(a, "旁白")) {
                a = "";
            }
            guiGraphics.drawString(
                    galScreen.fonta,
                    Component.literal(a),
                    galScreen.getX(75,true), galScreen.getY(750,true),
                    0xFFFFFF,
                    true
            );
        }

    }
    private void render_character(GuiGraphics guiGraphics,GalScreen galScreen){
        guiGraphics.blit(
                this.character,
                galScreen.getX(x,true), galScreen.getY(y,true), // 位置
                0, 0,           // 纹理坐标
                galScreen.getX(ix,false), galScreen.getY(iy,false),  // 尺寸
                galScreen.getX(ix,false), galScreen.getY(iy,false)        // 纹理尺寸
        );
    }
    public boolean is_sound(){
        return !Objects.equals(this.sound, "null");
    }

    private void move(int nx, int ny, int time) {
        // 初始化或新目标
        if ((nx != x || ny != y)&&currentTime<=time) {
            startX = x;
            startY = y;
        }

        // 更新位置
        if (currentTime < time) {
            currentTime++;
            float progress = (float) currentTime / time;

            // 应用平滑曲线 (ease-out)
            float smoothProgress = 1 - (float)Math.pow(1 - progress, 2);

            this.x = (int) (startX + (nx - startX) * smoothProgress);
            this.y = (int) (startY + (ny - startY) * smoothProgress);
        }

    }

}
