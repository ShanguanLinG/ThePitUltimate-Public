package cn.charlotte.pit.data.deserializer;

import cn.charlotte.pit.data.sub.PlayerWarehouse;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * @Author: Araykal
 * @Date: 2025/6/21
 */
public class WarehouseDeserializer extends JsonDeserializer<PlayerWarehouse> {

    @Override
    public PlayerWarehouse deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        try {
            JsonNode node = p.getCodec().readTree(p);
            String content = node.get("content").asText();
            return PlayerWarehouse.deserialization(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return new PlayerWarehouse();
    }
} 