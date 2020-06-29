package com.redhat.insights.kafka.connect.transforms;

import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.transforms.Transformation;

class TestParams {
    final Supplier<Transformation<SinkRecord>> transformSupplier;
    final Function<SinkRecord, Object> getObject;
    final Function<SinkRecord, Schema> getSchema;

    TestParams(
        Supplier<Transformation<SinkRecord>> transformSupplier,
        Function<SinkRecord, Object> getObject,
        Function<SinkRecord, Schema> getSchema) {
        this.transformSupplier = transformSupplier;
        this.getObject = getObject;
        this.getSchema = getSchema;
    }
}
