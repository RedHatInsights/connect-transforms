package com.redhat.insights.kafka.connect.transforms;

import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.transforms.Transformation;

/**
 * A transformation that work either a record key or record value.
 *
 * The details of accessing and manipulating key/value are abstracted away.
 * Two imlementations (Key, Value) are provided as default methods.
 * This allows a new SMT to be written without duplicating the key/value specific code.
 */
interface KeyOrValueTransformation<T extends ConnectRecord<T>> extends Transformation<T> {

    Object getObject (T record);
    Schema getSchema (T record);

    T newRecord(T record, Object object);
    T newRecord(T record, Object object, Schema schema);

    interface Key<T extends ConnectRecord<T>> extends KeyOrValueTransformation<T> {

        @Override
        default Object getObject (T record) {
            return record.key();
        }

        @Override
        default Schema getSchema (T record) {
            return record.keySchema();
        }

        @Override
        default T newRecord(T record, Object object) {
            return this.newRecord(record, object, record.keySchema());
        }

        @Override
        default T newRecord(T record, Object object, Schema schema) {
            return record.newRecord(record.topic(), record.kafkaPartition(), schema, object, record.valueSchema(), record.value(), record.timestamp(), record.headers());
        }
    }

    interface Value<T extends ConnectRecord<T>> extends KeyOrValueTransformation<T> {

        @Override
        default Object getObject (T record) {
            return record.value();
        }

        @Override
        default Schema getSchema (T record) {
            return record.valueSchema();
        }

        @Override
        default T newRecord(T record, Object object) {
            return this.newRecord(record, object, record.valueSchema());
        }

        @Override
        default T newRecord(T record, Object object, Schema schema) {
            return record.newRecord(record.topic(), record.kafkaPartition(), record.keySchema(), record.key(), schema, object, record.timestamp(), record.headers());
        }
    }
}
