package com.redhat.insights.kafka.connect.transforms;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.transforms.Transformation;
import org.junit.After;
import org.junit.Before;

public class AbstractKeyValueTransformationTest {

    protected final TestParams params;
    protected Transformation<SinkRecord> transform;

    protected static Collection<Object[]> buildParameters (
        Supplier<Transformation<SinkRecord>> keySupplier,
        Supplier<Transformation<SinkRecord>> valueSupplier) {
        return Arrays.asList(new Object[][] {
            {
                "key",
                new TestParams(keySupplier, SinkRecord::key, SinkRecord::keySchema)
            }, {
                "value",
                new TestParams(valueSupplier, SinkRecord::value, SinkRecord::valueSchema)
            }
        });
    }

    public AbstractKeyValueTransformationTest(TestParams params) {
        this.params = params;
    }

    @Before
    public void setUp () {
        this.transform = params.transformSupplier.get();
    }

    @After
    public void tearDown () {
        this.transform.close();
    }
}
