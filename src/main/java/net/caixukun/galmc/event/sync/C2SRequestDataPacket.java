package net.caixukun.galmc.event.sync;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class C2SRequestDataPacket {
    private final String message;  // 自定义数据字段
    private final int value;

    // 构造函数（发送时使用）
    public C2SRequestDataPacket(String message, int value) {
        this.message = message;
        this.value = value;
    }

    // 编码：将数据写入字节缓冲（发送方）
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(message, 256);  // 写入字符串（限制长度防溢出）
        buf.writeInt(value);
    }

    // 解码：从字节缓冲读取数据（接收方）
    public static C2SRequestDataPacket decode(FriendlyByteBuf buf) {
        String message = buf.readUtf(256);
        int value = buf.readInt();
        return new C2SRequestDataPacket(message, value);
    }

    // 处理逻辑（在服务端执行）
    public void handle(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            // ⭐ 在服务端执行（安全线程）
            ServerPlayer sender = ctx.getSender();  // 获取发送包的那个玩家
            /*
            if (sender != null) {
                System.out.println("[服务端] 收到来自 " + sender.getName().getString() +
                        " 的数据: " + message + ", " + value);

                // 示例：给玩家回传一条消息
                ModNetworking.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> sender),
                        new S2CSendDataPacket("服务端消息", System.currentTimeMillis())
                );
            }

             */
        });
        ctx.setPacketHandled(true);
    }
}