package cn.charlotte.pit.data.serializer;

import cn.charlotte.pit.data.sub.PlayerWarehouse;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * @Author: Araykal
 * @Date: 2025/6/21
 */
public class WarehouseSerializer extends JsonSerializer<PlayerWarehouse> {

    @Override
    public void serialize(PlayerWarehouse value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        try {
            String content = value.serialize();
            
            gen.writeStartObject();
            gen.writeStringField("content", content);
            gen.writeEndObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 