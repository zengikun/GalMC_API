package net.caixukun.galmc.event.sync;// ModNetworking.java


import net.caixukun.galmc.Galmc_api;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;


public class ModNetworking {
    private static final String PROTOCOL_VERSION = "1.0.0";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(Galmc_api.MODID,"main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void registerPackets() {
        CHANNEL.registerMessage(packetId++,
                C2SRequestDataPacket.class,
                C2SRequestDataPacket::encode,
                C2SRequestDataPacket::decode,
                C2SRequestDataPacket::handle
        );

        CHANNEL.registerMessage(packetId++,
                S2CSendDataPacket.class,
                S2CSendDataPacket::encode,
                S2CSendDataPacket::decode,
                S2CSendDataPacket::handle
        );
    }

    // ========== 便捷工具方法（可选） ==========

    // 发送给指定玩家
    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    // 发送给所有玩家
    public static <MSG> void sendToAll(MSG message) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), message);
    }

    // 发送给维度内所有玩家
    public static <MSG> void sendToDimension(MSG message, Level level) {
        CHANNEL.send(PacketDistributor.DIMENSION.with(() -> level.dimension()), message);
    }
}