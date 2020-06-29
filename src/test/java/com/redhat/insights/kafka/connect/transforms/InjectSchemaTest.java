package com.redhat.insights.kafka.connect.transforms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.sink.SinkRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class InjectSchemaTest extends AbstractKeyValueTransformationTest {

    @Parameters(name="{0}")
    public static Collection<Object[]> data() {
        return buildParameters(InjectSchema.Key::new, InjectSchema.Value::new);
    }

    public InjectSchemaTest(String id, TestParams params) {
        super(params);
    }

    @Test
    public void testStringSchema () {
        final Map<String, String> props = Collections.singletonMap("schema", "{\"type\":\"string\",\"optional\":false}");
        final SinkRecord record = new SinkRecord("test", 0, null, "hello", null, "hello", 0);

        transform.configure(props);
        final SinkRecord result = transform.apply(record);

        final Schema schema = params.getSchema.apply(result);
        assertNotNull(schema);
        assertEquals(Schema.Type.STRING, schema.type());
        assertNull(schema.name());
    }

    @Test
    public void testStructSchema () {
        final Map<String, String> props = Collections.singletonMap("schema", "{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false, \"field\": \"location\"}, {\"type\":\"int32\",\"optional\":false, \"field\": \"temperature\"}]}");
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("location", "LKTB");
        data.put("temperature", 15);

        final SinkRecord record = new SinkRecord("test", 0, null, data, null, data, 0);

        transform.configure(props);
        final SinkRecord result = transform.apply(record);

        final Schema schema = params.getSchema.apply(result);
        assertNotNull(schema);
        assertEquals(Schema.Type.STRUCT, schema.type());
        assertNull(schema.name());

        final Field locationField = schema.field("location");
        assertNotNull(locationField);
        assertEquals("location", locationField.name());
        assertEquals(Schema.Type.STRING, locationField.schema().type());

        final Field temperatureField = schema.field("temperature");
        assertNotNull(temperatureField);
        assertEquals("temperature", temperatureField.name());
        assertEquals(Schema.Type.INT32, temperatureField.schema().type());

        final Object object = params.getObject.apply(result);
        assertTrue(object instanceof Struct);
        final Struct struct = Utils.cast(object);
        assertEquals("LKTB", struct.get("location"));
        assertEquals(15, struct.get("temperature"));
    }
}
