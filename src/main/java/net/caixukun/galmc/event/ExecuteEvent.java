package net.caixukun.galmc.event;

import net.caixukun.galmc.Galmc_api;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class ExecuteEvent {
    public static ArrayList<Pair<UUID,String>> commands = new ArrayList<>();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // 1. 确保只在服务端执行
        if (event.side == LogicalSide.CLIENT) {
            return;
        }

        // 2. 如果你只希望逻辑发生在 tick 的结束阶段（通常是 END）
        //   阶段分为 START 和 END，大部分逻辑放在 END 阶段执行
        if (event.phase == TickEvent.Phase.END) {


            // 你的代码逻辑
            for (Pair<UUID,String> s :commands){
                if(s.getLeft() == event.player.getUUID()) {
                    CommandSourceStack source = event.player.createCommandSourceStack()
                            .withPosition(event.player.position())
                            .withRotation(event.player.getRotationVector());
                    try {
                        event.player.getServer().getCommands().performPrefixedCommand(source, s.getRight());
                    } catch (NullPointerException e) {
                        Galmc_api.LOGGER.error(e.getMessage());
                    }
                }
            }
            commands.clear();
        }
    }
}
