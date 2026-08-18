package net.caixukun.galmc.init;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.caixukun.galmc.resource.GalResourceManger;
import net.caixukun.galmc.event.OpenUIEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class CommandInit {
    /*
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // 在这里注册指令
        dispatcher.register(
                Commands.literal("play_galgame").then(Commands.argument("path", StringArgumentType.greedyString())
                        .suggests(RESOURCE_PATH_SUGGESTIONS)
                        .executes(context -> executeResourceCommand(context))
                )
        );
    }

     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("play_galgame")
                        .then(Commands.argument("player", EntityArgument.player())   // 新增玩家参数
                                .then(Commands.argument("path", StringArgumentType.greedyString())
                                        .suggests(RESOURCE_PATH_SUGGESTIONS)
                                        .executes(context -> {
                                            ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
                                            return executeResourceCommand(context,targetPlayer);
                                        })
                                )
                        )
        );
    }
    private static final SuggestionProvider<CommandSourceStack> RESOURCE_PATH_SUGGESTIONS =
            (context, builder) -> {
                List<String> data = GalResourceManger.getText();
                data.addAll(GalResourceManger.getCCg());
                return SharedSuggestionProvider.suggest(data, builder);
            };
    private static int executeResourceCommand(CommandContext<CommandSourceStack> context,ServerPlayer player){
        String resourcePath = StringArgumentType.getString(context, "path");
        /*
        CommandSourceStack source = context.getSource();
        if (source.getEntity() instanceof ServerPlayer player) {
            String newPath = resourcePath.replace("galmc_api:", "").replace("\"", "");
            OpenUIEvent.openUI(newPath,player.getUUID());
            return 1;
        } else {
            MinecraftServer server = source.getServer();

            // 获取所有在线玩家
            List<ServerPlayer> players = server.getPlayerList().getPlayers();

            // 如果是单人游戏（只有一个玩家在线）或玩家列表不为空
            if (!players.isEmpty() && players.size() == 1) {
                // 为第一个（也是唯一的）玩家执行打开UI操作
                ServerPlayer singlePlayer = players.get(0);

                String newPath = resourcePath.replace("galmc_api:", "").replace("\"", "");
                OpenUIEvent.openUI(newPath, singlePlayer.getUUID());
                return 1;
            } else {
                source.sendFailure(Component.literal("不能为多个玩家播放galgame"));
                return 0;
            }
        }

         */
        String newPath = resourcePath.replace("galmc_api:", "").replace("\"", "");
        OpenUIEvent.openUI(newPath,player.getUUID());
        return 1;

    }

}
