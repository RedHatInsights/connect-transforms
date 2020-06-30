package com.redhat.insights.kafka.connect.transforms;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class FilterFields<T extends ConnectRecord<T>> extends AbstractTransformation<T> implements KeyOrValueTransformation<T> {
    private static final Logger LOG = LoggerFactory.getLogger(FilterFields.class);

    private static final String CONFIG_FIELD = "field";
    private static final String CONFIG_DENYLIST = "denylist";
    private static final String CONFIG_ALLOWLIST = "allowlist";

    private volatile List<String> denylist;
    private volatile List<String> allowlist;
    private String fieldName;

    public FilterFields () {
        super(new ConfigDef()
            .define(CONFIG_FIELD, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH,
                "Name of the field whose keys will be filtered")
            .define(CONFIG_DENYLIST, ConfigDef.Type.LIST, Collections.emptyList(), ConfigDef.Importance.HIGH,
                "Keys that will be dropped from the field")
            .define(CONFIG_ALLOWLIST, ConfigDef.Type.LIST, Collections.emptyList(), ConfigDef.Importance.HIGH,
                "Keys that will be kept in the field")
        );
    }

    @Override
    public void configure(Map<String, ?> configs, AbstractConfig config) {
        this.fieldName = config.getString(CONFIG_FIELD);
        this.denylist = config.getList(CONFIG_DENYLIST);
        this.allowlist = config.getList(CONFIG_ALLOWLIST);
    }

    @Override
    public T apply(T record) {
        final Object transformObject = getObject(record);

        if (!(transformObject instanceof Map)) {
            LOG.info("The object is not instance of map. Object is: {}", transformObject);
            return record;
        }

        final Map<String, Object> transformMap = Utils.cast(transformObject);

        if (!transformMap.containsKey(fieldName)) {
            LOG.info("Field {} not found. Skipping filter.", fieldName);
            return record;
        }

        final Map<String, Object> field = Utils.cast(transformMap.get(fieldName));

        field.keySet().removeAll(denylist);
        if (!allowlist.isEmpty()) {
            field.keySet().retainAll(allowlist);
        }

        transformMap.put(fieldName, field); //Avoids DU-anomaly PMD violation.
        return newRecord(record, transformMap);
    }

    public static class Key<T extends ConnectRecord<T>> extends FilterFields<T> implements KeyOrValueTransformation.Key<T> {}
    public static class Value<T extends ConnectRecord<T>> extends FilterFields<T> implements KeyOrValueTransformation.Value<T> {}
}
