package com.redhat.insights.kafka.connect.transforms;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;

/**
 * Drops a message key or value based on the given ECMAScript predicate.
 */
public abstract class DropIf<T extends ConnectRecord<T>> extends BooleanPredicateTransform<T> implements KeyOrValueTransformation<T>  {

    public DropIf(){
        super(new ConfigDef().define(CONFIG_FIELD, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH,
                        "ECMAScript predicate to be evaluated for each message. If the predicate evaluates to true the message key/value is dropped."));

    }

    @Override
    public T apply(T record) {
        if (evalPredicate(record)) {
            return newRecord(record, null, null);
        }

        return record;
    }

    public static class Key<T extends ConnectRecord<T>> extends DropIf<T> implements KeyOrValueTransformation.Key<T> {}
    public static class Value<T extends ConnectRecord<T>> extends DropIf<T> implements KeyOrValueTransformation.Value<T> {}
}
