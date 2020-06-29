package com.redhat.insights.kafka.connect.transforms;

import org.apache.kafka.connect.sink.SinkRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.*;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class FieldToJsonTest extends AbstractKeyValueTransformationTest {

    @Parameters(name="{0}")
    public static Collection<Object[]> data() {
        return buildParameters(FieldToJson.Key::new, FieldToJson.Value::new);
    }

    public FieldToJsonTest(String id, TestParams params) {
        super(params);
    }

    @Test
    public void testJsonSerializeValue() {
        final Map<String, String> props = Map.of("originalField", "tags", "destinationField", "tags_json");
        final Map<String, String> tag = new HashMap<>();
        tag.put("namespace", "insights");
        tag.put("key", "environment");
        tag.put("value", "prod");
        final List<Map<String, String>> tags = Arrays.asList(tag);
        final Map<String, Object> value = new HashMap<>();
        value.put("tags", tags);
        final SinkRecord record = new SinkRecord("test", 0, null, value, null, value, 0);

        transform.configure(props);
        final SinkRecord result = transform.apply(record);

        final Map<String, Object> transformedObject = Utils.cast(params.getObject.apply(result));
        final String transformed_tags = Utils.cast(transformedObject.get("tags_json"));

        assertEquals("[{\"namespace\":\"insights\",\"value\":\"prod\",\"key\":\"environment\"}]", transformed_tags);
    }
}
