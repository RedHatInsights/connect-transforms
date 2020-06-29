package com.redhat.insights.kafka.connect.transforms;

import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.transforms.Transformation;
import org.apache.kafka.connect.transforms.util.SimpleConfig;

/**
 * Common functionality used by all Transformation implementations.
 */
abstract class AbstractTransformation<T extends ConnectRecord<T>> implements Transformation<T> {

    private final ConfigDef configDef;

    protected AbstractTransformation(ConfigDef configDef) {
        this.configDef = configDef;
    }

    protected abstract void configure(Map<String, ?> configs, AbstractConfig config);

    /**
     * Each transformation needs to parse the config using SimpleConfig.
     *
     * It is done here to reduce duplicate code.
     */
    @Override
    public void configure(Map<String, ?> configs) {
        final AbstractConfig config = new SimpleConfig(config(), configs);
        configure(configs, config);
    }

    @Override
    public ConfigDef config() {
        return configDef;
    }

    /**
     * Nothing to close by default. Override if needed.
     */
    @Override
    public void close() {
    }
}
