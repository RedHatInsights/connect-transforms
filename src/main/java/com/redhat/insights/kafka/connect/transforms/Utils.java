package com.redhat.insights.kafka.connect.transforms;

import java.util.Map;

import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;

final class Utils {

    private Utils () {}

    /**
     * Creates a new Struct based on the given schema. The values are populated using the given map
     * @param map the map with field values
     * @param schema the schema used by the Struct
     * @return a Struct based on the given schema populated with values from the given map
     */
    public static Struct mapToStruct (Map<String, Object> map, Schema schema) {
        final Struct struct = new Struct(schema);
        for (Field field : schema.fields()) {
            struct.put(field.name(), map.get(field.name()));
        }

        return struct;
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast (Object value) {
        return (T) value;
    }
}
