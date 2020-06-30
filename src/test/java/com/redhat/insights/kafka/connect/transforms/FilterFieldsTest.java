package com.redhat.insights.kafka.connect.transforms;

import org.apache.kafka.connect.sink.SinkRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class FilterFieldsTest extends AbstractKeyValueTransformationTest {

    @Parameters(name="{0}")
    public static Collection<Object[]> data() {
        return buildParameters(FilterFields.Key::new, FilterFields.Value::new);
    }

    public FilterFieldsTest(String id, TestParams params) {
        super(params);
    }

    @Test
    public void testFilterFieldsDenylist() {
        final List<String> fieldsToRemove = Arrays.asList("key_to_remove", "key_to_remove2");
        final Map<String, Object> props = Map.of("field", "field", "denylist", fieldsToRemove);

        final Map<String, Object> field = new HashMap<>();
        field.put("key_to_keep", "");
        field.put("key_to_remove", "");
        field.put("key_to_remove2", "");

        final Map<String, Object> value = new HashMap<>();
        value.put("field", field);

        final SinkRecord record = new SinkRecord("test", 0, null, value, null, value, 0);

        transform.configure(props);
        final SinkRecord result = transform.apply(record);

        final Map<String, Object> transformedObject = Utils.cast(params.getObject.apply(result));
        final Map<String, Object> modifiedField = Utils.cast(transformedObject.get("field"));

        assertTrue(modifiedField.containsKey("key_to_keep"));
        assertFalse(modifiedField.containsKey("key_to_remove"));
        assertFalse(modifiedField.containsKey("key_to_remove2"));
        assertEquals(1, modifiedField.size());
    }

    @Test
    public void testFilterFieldsAllowlist() {
        final List<String> fieldsToKeep = Arrays.asList("key_to_keep", "key_to_keep2");
        final Map<String, Object> props = Map.of("field", "field", "allowlist", fieldsToKeep);

        final Map<String, Object> field = new HashMap<>();
        field.put("key_to_keep", "");
        field.put("key_to_keep2", "");
        field.put("key_to_remove", "");

        final Map<String, Object> value = new HashMap<>();
        value.put("field", field);

        final SinkRecord record = new SinkRecord("test", 0, null, value, null, value, 0);

        transform.configure(props);
        final SinkRecord result = transform.apply(record);

        final Map<String, Object> transformedObject = Utils.cast(params.getObject.apply(result));
        final Map<String, Object> modifiedField = Utils.cast(transformedObject.get("field"));

        assertTrue(modifiedField.containsKey("key_to_keep"));
        assertTrue(modifiedField.containsKey("key_to_keep2"));
        assertFalse(modifiedField.containsKey("key_to_remove"));
        assertEquals(2, modifiedField.size());
    }
}
