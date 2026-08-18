package net.caixukun.galmc.ui.character;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.caixukun.galmc.Galmc_api;
import net.caixukun.galmc.event.ExecuteEvent;
import net.caixukun.your_wife.execute.ExecuteMethods;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class End_Execute {
    public String type;
    public List<String> data = new ArrayList<>();
    private ExecuteMethods executeMethods = new ExecuteMethods();
    public End_Execute(JsonObject jsonObject){
        try {
            this.type = jsonObject.get("type").getAsString();
            for (JsonElement product : jsonObject.getAsJsonArray("data")) {
                data.add(product.getAsString());
            }
        }catch (NullPointerException exception){
            type = "null";
        }
    }
    public boolean execute(UUID player){
        if(Objects.equals(type, "null")){
            return true;
        } else if (Objects.equals(type, "java")) {
            executeMethods.execute(player,data.get(0));
            return true;
        } else if (Objects.equals(type, "command")) {
            try {
                for (String s : data) {
                    ExecuteEvent.commands.add(new Pair<UUID, String>() {
                        @Override
                        public UUID getLeft() {
                            return player;
                        }

                        @Override
                        public String getRight() {
                            return s;
                        }

                        @Override
                        public String setValue(String value) {
                            return "";
                        }
                    });
                }
            }catch (RuntimeException e){
                Galmc_api.LOGGER.error("执行失败");
                Galmc_api.LOGGER.error(e.getMessage());
                return false;
            }
        }
        return false;
    }
}
