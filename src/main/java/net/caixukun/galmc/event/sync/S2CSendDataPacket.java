package net.caixukun.galmc.event.sync;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSendDataPacket {
    private final String response;
    private final long timestamp;

    public S2CSendDataPacket(String response, long timestamp) {
        this.response = response;
        this.timestamp = timestamp;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(response, 256);
        buf.writeLong(timestamp);
    }

    public static S2CSendDataPacket decode(FriendlyByteBuf buf) {
        String response = buf.readUtf(256);
        long timestamp = buf.readLong();
        return new S2CSendDataPacket(response, timestamp);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            // ⭐ 在客户端执行（注意：这里不能直接操作方块/实体，需通过任务队列）
            Minecraft minecraft = Minecraft.getInstance();


        });
        ctx.setPacketHandled(true);
    }
}
