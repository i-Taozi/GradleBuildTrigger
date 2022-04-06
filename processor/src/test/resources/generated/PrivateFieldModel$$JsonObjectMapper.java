package com.bluelinelabs.logansquare.processor;

import com.bluelinelabs.logansquare.JsonMapper;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.IOException;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unsafe,unchecked")
public final class PrivateFieldModel$$JsonObjectMapper extends JsonMapper<PrivateFieldModel> {
    @Override
    public PrivateFieldModel parse(JsonParser jsonParser) throws IOException {
        PrivateFieldModel instance = new PrivateFieldModel();
        if (jsonParser.getCurrentToken() == null) {
            jsonParser.nextToken();
        }
        if (jsonParser.getCurrentToken() != JsonToken.START_OBJECT) {
            jsonParser.skipChildren();
            return null;
        }
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jsonParser.getCurrentName();
            jsonParser.nextToken();
            parseField(instance, fieldName, jsonParser);
            jsonParser.skipChildren();
        }
        return instance;
    }

    @Override
    public void parseField(PrivateFieldModel instance, String fieldName, JsonParser jsonParser) throws IOException {
        if ("string_to_test_m_vars".equals(fieldName)) {
            instance.setStringThatStartsWithM(jsonParser.getValueAsString(null));
        } else if ("privateBoolean".equals(fieldName)){
            instance.setPrivateBoolean(jsonParser.getValueAsBoolean());
        } else if ("privateList".equals(fieldName)){
            if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                ArrayList<String> collection1 = new ArrayList<String>();
                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    String value1;
                    value1 = jsonParser.getValueAsString(null);
                    collection1.add(value1);
                }
                instance.setPrivateList(collection1);
            } else {
                instance.setPrivateList(null);
            }
        } else if ("privateMap".equals(fieldName)){
            if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
                HashMap<String, String> map1 = new HashMap<String, String>();
                while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                    String key1 = jsonParser.getText();
                    jsonParser.nextToken();
                    if (jsonParser.getCurrentToken() == JsonToken.VALUE_NULL) {
                        map1.put(key1, null);
                    } else{
                        map1.put(key1, jsonParser.getValueAsString(null));
                    }
                }
                instance.setPrivateMap(map1);
            } else {
                instance.setPrivateMap(null);
            }
        } else if ("private_named_string".equals(fieldName)){
            instance.setPrivateNamedString(jsonParser.getValueAsString(null));
        } else if ("privateString".equals(fieldName)){
            instance.setPrivateString(jsonParser.getValueAsString(null));
        }
    }

    @Override
    public void serialize(PrivateFieldModel object, JsonGenerator jsonGenerator, boolean writeStartAndEnd) throws IOException {
        if (writeStartAndEnd) {
            jsonGenerator.writeStartObject();
        }
        if (object.getStringThatStartsWithM() != null) {
            jsonGenerator.writeStringField("string_to_test_m_vars", object.getStringThatStartsWithM());
        }
        jsonGenerator.writeBooleanField("privateBoolean", object.isPrivateBoolean());
        final List<String> lslocalprivateList = object.getPrivateList();
        if (lslocalprivateList != null) {
            jsonGenerator.writeFieldName("privateList");
            jsonGenerator.writeStartArray();
            for (String element1 : lslocalprivateList) {
                if (element1 != null) {
                    jsonGenerator.writeString(element1);
                }
            }
            jsonGenerator.writeEndArray();
        }
        final Map<String, String> lslocalprivateMap = object.getPrivateMap();
        if (lslocalprivateMap != null) {
            jsonGenerator.writeFieldName("privateMap");
            jsonGenerator.writeStartObject();
            for (Map.Entry<String, String> entry1 : lslocalprivateMap.entrySet()) {
                jsonGenerator.writeFieldName(entry1.getKey().toString());
                if (entry1.getValue() != null) {
                    jsonGenerator.writeString(entry1.getValue());
                }
            }
            jsonGenerator.writeEndObject();
        }
        if (object.getPrivateNamedString() != null) {
            jsonGenerator.writeStringField("private_named_string", object.getPrivateNamedString());
        }
        if (object.getPrivateString() != null) {
            jsonGenerator.writeStringField("privateString", object.getPrivateString());
        }
        if (writeStartAndEnd) {
            jsonGenerator.writeEndObject();
        }
    }
}