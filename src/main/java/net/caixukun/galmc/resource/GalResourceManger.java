package net.caixukun.galmc.resource;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.caixukun.galmc.Galmc_api;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class GalResourceManger {
    public static List<String> text = new ArrayList<>();
    public static List<String> cg = new ArrayList<>();
    public static Path zipFile = null;
    public static String cg_background = null;
    public static final Logger LOGGER = LogUtils.getLogger();
    public static String TARGET_PACK_ID = null;
    public static String ROOT_PATH = null;


    public static void handleResourcePack() {

        try {
            // 源目录：.minecraft/config/yourmod/resourcepacks/
            Path srcDir = FMLPaths.CONFIGDIR.get().resolve(Galmc_api.MODID).resolve("resourcepacks");

            if (!Files.exists(srcDir)){
                Files.createDirectories(srcDir);
            }

            // 找到第一个zip文件
            try (Stream<Path> stream = Files.list(srcDir)) {
                Optional<Path> firstZip = stream.filter(p -> p.toString().endsWith(".zip")).findFirst();
                if (firstZip.isEmpty()) return ;

                Path zipPath = firstZip.get();
                // 目标目录：.minecraft/resourcepacks/
                Path targetDir = FMLPaths.GAMEDIR.get().resolve("resourcepacks");
                if (!Files.exists(targetDir)) Files.createDirectories(targetDir);
                Path targetPath = targetDir.resolve(zipPath.getFileName());
                GalResourceManger.TARGET_PACK_ID = Paths.get(targetPath.toString()).getFileName().toString();
                GalResourceManger.ROOT_PATH = zipPath.toString().substring(0, zipPath.toString().lastIndexOf(".zip"));
                unzip(zipPath, Path.of(ROOT_PATH));
                // 复制文件（覆盖已存在）
                Files.copy(zipPath, targetPath, StandardCopyOption.REPLACE_EXISTING);

                GalResourceManger.zipFile = zipPath;

            }
        } catch (Exception e) {
            LogManager.getLogger().error("Failed to handle resource pack", e);
        }
    }
    public static void unzip(Path zipFile, Path destDir) throws IOException {
        // 确保目标目录存在
        Files.createDirectories(destDir);

        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zipFile)))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                // 防止 Zip Slip 攻击
                Path targetPath = destDir.resolve(entry.getName()).normalize();
                if (!targetPath.startsWith(destDir.normalize())) {
                    throw new IOException("非法 ZIP 条目: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(targetPath);
                } else {
                    // 确保父目录存在
                    Files.createDirectories(targetPath.getParent());
                    // 复制文件内容
                    Files.copy(zis, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
    }
    public static File get_sound(String id){
        String[] parts = id.split("\\.");
        String path = ROOT_PATH+"\\assets\\galmc_api\\sounds";
        for(String i : parts){
            path = path+"\\"+i;
        }
        path = path + ".ogg";
        return new File(path);
    }

    public static List<String> getText(){
        if(GalResourceManger.text.isEmpty()){
            try {
                try (ZipFile zipFile = new ZipFile(GalResourceManger.zipFile.toFile())) {
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        String name = entry.getName();
                        // 匹配 assets/<namespace>/sounds.json
                        if (name.startsWith("assets/galmc_api/data/text/") && name.endsWith(".json")) {
                            name = name.replace("assets/galmc_api/", "");
                            GalResourceManger.text.add(name);
                        }
                    }
                } catch (IOException e) {
                    LogManager.getLogger().error("Failed to handle resource pack", e);
                }
            }catch (NullPointerException e){
                LogManager.getLogger().warn("没有资源包加载", e);
            }
        }
        return GalResourceManger.text;
    }
    public static List<String> getCCg(){
        if(GalResourceManger.cg.isEmpty()){
            try {
                try (ZipFile zipFile = new ZipFile(GalResourceManger.zipFile.toFile())) {
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        String name = entry.getName();
                        // 匹配 assets/<namespace>/sounds.json
                        if (name.startsWith("assets/galmc_api/data/cg/") && name.endsWith(".json")) {
                            name = name.replace("assets/galmc_api/", "");
                            GalResourceManger.cg.add(name);
                        }
                    }
                } catch (Exception e) {
                    LogManager.getLogger().error("Failed to handle resource pack", e);
                }
            }catch (NullPointerException e){
                LogManager.getLogger().warn("没有资源包加载", e);
            }
        }
        return GalResourceManger.cg;
    }
    public static String getCgUI(){
        if(GalResourceManger.cg_background==null){
            try {
                try (ZipFile zipFile = new ZipFile(GalResourceManger.zipFile.toFile())) {
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        String name = entry.getName();
                        // 匹配 assets/<namespace>/sounds.json
                        if (name.startsWith("assets/galmc_api/texture/gui/") && name.endsWith("cg_background.png")) {
                            name = name.replace("assets/galmc_api/", "");
                            GalResourceManger.cg_background = name;
                        }
                    }
                } catch (Exception e) {
                    LogManager.getLogger().error("Failed to handle resource pack", e);
                }
            }catch (NullPointerException e){
                LogManager.getLogger().warn("没有资源包加载", e);
            }
        }
        return GalResourceManger.cg_background;
    }
}
