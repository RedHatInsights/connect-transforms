# Kafka Connect Transforms

This project implements a set of generic Kafka Connect transformations that complement the [built-in transformations](https://docs.confluent.io/current/connect/transforms/index.html).

## DropIf

Sets either a message key (`com.redhat.insights.kafka.connect.transforms.DropIf$Key`) or message value (`com.redhat.insights.kafka.connect.transforms.DropIf$Value`) to `null` if the given predicate evaluates to `true`.

Unlike [Drop](https://docs.confluent.io/current/connect/transforms/drop.html#drop), this transform does not alter the key/value schema.
In cases where the schema does not allow for a key/value to be null the user of this transform is responsible for altering the schema.

This transform evaluates the predicate using an [ECMAScript ScriptEngine instance](https://docs.oracle.com/javase/8/docs/api/javax/script/ScriptEngine.html).
The message is made available to the expression under the `record` variable.
Use `record.key()`, `record.value()` and `record.headers()` to access key, value and headers, respectively.

If the predicate evaluation fails, or the predicate returns non-boolean result, this is considered an error.

### Configuration properties

|Name|Description|Type|Default|Valid values|Importance|
|---|---|---|---|---|---|
|`predicate`|The predicate to be evaluated on each message|string|-|Any valid ECMAScript expression that evaluates to a boolean|HIGH

### Examples

Assume the following configuration:

```json
"transforms": "dropValue",
"transforms.dropValue.type":"com.redhat.insights.kafka.connect.transforms.DropIf$Value",
"transforms.dropValue.predicate": "record.value().get('country') == 'CZ'"
```

Example 1

* Before: `{"country": "CZ"}`
* After: `null`

Example 2

* Before: `{"country": "SK"}`
* After: `{"country": "SK"}`

## FieldToJson

Converts the given original field on a value to a JSON representation.  This JSON representation is stored in the
 given destination field.

### Configuration properties

|Name|Description|Type|Default|Valid values|Importance|
|---|---|---|---|---|---|
|`originalField`|The field that will be serialized into JSON|string|-|Any field name containing serializable data|HIGH
|`destinationField`|The field that will contain the JSON data|string|-|Any valid field name|HIGH

### Example

Assume the following configuration:

```json
"transforms.tagsToJson.type": "com.redhat.insights.kafka.connect.transforms.FieldToJson$Value",
"transforms.tagsToJson.originalField": "orig",
"transforms.tagsToJson.destinationField": "dest"
```

* Before: `{orig=[{key=value}]}`
* After: `{orig=[{key=value}], dest="[{"key":"value"}]"}`

## Filter

Drops the entire record if the given predicate evaluates to `false`.

This transform evaluates the predicate using an [ECMAScript ScriptEngine instance](https://docs.oracle.com/javase/8/docs/api/javax/script/ScriptEngine.html).
The message is made available to the expression under the `record` variable.
Use `record.key()`, `record.value()` and `record.headers()` to access key, value and headers, respectively.

If the predicate evaluation fails, or the predicate returns non-boolean result, this is considered an error.

### Configuration properties

|Name|Description|Type|Default|Valid values|Importance|
|---|---|---|---|---|---|
|`predicate`|The predicate to be evaluated on each message|string|-|Any valid ECMAScript expression that evaluates to a boolean|HIGH

### Examples

Assume the following configuration:

```json
"transforms": "filter",
"transforms.filter.type":"com.redhat.insights.kafka.connect.transforms.Filter",
"transforms.filter.predicate": "record['value']['country'] === 'CZ'"
```

Example 1

* Before: `SinkRecord{value={country=SK}}`
* After: `null`

Example 2

* Before: `SinkRecord{value={country=CZ}}`
* After: `SinkRecord{value={country=CZ}}`

## FilterFields

Removes fields specified by the allowlist or denylist.

### Configuration properties

|Name|Description|Type|Default|Valid values|Importance|
|---|---|---|---|---|---|
|`field`|Name of the field to remove keys from|string|-|Any valid field name|HIGH
|`allowlist`|If set, keys not in this list will be removed from the field|list|-|Any valid field name|HIGH
|`denylist`|If set, keys in this list will be removed from the field|list|-|Any valid field name|HIGH

### Example

Assume the following configuration:

```json
"transforms.filterFields.type": "com.redhat.insights.kafka.connect.transforms.FilterFields$Value",
"transforms.filterFields.field": "data",
"transforms.filterFields.denylist": "key1, key2",
```

* Before: `{"data":{"key1":"value1", "key2":"value2", "key3":"value3"}}`
* After: `{"data":{"key3":"value3"}}`

## InjectSchema

Sets a key (`com.redhat.insights.kafka.connect.transforms.InjectSchema$Key`) or value (`com.redhat.insights.kafka.connect.transforms.InjectSchema$Value`) schema of a record.
Intended primarily to enrich schemaless messages but can also be used to override an existing schema on the record.

This transform uses `JsonConverter` to parse a Connect schema from it's JSON format.

### Configuration properties

|Name|Description|Type|Default|Valid values|Importance|
|---|---|---|---|---|---|
|`schema`|The schema to be used|string|-|Any valid Kafka Connect schema definition in the JSON format|HIGH

### Example

Assume the following configuration:

```json
"transforms": "injectSchema",
"transforms.injectSchema.type":"com.redhat.insights.kafka.connect.transforms.InjectSchema$Value",
"transforms.injectSchema.schema": "{\"type\":\"string\",\"optional\":false}"
```

* Before: `value: "foo"`, `schema: null`
* After: `value: "foo"`, `schema: Schema{STRING}`
