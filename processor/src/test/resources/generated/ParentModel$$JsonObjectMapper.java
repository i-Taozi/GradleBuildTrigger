package com.bluelinelabs.logansquare.processor;

import com.bluelinelabs.logansquare.JsonMapper;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.IOException;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;

@SuppressWarnings("unsafe,unchecked")
public final class ParentModel$$JsonObjectMapper extends JsonMapper<ParentModel> {
    @Override
    public ParentModel parse(JsonParser jsonParser) throws IOException {
        ParentModel instance = new ParentModel();
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
    public void parseField(ParentModel instance, String fieldName, JsonParser jsonParser) throws IOException {
        if ("parentTestInt".equals(fieldName)) {
            instance.parentTestInt = jsonParser.getValueAsInt();
        }
    }

    @Override
    public void serialize(ParentModel object, JsonGenerator jsonGenerator, boolean writeStartAndEnd) throws IOException {
        if (writeStartAndEnd) {
            jsonGenerator.writeStartObject();
        }
        jsonGenerator.writeNumberField("parentTestInt", object.parentTestInt);
        if (writeStartAndEnd) {
            jsonGenerator.writeEndObject();
        }
    }
}
