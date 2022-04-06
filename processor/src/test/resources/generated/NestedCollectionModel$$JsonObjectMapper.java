package model.good;

import com.bluelinelabs.logansquare.JsonMapper;
import com.bluelinelabs.logansquare.LoganSquare;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.IOException;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

@SuppressWarnings("unsafe,unchecked")
public final class NestedCollectionModel$$JsonObjectMapper extends JsonMapper<NestedCollectionModel> {
    private static final JsonMapper<NestedCollectionModel.ModelForCollection> MODEL_GOOD_NESTEDCOLLECTIONMODEL_MODELFORCOLLECTION__JSONOBJECTMAPPER = LoganSquare.mapperFor(NestedCollectionModel.ModelForCollection.class);

    @Override
    public NestedCollectionModel parse(JsonParser jsonParser) throws IOException {
        NestedCollectionModel instance = new NestedCollectionModel();
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
    public void parseField(NestedCollectionModel instance, String fieldName, JsonParser jsonParser) throws IOException {
        if ("arrayList".equals(fieldName)) {
            if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                ArrayList<String[]> collection1 = new ArrayList<String[]>();
                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    String[] value1;
                    if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                        List<String> collection2 = new ArrayList<String>();
                        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                            String value2;
                            value2 = jsonParser.getValueAsString(null);
                            collection2.add(value2);
                        }
                        String[] array = collection2.toArray(new String[collection2.size()]);
                        value1 = array;
                    } else{
                        value1 = null;
                    }
                    collection1.add(value1);
                }
                instance.arrayList = collection1;
            } else{
                instance.arrayList = null;
            }
        } else if ("complexArray".equals(fieldName)){
            if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                ArrayList<List<Map<String, NestedCollectionModel.ModelForCollection>>[]> collection1 = new ArrayList<List<Map<String, NestedCollectionModel.ModelForCollection>>[]>();
                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    ArrayList[] value1;
                    if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                        List<List<Map<String, NestedCollectionModel.ModelForCollection>>> collection2 = new ArrayList<List<Map<String, NestedCollectionModel.ModelForCollection>>>();
                        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                            ArrayList value2;
                            if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                                ArrayList<Map<String, NestedCollectionModel.ModelForCollection>> collection3 = new ArrayList<Map<String, NestedCollectionModel.ModelForCollection>>();
                                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                                    HashMap value3;
                                    if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
                                        HashMap<String, NestedCollectionModel.ModelForCollection> map4 = new HashMap<String, NestedCollectionModel.ModelForCollection>();
                                        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                                            String key4 = jsonParser.getText();
                                            jsonParser.nextToken();
                                            if (jsonParser.getCurrentToken() == JsonToken.VALUE_NULL) {
                                                map4.put(key4, null);
                                            } else{
                                                map4.put(key4, MODEL_GOOD_NESTEDCOLLECTIONMODEL_MODELFORCOLLECTION__JSONOBJECTMAPPER.parse(jsonParser));                                            }
                                        }
                                        value3 = map4;
                                    } else{
                                        value3 = null;
                                    }
                                    collection3.add(value3);
                                }
                                value2 = collection3;
                            } else{
                                value2 = null;
                            }
                            collection2.add(value2);
                        }
                        ArrayList[] array = collection2.toArray(new ArrayList[collection2.size()]);
                        value1 = array;
                    } else{
                        value1 = null;
                    }
                    collection1.add(value1);
                }
                instance.complexArray = collection1;
            } else{
                instance.complexArray = null;
            }
        } else if ("complexerArray".equals(fieldName)){
            if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                ArrayList<List<Map<String, NestedCollectionModel.ModelForCollection[]>>[]> collection1 = new ArrayList<List<Map<String, NestedCollectionModel.ModelForCollection[]>>[]>();
                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    ArrayList[] value1;
                    if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                        List<List<Map<String, NestedCollectionModel.ModelForCollection[]>>> collection2 = new ArrayList<List<Map<String, NestedCollectionModel.ModelForCollection[]>>>();
                        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                            ArrayList value2;
                            if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                                ArrayList<Map<String, NestedCollectionModel.ModelForCollection[]>> collection3 = new ArrayList<Map<String, NestedCollectionModel.ModelForCollection[]>>();
                                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                                    HashMap value3;
                                    if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
                                        HashMap<String, NestedCollectionModel.ModelForCollection[]> map4 = new HashMap<String, NestedCollectionModel.ModelForCollection[]>();
                                        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                                            String key4 = jsonParser.getText();
                                            jsonParser.nextToken();
                                            if (jsonParser.getCurrentToken() == JsonToken.VALUE_NULL) {
                                                map4.put(key4, null);
                                            } else{
                                                if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                                                    List<NestedCollectionModel.ModelForCollection> collection5 = new ArrayList<NestedCollectionModel.ModelForCollection>();
                                                    while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                                                        NestedCollectionModel.ModelForCollection value5;
                                                        value5 = MODEL_GOOD_NESTEDCOLLECTIONMODEL_MODELFORCOLLECTION__JSONOBJECTMAPPER.parse(jsonParser);
                                                        collection5.add(value5);
                                                    }
                                                    NestedCollectionModel.ModelForCollection[] array = collection5.toArray(new NestedCollectionModel.ModelForCollection[collection5.size()]);
                                                    map4.put(key4, array);
                                                } else{
                                                    map4.put(key4, null);
                                                }
                                            }
                                        }
                                        value3 = map4;
                                    } else{
                                        value3 = null;
                                    }
                                    collection3.add(value3);
                                }
                                value2 = collection3;
                            } else{
                                value2 = null;
                            }
                            collection2.add(value2);
                        }
                        ArrayList[] array = collection2.toArray(new ArrayList[collection2.size()]);
                        value1 = array;
                    } else{
                        value1 = null;
                    }
                    collection1.add(value1);
                }
                instance.complexerArray = collection1;
            } else{
                instance.complexerArray = null;
            }
        } else if ("dontKnowWhatImDoingList".equals(fieldName)){
            if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                ArrayList<List<List<ArrayList<NestedCollectionModel.ModelForCollection>>>> collection1 = new ArrayList<List<List<ArrayList<NestedCollectionModel.ModelForCollection>>>>();
                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    ArrayList value1;
                    if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                        ArrayList<List<ArrayList<NestedCollectionModel.ModelForCollection>>> collection2 = new ArrayList<List<ArrayList<NestedCollectionModel.ModelForCollection>>>();
                        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                            ArrayList value2;
                            if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                                ArrayList<ArrayList<NestedCollectionModel.ModelForCollection>> collection3 = new ArrayList<ArrayList<NestedCollectionModel.ModelForCollection>>();
                                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                                    ArrayList value3;
                                    if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                                        ArrayList<NestedCollectionModel.ModelForCollection> collection4 = new ArrayList<NestedCollectionModel.ModelForCollection>();
                                        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                                            NestedCollectionModel.ModelForCollection value4;
                                            value4 = MODEL_GOOD_NESTEDCOLLECTIONMODEL_MODELFORCOLLECTION__JSONOBJECTMAPPER.parse(jsonParser);
                                            collection4.add(value4);
                                        }
                                        value3 = collection4;
                                    } else{
                                        value3 = null;
                                    }
                                    collection3.add(value3);
                                }
                                value2 = collection3;
                            } else{
                                value2 = null;
                            }
                            collection2.add(value2);
                        }
                        value1 = collection2;
                    } else{
                        value1 = null;
                    }
                    collection1.add(value1);
                }
                instance.dontKnowWhatImDoingList = collection1;
            } else{
                instance.dontKnowWhatImDoingList = null;
            }
        } else if ("dontKnowWhatImDoingMap".equals(fieldName)){
            if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
                HashMap<String, Map<String, ArrayList<List<Map<String, NestedCollectionModel.ModelForCollection>>>>> map1 = new HashMap<String, Map<String, ArrayList<List<Map<String, NestedCollectionModel.ModelForCollection>>>>>();
                while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                    String key1 = jsonParser.getText();
                    jsonParser.nextToken();
                    if (jsonParser.getCurrentToken() == JsonToken.VALUE_NULL) {
                        map1.put(key1, null);
                    } else{
                        if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
                            HashMap<String, ArrayList<List<Map<String, NestedCollectionModel.ModelForCollection>>>> map2 = new HashMap<String, ArrayList<List<Map<String, NestedCollectionModel.ModelForCollection>>>>();
                            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                                String key2 = jsonParser.getText();
                                jsonParser.nextToken();
                                if (jsonParser.getCurrentToken() == JsonToken.VALUE_NULL) {
                                    map2.put(key2, null);
                                } else{
                                    if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                                        ArrayList<List<Map<String, NestedCollectionModel.ModelForCollection>>> collection3 = new ArrayList<List<Map<String, NestedCollectionModel.ModelForCollection>>>();
                                        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                                            ArrayList value3;
                                            if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                                                ArrayList<Map<String, NestedCollectionModel.ModelForCollection>> collection4 = new ArrayList<Map<String, NestedCollectionModel.ModelForCollection>>();
                                                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                                                    HashMap value4;
                                                    if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
                                                        HashMap<String, NestedCollectionModel.ModelForCollection> map5 = new HashMap<String, NestedCollectionModel.ModelForCollection>();
                                                        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                                                            String key5 = jsonParser.getText();
                                                            jsonParser.nextToken();
                                                            if (jsonParser.getCurrentToken() == JsonToken.VALUE_NULL) {
                                                                map5.put(key5, null);
                                                            } else{
                                                                map5.put(key5, MODEL_GOOD_NESTEDCOLLECTIONMODEL_MODELFORCOLLECTION__JSONOBJECTMAPPER.parse(jsonParser));
                                                            }
                                                        }
                                                        value4 = map5;
                                                    } else{
                                                        value4 = null;
                                                    }
                                                    collection4.add(value4);
                                                }
                                                value3 = collection4;
                                            } else{
                                                value3 = null;
                                            }
                                            collection3.add(value3);
                                        }
                                        map2.put(key2, collection3);
                                    } else{
                                        map2.put(key2, null);
                                    }
                                }
                            }
                            map1.put(key1, map2);
                        } else{
                            map1.put(key1, null);
                        }
                    }
                }
                instance.dontKnowWhatImDoingMap = map1;
            } else{
                instance.dontKnowWhatImDoingMap = null;
            }
        } else if ("model_deque".equals(fieldName)){
            if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                ArrayDeque<List<NestedCollectionModel.ModelForCollection>> collection1 = new ArrayDeque<List<NestedCollectionModel.ModelForCollection>>();
                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    ArrayList value1;
                    if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                        ArrayList<NestedCollectionModel.ModelForCollection> collection2 = new ArrayList<NestedCollectionModel.ModelForCollection>();
                        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                            NestedCollectionModel.ModelForCollection value2;
                            value2 = MODEL_GOOD_NESTEDCOLLECTIONMODEL_MODELFORCOLLECTION__JSONOBJECTMAPPER.parse(jsonParser);
                            collection2.add(value2);
                        }
                        value1 = collection2;
                    } else{
                        value1 = null;
                    }
                    collection1.add(value1);
                }
                instance.modelForCollectionDeque = collection1;
            } else{
                instance.modelForCollectionDeque = null;
            }
        } else if ("model_list".equals(fieldName)){
            if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                ArrayList<List<NestedCollectionModel.ModelForCollection>> collection1 = new ArrayList<List<NestedCollectionModel.ModelForCollection>>();
                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    ArrayList value1;
                    if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                        ArrayList<NestedCollectionModel.ModelForCollection> collection2 = new ArrayList<NestedCollectionModel.ModelForCollection>();
                        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                            NestedCollectionModel.ModelForCollection value2;
                            value2 = MODEL_GOOD_NESTEDCOLLECTIONMODEL_MODELFORCOLLECTION__JSONOBJECTMAPPER.parse(jsonParser);
                            collection2.add(value2);
                        }
                        value1 = collection2;
                    } else{
                        value1 = null;
                    }
                    collection1.add(value1);
                }
                instance.modelForCollectionList = collection1;
            } else{
                instance.modelForCollectionList = null;
            }
        } else if ("model_map".equals(fieldName)){
            if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
                HashMap<String, List<NestedCollectionModel.ModelForCollection>> map1 = new HashMap<String, List<NestedCollectionModel.ModelForCollection>>();
                while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                    String key1 = jsonParser.getText();
                    jsonParser.nextToken();
                    if (jsonParser.getCurrentToken() == JsonToken.VALUE_NULL) {
                        map1.put(key1, null);
                    } else{
                        if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                            ArrayList<NestedCollectionModel.ModelForCollection> collection2 = new ArrayList<NestedCollectionModel.ModelForCollection>();
                            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                                NestedCollectionModel.ModelForCollection value2;
                                value2 = MODEL_GOOD_NESTEDCOLLECTIONMODEL_MODELFORCOLLECTION__JSONOBJECTMAPPER.parse(jsonParser);
                                collection2.add(value2);
                            }
                            map1.put(key1, collection2);
                        } else{
                            map1.put(key1, null);
                        }
                    }
                }
                instance.modelForCollectionMap = map1;
            } else{
                instance.modelForCollectionMap = null;
            }
        } else if ("model_queue".equals(fieldName)){
            if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                ArrayDeque<List<NestedCollectionModel.ModelForCollection>> collection1 = new ArrayDeque<List<NestedCollectionModel.ModelForCollection>>();
                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    ArrayList value1;
                    if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                        ArrayList<NestedCollectionModel.ModelForCollection> collection2 = new ArrayList<NestedCollectionModel.ModelForCollection>();
                        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                            NestedCollectionModel.ModelForCollection value2;
                            value2 = MODEL_GOOD_NESTEDCOLLECTIONMODEL_MODELFORCOLLECTION__JSONOBJECTMAPPER.parse(jsonParser);
                            collection2.add(value2);
                        }
                        value1 = collection2;
                    } else{
                        value1 = null;
                    }
                    collection1.add(value1);
                }
                instance.modelForCollectionQueue = collection1;
            } else{
                instance.modelForCollectionQueue = null;
            }
        } else if ("model_set".equals(fieldName)){
            if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                HashSet<List<NestedCollectionModel.ModelForCollection>> collection1 = new HashSet<List<NestedCollectionModel.ModelForCollection>>();
                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    ArrayList value1;
                    if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                        ArrayList<NestedCollectionModel.ModelForCollection> collection2 = new ArrayList<NestedCollectionModel.ModelForCollection>();
                        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                            NestedCollectionModel.ModelForCollection value2;
                            value2 = MODEL_GOOD_NESTEDCOLLECTIONMODEL_MODELFORCOLLECTION__JSONOBJECTMAPPER.parse(jsonParser);
                            collection2.add(value2);
                        }
                        value1 = collection2;
                    } else{
                        value1 = null;
                    }
                    collection1.add(value1);
                }
                instance.modelForCollectionSet = collection1;
            } else{
                instance.modelForCollectionSet = null;
            }
        }
    }

    @Override
    public void serialize(NestedCollectionModel object, JsonGenerator jsonGenerator, boolean writeStartAndEnd) throws IOException {
        if (writeStartAndEnd) {
            jsonGenerator.writeStartObject();
        }
        final List<String[]> lslocalarrayList = object.arrayList;
        if (lslocalarrayList != null) {
            jsonGenerator.writeFieldName("arrayList");
            jsonGenerator.writeStartArray();
            for (String[] element1 : lslocalarrayList) {
                if (element1 != null) {
                    final String[] lslocallslocalarrayListElement = element1;
                    if (lslocallslocalarrayListElement != null) {
                        jsonGenerator.writeStartArray();
                        for (String element2 : lslocallslocalarrayListElement) {
                            if (element2 != null) {
                                jsonGenerator.writeString(element2);
                            }
                        }
                        jsonGenerator.writeEndArray();
                    }
                }
            }
            jsonGenerator.writeEndArray();
        }
        final List<List<Map<String, NestedCollectionModel.ModelForCollection>>[]> lslocalcomplexArray = object.complexArray;
        if (lslocalcomplexArray != null) {
            jsonGenerator.writeFieldName("complexArray");
            jsonGenerator.writeStartArray();
            for (List<Map<String, NestedCollectionModel.ModelForCollection>>[] element1 : lslocalcomplexArray) {
                if (element1 != null) {
                    final List<Map<String, NestedCollectionModel.ModelForCollection>>[] lslocallslocalcomplexArrayElement = element1;
                    if (lslocallslocalcomplexArrayElement != null) {
                        jsonGenerator.writeStartArray();
                        for (List<Map<String, NestedCollectionModel.ModelForCollection>> element2 : lslocallslocalcomplexArrayElement) {
                            if (element2 != null) {
                                final List<Map<String, NestedCollectionModel.ModelForCollection>> lslocallslocallslocalcomplexArrayElementElement = element2;
                                if (lslocallslocallslocalcomplexArrayElementElement != null) {
                                    jsonGenerator.writeStartArray();
                                    for (Map<String, NestedCollectionModel.ModelForCollection> element3 : lslocallslocallslocalcomplexArrayElementElement) {
                                        if (element3 != null) {
                                            final Map<String, NestedCollectionModel.ModelForCollection> lslocallslocallslocallslocalcomplexArrayElementElementElement = element3;
                                            if (lslocallslocallslocallslocalcomplexArrayElementElementElement != null) {
                                                jsonGenerator.writeStartObject();
                                                for (Map.Entry<String, NestedCollectionModel.ModelForCollection> entry4 : lslocallslocallslocallslocalcomplexArrayElementElementElement.entrySet()) {
                                                    jsonGenerator.writeFieldName(entry4.getKey().toString());
                                                    if (entry4.getValue() != null) {
                                                        MODEL_GOOD_NESTEDCOLLECTIONMODEL_MODELFORCOLLECTION__JSONOBJECTMAPPER.serialize(entry4.getValue(), jsonGenerator, true);
                                                    }
                                                }
                                                jsonGenerator.writeEndObject();
                                            }
                                        }
                                    }
                                    jsonGenerator.writeEndArray();
                                }
                            }
                        }
                        jsonGenerator.writeEndArray();
                    }
                }
            }
            jsonGenerator.writeEndArray();
        }
        final List<List<Map<String, NestedCollectionModel.ModelForCollection[]>>[]> lslocalcomplexerArray = object.complexerArray;
        if (lslocalcomplexerArray != null) {
            jsonGenerator.writeFieldName("complexerArray");
            jsonGenerator.writeStartArray();
            for (List<Map<String, NestedCollectionModel.ModelForCollection[]>>[] element1 : lslocalcomplexerArray) {
                if (element1 != null) {
                    final List<Map<String, NestedCollectionModel.ModelForCollection[]>>[] lslocallslocalcomplexerArrayElement = element1;
                    if (lslocallslocalcomplexerArrayElement != null) {
                        jsonGenerator.writeStartArray();
                        for (List<Map<String, NestedCollectionModel.ModelForCollection[]>> element2 : lslocallslocalcomplexerArrayElement) {
                            if (element2 != null) {
                                final List<Map<String, NestedCollectionModel.ModelForCollection[]>> lslocallslocallslocalcomplexerArrayElementElement = element2;
                                if (lslocallslocallslocalcomplexerArrayElementElement != null) {
                                    jsonGenerator.writeStartArray();
                                    for (Map<String, NestedCollectionModel.ModelForCollection[]> element3 : lslocallslocallslocalcomplexerArrayElementElement) {
                                        if (element3 != null) {
                                            final Map<String, NestedCollectionModel.ModelForCollection[]> lslocallslocallslocallslocalcomplexerArrayElementElementElement = element3;
                                            if (lslocallslocallslocallslocalcomplexerArrayElementElementElement != null) {
                                                jsonGenerator.writeStartObject();
                                                for (Map.Entry<String, NestedCollectionModel.ModelForCollection[]> entry4 : lslocallslocallslocallslocalcomplexerArrayElementElementElement.entrySet()) {
                                                    jsonGenerator.writeFieldName(entry4.getKey().toString());
                                                    if (entry4.getValue() != null) {
                                                        final NestedCollectionModel.ModelForCollection[] lslocallslocallslocallslocallslocalcomplexerArrayElementElementElementElement = entry4.getValue();
                                                        if (lslocallslocallslocallslocallslocalcomplexerArrayElementElementElementElement != null) {
                                                            jsonGenerator.writeStartArray();
                                                            for (NestedCollectionModel.ModelForCollection element5 : lslocallslocallslocallslocallslocalcomplexerArrayElementElementElementElement) {
                                                                if (element5 != null) {
                                                                    MODEL_GOOD_NESTEDCOLLECTIONMODEL_MODELFORCOLLECTION__JSONOBJECTMAPPER.serialize(element5, jsonGenerator, true);
                                                                }
                                                            }
                                                            jsonGenerator.writeEndArray();
                                                        }
                                                    }
                                                }
                                                jsonGenerator.writeEndObject();
                                            }
                                        }
                                    }
                                    jsonGenerator.writeEndArray();
                                }
                            }
                        }
                        jsonGenerator.writeEndArray();
                    }
                }
            }
            jsonGenerator.writeEndArray();
        }
        final List<List<List<ArrayList<NestedCollectionModel.ModelForCollection>>>> lslocaldontKnowWhatImDoingList = object.dontKnowWhatImDoingList;
        if (lslocaldontKnowWhatImDoingList != null) {
            jsonGenerator.writeFieldName("dontKnowWhatImDoingList");
            jsonGenerator.writeStartArray();
            for (List<List<ArrayList<NestedCollectionModel.ModelForCollection>>> element1 : lslocaldontKnowWhatImDoingList) {
                if (element1 != null) {
                    final List<List<ArrayList<NestedCollectionModel.ModelForCollection>>> lslocallslocaldontKnowWhatImDoingListElement = element1;
                    if (lslocallslocaldontKnowWhatImDoingListElement != null) {
                        jsonGenerator.writeStartArray();
                        for (List<ArrayList<NestedCollectionModel.ModelForCollection>> element2 : lslocallslocaldontKnowWhatImDoingListElement) {
                            if (element2 != null) {
                                final List<ArrayList<NestedCollectionModel.ModelForCollection>> lslocallslocallslocaldontKnowWhatImDoingListElementElement = element2;
                                if (lslocallslocallslocaldontKnowWhatImDoingListElementElement != null) {
                                    jsonGenerator.writeStartArray();
                                    for (ArrayList<NestedCollectionModel.ModelForCollection> element3 : lslocallslocallslocaldontKnowWhatImDoingListElementElement) {
                                        if (element3 != null) {
                                            final List<NestedCollectionModel.ModelForCollection> lslocallslocallslocallslocaldontKnowWhatImDoingListElementElementElement = element3;
                                            if (lslocallslocallslocallslocaldontKnowWhatImDoingListElementElementElement != null) {
                                                jsonGenerator.writeStartArray();
                                                for (NestedCollectionModel.ModelForCollection element4 : lslocallslocallslocallslocaldontKnowWhatImDoingListElementElementElement) {
                                                    if (element4 != null) {
                                                        MODEL_GOOD_NESTEDCOLLECTIONMODEL_MODELFORCOLLECTION__JSONOBJECTMAPPER.serialize(element4, jsonGenerator, true);
                                                    }
                                                }
                                                jsonGenerator.writeEndArray();
                                            }
                                        }
                                    }
                                    jsonGenerator.writeEndArray();
                                }
                            }
                        }
                        jsonGenerator.writeEndArray();
                    }
                }
            }
            jsonGenerator.writeEndArray();
        }
        final Map<String, Map<String, ArrayList<List<Map<String, NestedCollectionModel.ModelForCollection>>>>> lslocaldontKnowWhatImDoingMap = object.dontKnowWhatImDoingMap;
        if (lslocaldontKnowWhatImDoingMap != null) {
            jsonGenerator.writeFieldName("dontKnowWhatImDoingMap");
            jsonGenerator.writeStartObject();
            for (Map.Entry<String, Map<String, ArrayList<List<Map<String, NestedCollectionModel.ModelForCollection>>>>> entry1 : lslocaldontKnowWhatImDoingMap.entrySet()) {
                jsonGenerator.writeFieldName(entry1.getKey().toString());
                if (entry1.getValue() != null) {
                    final Map<String, ArrayList<List<Map<String, NestedCollectionModel.ModelForCollection>>>> lslocallslocaldontKnowWhatImDoingMapElement = entry1.getValue();
                    if (lslocallslocaldontKnowWhatImDoingMapElement != null) {
                        jsonGenerator.writeStartObject();
                        for (Map.Entry<String, ArrayList<List<Map<String, NestedCollectionModel.ModelForCollection>>>> entry2 : lslocallslocaldontKnowWhatImDoingMapElement.entrySet()) {
                            jsonGenerator.writeFieldName(entry2.getKey().toString());
                            if (entry2.getValue() != null) {
                                final List<List<Map<String, NestedCollectionModel.ModelForCollection>>> lslocallslocallslocaldontKnowWhatImDoingMapElementElement = entry2.getValue();
                                if (lslocallslocallslocaldontKnowWhatImDoingMapElementElement != null) {
                                    jsonGenerator.writeStartArray();
                                    for (List<Map<String, NestedCollectionModel.ModelForCollection>> element3 : lslocallslocallslocaldontKnowWhatImDoingMapElementElement) {
                                        if (element3 != null) {
                                            final List<Map<String, NestedCollectionModel.ModelForCollection>> lslocallslocallslocallslocaldontKnowWhatImDoingMapElementElementElement = element3;
                                            if (lslocallslocallslocallslocaldontKnowWhatImDoingMapElementElementElement != null) {
                                                jsonGenerator.writeStartArray();
                                                for (Map<String, NestedCollectionModel.ModelForCollection> element4 : lslocallslocallslocallslocaldontKnowWhatImDoingMapElementElementElement) {
                                                    if (element4 != null) {
                                                        final Map<String, NestedCollectionModel.ModelForCollection> lslocallslocallslocallslocallslocaldontKnowWhatImDoingMapElementElementElementElement = element4;
                                                        if (lslocallslocallslocallslocallslocaldontKnowWhatImDoingMapElementElementElementElement != null) {
                                                            jsonGenerator.writeStartObject();
                                                            for (Map.Entry<String, NestedCollectionModel.ModelForCollection> entry5 : lslocallslocallslocallslocallslocaldontKnowWhatImDoingMapElementElementElementElement.entrySet()) {
                                                                jsonGenerator.writeFieldName(entry5.getKey().toString());
                                                                if (entry5.getValue() != null) {
                                                                    MODEL_GOOD_NESTEDCOLLECTIONMODEL_MODELFORCOLLECTION__JSONOBJECTMAPPER.serialize(entry5.getValue(), jsonGenerator, true);
                                                                }
                                                            }
                                                            jsonGenerator.writeEndObject();
                                                        }
                                                    }
                                                }
                                                jsonGenerator.writeEndArray();
                                            }
                                        }
                                    }
                                    jsonGenerator.writeEndArray();
                                }
                            }
                        }
                        jsonGenerator.writeEndObject();
                    }
                }
            }
            jsonGenerator.writeEndObject();
        }
        final Queue<List<NestedCollectionModel.ModelForCollection>> lslocalmodel_deque = object.modelForCollectionDeque;
        if (lslocalmodel_deque != null) {
            jsonGenerator.writeFieldName("model_deque");
            jsonGenerator.writeStartArray();
            for (List<NestedCollectionModel.ModelForCollection> element1 : lslocalmodel_deque) {
                if (element1 != null) {
                    final List<NestedCollectionModel.ModelForCollection> lslocallslocalmodel_dequeElement = element1;
                    if (lslocallslocalmodel_dequeElement != null) {
                        jsonGenerator.writeStartArray();
                        for (NestedCollectionModel.ModelForCollection element2 : lslocallslocalmodel_dequeElement) {
                            if (element2 != null) {
                                MODEL_GOOD_NESTEDCOLLECTIONMODEL_MODELFORCOLLECTION__JSONOBJECTMAPPER.serialize(element2, jsonGenerator, true);
                            }
                        }
                        jsonGenerator.writeEndArray();
                    }
                }
            }
            jsonGenerator.writeEndArray();
        }
        final List<List<NestedCollectionModel.ModelForCollection>> lslocalmodel_list = object.modelForCollectionList;
        if (lslocalmodel_list != null) {
            jsonGenerator.writeFieldName("model_list");
            jsonGenerator.writeStartArray();
            for (List<NestedCollectionModel.ModelForCollection> element1 : lslocalmodel_list) {
                if (element1 != null) {
                    final List<NestedCollectionModel.ModelForCollection> lslocallslocalmodel_listElement = element1;
                    if (lslocallslocalmodel_listElement != null) {
                        jsonGenerator.writeStartArray();
                        for (NestedCollectionModel.ModelForCollection element2 : lslocallslocalmodel_listElement) {
                            if (element2 != null) {
                                MODEL_GOOD_NESTEDCOLLECTIONMODEL_MODELFORCOLLECTION__JSONOBJECTMAPPER.serialize(element2, jsonGenerator, true);
                            }
                        }
                        jsonGenerator.writeEndArray();
                    }
                }
            }
            jsonGenerator.writeEndArray();
        }
        final Map<String, List<NestedCollectionModel.ModelForCollection>> lslocalmodel_map = object.modelForCollectionMap;
        if (lslocalmodel_map != null) {
            jsonGenerator.writeFieldName("model_map");
            jsonGenerator.writeStartObject();
            for (Map.Entry<String, List<NestedCollectionModel.ModelForCollection>> entry1 : lslocalmodel_map.entrySet()) {
                jsonGenerator.writeFieldName(entry1.getKey().toString());
                if (entry1.getValue() != null) {
                    final List<NestedCollectionModel.ModelForCollection> lslocallslocalmodel_mapElement = entry1.getValue();
                    if (lslocallslocalmodel_mapElement != null) {
                        jsonGenerator.writeStartArray();
                        for (NestedCollectionModel.ModelForCollection element2 : lslocallslocalmodel_mapElement) {
                            if (element2 != null) {
                                MODEL_GOOD_NESTEDCOLLECTIONMODEL_MODELFORCOLLECTION__JSONOBJECTMAPPER.serialize(element2, jsonGenerator, true);
                            }
                        }
                        jsonGenerator.writeEndArray();
                    }
                }
            }
            jsonGenerator.writeEndObject();
        }
        final Queue<List<NestedCollectionModel.ModelForCollection>> lslocalmodel_queue = object.modelForCollectionQueue;
        if (lslocalmodel_queue != null) {
            jsonGenerator.writeFieldName("model_queue");
            jsonGenerator.writeStartArray();
            for (List<NestedCollectionModel.ModelForCollection> element1 : lslocalmodel_queue) {
                if (element1 != null) {
                    final List<NestedCollectionModel.ModelForCollection> lslocallslocalmodel_queueElement = element1;
                    if (lslocallslocalmodel_queueElement != null) {
                        jsonGenerator.writeStartArray();
                        for (NestedCollectionModel.ModelForCollection element2 : lslocallslocalmodel_queueElement) {
                            if (element2 != null) {
                                MODEL_GOOD_NESTEDCOLLECTIONMODEL_MODELFORCOLLECTION__JSONOBJECTMAPPER.serialize(element2, jsonGenerator, true);
                            }
                        }
                        jsonGenerator.writeEndArray();
                    }
                }
            }
            jsonGenerator.writeEndArray();
        }
        final Set<List<NestedCollectionModel.ModelForCollection>> lslocalmodel_set = object.modelForCollectionSet;
        if (lslocalmodel_set != null) {
            jsonGenerator.writeFieldName("model_set");
            jsonGenerator.writeStartArray();
            for (List<NestedCollectionModel.ModelForCollection> element1 : lslocalmodel_set) {
                if (element1 != null) {
                    final List<NestedCollectionModel.ModelForCollection> lslocallslocalmodel_setElement = element1;
                    if (lslocallslocalmodel_setElement != null) {
                        jsonGenerator.writeStartArray();
                        for (NestedCollectionModel.ModelForCollection element2 : lslocallslocalmodel_setElement) {
                            if (element2 != null) {
                                MODEL_GOOD_NESTEDCOLLECTIONMODEL_MODELFORCOLLECTION__JSONOBJECTMAPPER.serialize(element2, jsonGenerator, true);
                            }
                        }
                        jsonGenerator.writeEndArray();
                    }
                }
            }
            jsonGenerator.writeEndArray();
        }
        if (writeStartAndEnd) {
            jsonGenerator.writeEndObject();
        }
    }
}
