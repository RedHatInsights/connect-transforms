package com.redhat.insights.kafka.connect.transforms;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;

/**
 * Drops a message based on the given ECMAScript predicate.
 */
public class Filter<T extends ConnectRecord<T>> extends BooleanPredicateTransform<T> {

    public Filter(){
        super(new ConfigDef().define(CONFIG_FIELD, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH,
                "ECMAScript predicate to be evaluated for each message. If the predicate evaluates to true the message is dropped."));
    }

    @Override
    public T apply(T record) {
        if (!evalPredicate(record)) {
            return null;
        }

        return record;
    }
}
