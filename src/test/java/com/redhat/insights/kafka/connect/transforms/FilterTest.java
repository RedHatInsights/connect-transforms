package com.redhat.insights.kafka.connect.transforms;

import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.sink.SinkRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

public class FilterTest {

    private Filter<SinkRecord> transform;

    @Before
    public void setUp() {
        this.transform = new Filter<>();
    }

    @After
    public void tearDown() {
        this.transform.close();
    }

    @Test
    public void testPredicateMatch() {
        final Map<String, String> props = Collections.singletonMap("predicate", "!!record.value().get('requiredFact')");
        Map<String, Integer> value = Collections.singletonMap("requiredFact", 1);
        final SinkRecord record = new SinkRecord("test", 0, null, null, null, value, 0);

        transform.configure(props);
        final SinkRecord result = transform.apply(record);

        assertNotNull(result);
        assertEquals(result.value(), value);
    }

    @Test
    public void testNegative() {
        final Map<String, String> props = Collections.singletonMap("predicate", "!!record.value().get('requiredFact')");
        final SinkRecord record = new SinkRecord("test", 0, null, null, null, Collections.singletonMap("otherFact", 1), 0);

        transform.configure(props);
        final SinkRecord result = transform.apply(record);

        assertNull(result);
    }


    @Test(expected = ConnectException.class)
    public void testExceptionThrownOnInvalidScript() {
        final Map<String, String> props = Collections.singletonMap("predicate", "record.key(");
        final SinkRecord record = new SinkRecord("test", 0, null, "key", null, "value", 0);

        transform.configure(props);
        transform.apply(record);
    }
}
