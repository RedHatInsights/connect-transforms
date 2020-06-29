package com.redhat.insights.kafka.connect.transforms;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.errors.ConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transformation that converts a complex value into a JSON string.
 * Can be used as a workaround for https://github.com/confluentinc/kafka-connect-jdbc/issues/46
 */
public abstract class FieldToJson<T extends ConnectRecord<T>> extends AbstractTransformation<T> implements KeyOrValueTransformation<T>  {

    private static final Logger LOG = LoggerFactory.getLogger(FieldToJson.class);

    private static final String CONFIG_ORIGINAL = "originalField";
    private static final String CONFIG_DESTINATION = "destinationField";

    private volatile String originalField;
    private volatile String destinationField;

    public FieldToJson () {
        super(new ConfigDef()
            .define(CONFIG_ORIGINAL, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH,
                "Name of the source field whose value will be serialized to JSON")
            .define(CONFIG_DESTINATION, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH,
                "Name of the destination field whose value will be set to the serialized JSON")
        );
    }

    @Override
    public void configure(Map<String, ?> configs, AbstractConfig config) {
        this.originalField = config.getString(CONFIG_ORIGINAL);
        this.destinationField = config.getString(CONFIG_DESTINATION);
    }

    @Override
    public T apply(T record) {
        final Object transformObject = getObject(record);

        if (!(transformObject instanceof Map)) {
            return record;
        }

        final Map<String, Object> transformMap = Utils.cast(transformObject);

        if (!transformMap.containsKey(originalField)) {
            return record;
        }

        final ObjectMapper serializer = new ObjectMapper();

        try {
            final String json = serializer.writeValueAsString(transformMap.get(originalField));
            transformMap.put(destinationField, json);
        } catch (JsonProcessingException e) {
            LOG.error("Error transforming field {} to JSON. Record: {}", originalField, record, e);
            throw new ConnectException("Error serializing field to JSON", e);
        }

        return newRecord(record, transformMap);
    }

    public static class Key<T extends ConnectRecord<T>> extends FieldToJson<T> implements KeyOrValueTransformation.Key<T> {}
    public static class Value<T extends ConnectRecord<T>> extends FieldToJson<T> implements KeyOrValueTransformation.Value<T> {}
}
