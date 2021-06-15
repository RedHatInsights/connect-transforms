package com.redhat.insights.kafka.connect.transforms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.Map;

import org.apache.kafka.common.record.TimestampType;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.header.ConnectHeaders;
import org.apache.kafka.connect.header.Headers;
import org.apache.kafka.connect.sink.SinkRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DropIfTest {

    private DropIf<SinkRecord> transformKey;
    private DropIf<SinkRecord> transformValue;

    @Before
    public void setUp () {
        this.transformKey = new DropIf.Key<>();
        this.transformValue = new DropIf.Value<>();
    }

    @After
    public void tearDown () {
        this.transformKey.close();
        this.transformValue.close();
    }

    @Test
    public void testValuePositive() {
        final Map<String, String> props = Collections.singletonMap("if", "record.value().get('number') == 1");
        final SinkRecord record = new SinkRecord("test", 0, null, null, null, Collections.singletonMap("number", 1), 0);

        transformValue.configure(props);
        final SinkRecord result = transformValue.apply(record);

        assertNull(result.value());
    }

    @Test
    public void testValueNegative() {
        final Map<String, String> props = Collections.singletonMap("if", "record.value().get('number') == 1");
        final Map<String, Object> value = Collections.singletonMap("number", 2);
        final SinkRecord record = new SinkRecord("test", 0, null, null, null, value, 0);

        transformValue.configure(props);
        final SinkRecord result = transformValue.apply(record);

        assertNotNull(result);
        assertEquals(result.value(), value);
    }

    @Test
    public void testValueBasedOnHeaderPositive() {
        final Map<String, String> props = Collections.singletonMap("if", "record.headers().lastWithName('request_id').value() == '1'");
        final Headers headers = new ConnectHeaders().add("request_id", "1", null);
        final SinkRecord record = new SinkRecord("test", 0, null, null, null, "value", 0, null, TimestampType.NO_TIMESTAMP_TYPE, headers);

        transformValue.configure(props);
        final SinkRecord result = transformValue.apply(record);

        assertNull(result.value());
    }

    @Test
    public void testKeyPositive() {
        final Map<String, String> props = Collections.singletonMap("if", "record.key() == 'abc'");
        final SinkRecord record = new SinkRecord("test", 0, null, "abc", null, "value", 0);

        transformKey.configure(props);
        final SinkRecord result = transformKey.apply(record);

        assertNull(result.key());
    }

    @Test
    public void testKeyNegative() {
        final Map<String, String> props = Collections.singletonMap("if", "record.key() == 'abc'");
        final String key = "def";
        final SinkRecord record = new SinkRecord("test", 0, null, key, null, "value", 0);

        transformKey.configure(props);
        final SinkRecord result = transformKey.apply(record);

        assertNotNull(result);
        assertEquals(result.key(), key);
    }

    @Test(expected = ConnectException.class)
    public void testExceptionThrownOnInvalidScript() {
        final Map<String, String> props = Collections.singletonMap("if", "record.key(");
        final SinkRecord record = new SinkRecord("test", 0, null, "key", null, "value", 0);

        transformKey.configure(props);
        transformKey.apply(record);
    }

    @Test
    public void testDefaultPredicate() {
        final Map<String, String> props = Collections.emptyMap();
        final Map<String, Object> value = Collections.singletonMap("number", 2);
        final SinkRecord record = new SinkRecord("test", 0, null, null, null, value, 0);

        transformValue.configure(props);
        final SinkRecord result = transformValue.apply(record);

        assertNotNull(result);
        assertEquals(result.value(), null);
    }

    @Test
    public void testLegacyProperty() {
        final Map<String, String> props = Collections.singletonMap("predicate", "record.value().get('number') == 1");
        final Map<String, Object> value = Collections.singletonMap("number", 2);
        final SinkRecord record = new SinkRecord("test", 0, null, null, null, value, 0);

        transformValue.configure(props);
        final SinkRecord result = transformValue.apply(record);

        assertNotNull(result);
        assertEquals(result.value(), value);
    }

    @Test
    public void testPredicatePriority() {
        final Map<String, String> props = Map.of("predicate", "record.value().get('number') == 2", "if", "record.value().get('number') == 1");
        final Map<String, Object> value = Collections.singletonMap("number", 2);
        final SinkRecord record = new SinkRecord("test", 0, null, null, null, value, 0);

        transformValue.configure(props);
        final SinkRecord result = transformValue.apply(record);

        assertNotNull(result);
        assertEquals(result.value(), value);
    }
}
