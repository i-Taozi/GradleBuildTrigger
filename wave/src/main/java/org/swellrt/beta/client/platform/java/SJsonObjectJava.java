package org.swellrt.beta.client.platform.java;

import org.swellrt.beta.model.json.SJsonObject;

import com.google.gson.JsonObject;


public class SJsonObjectJava implements SJsonObject {

  private final JsonObject jso;

  public SJsonObjectJava() {
    this.jso = new JsonObject();
  }

  protected SJsonObjectJava(JsonObject jso) {
    this.jso = jso;
  }

  @Override
  public SJsonObject addInt(String name, int value) {
    jso.addProperty(name, value);
    return this;
  }

  @Override
  public SJsonObject addLong(String name, long value) {
    jso.addProperty(name, value);
    return this;
  }

  @Override
  public SJsonObject addBoolean(String name, boolean value) {
    jso.addProperty(name, value);
    return this;
  }

  @Override
  public SJsonObject addString(String name, String value) {
    jso.addProperty(name, value);
    return this;
  }

  @Override
  public SJsonObject addDouble(String name, Double value) {
    jso.addProperty(name, value);
    return this;
  }

  @Override
  public SJsonObject addObject(String name, SJsonObject value) {

    if (value instanceof SJsonObjectJava) {
      SJsonObjectJava joj = (SJsonObjectJava) value;
      jso.add(name, joj.jso);
    }

    return this;
  }

  @Override
  public Integer getInt(String name) {
    if (jso.has(name))
      return jso.get(name).getAsInt();
    else
      return null;
  }

  @Override
  public Long getLong(String name) {
    if (jso.has(name))
      return jso.get(name).getAsLong();
    else
      return null;
  }

  @Override
  public boolean getBoolean(String name) {
    return jso.get(name).getAsBoolean();
  }

  @Override
  public String getString(String name) {
    if (jso.has(name))
      return jso.get(name).getAsString();
    else
      return null;
  }

  @Override
  public SJsonObject getObject(String name) {
    JsonObject joo = jso.get(name).getAsJsonObject();
    return new SJsonObjectJava(joo);
  }

  @Override
  public boolean has(String name) {
    return jso.has(name);
  }

  protected JsonObject getJsonObject() {
    return jso;
  }

  @Override
  public Object getNative() {
    return jso;
  }

  @Override
  public Double getDouble(String name) {
    if (jso.has(name))
      return jso.get(name).getAsDouble();
    else
      return null;
  }

}
