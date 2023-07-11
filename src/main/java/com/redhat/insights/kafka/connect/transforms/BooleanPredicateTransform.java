package com.redhat.insights.kafka.connect.transforms;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.errors.ConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import org.openjdk.nashorn.api.scripting.*;

abstract class BooleanPredicateTransform<T extends ConnectRecord<T>> extends AbstractTransformation<T> {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    protected static final String CONFIG_FIELD_LEGACY = "predicate";
    protected static final String CONFIG_FIELD = "if";
    private volatile String predicate;
    private volatile ScriptEngine engine;

    public BooleanPredicateTransform(ConfigDef configDef) {
        super(configDef);
    }

    @Override
    public void configure(Map<String, ?> configs, AbstractConfig config) {
        this.predicate = config.getString(CONFIG_FIELD);

        if (this.predicate == null) {
            this.predicate = config.getString(CONFIG_FIELD_LEGACY);
        }

        final NashornScriptEngineFactory manager = new NashornScriptEngineFactory();
        this.engine = manager.getScriptEngine();
    }

    protected boolean evalPredicate(T record) {
        if (this.predicate == null) {
            return true;
        }

        final Bindings bindings = new SimpleBindings();
        bindings.put("record", record);

        try {
            final Object result = engine.eval(this.predicate, bindings);
            if (result instanceof Boolean) {
                return (Boolean) result;
            }

            LOG.error("Predicate did not resolve to boolean. Got {} instead. Predicate: {}, Record: {}", result, this.predicate, record);
            throw new ConnectException("Predicate did not resolve to boolean");
        } catch (ScriptException e) {
            LOG.error("Error evaluating predicate. Record: {}", record, e);
            throw new ConnectException("Error evaluating predicate", e);
        }
    }
}
