package org.swellrt.beta.model;

import org.swellrt.beta.client.wave.WaveDeps;
import org.swellrt.beta.common.SException;
import org.swellrt.beta.model.json.SJsonObject;
import org.swellrt.beta.model.wave.mutable.SWaveNode;
import org.waveprotocol.wave.model.util.Preconditions;
import org.waveprotocol.wave.model.wave.ParticipantId;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;


@JsType(namespace = "swell", name = "Primitive")
public class SPrimitive extends SWaveNode {

  private static final String PRIMITIVE_SEPARATOR = ":";
  private static final String PRIMITIVE_STRING_TYPE_PREFIX  = "s";
  private static final String PRIMITIVE_BOOLEAN_TYPE_PREFIX  = "b";
  private static final String PRIMITIVE_INTEGER_TYPE_PREFIX  = "i";
  private static final String PRIMITIVE_DOUBLE_TYPE_PREFIX  = "d";
  private static final String PRIMITIVE_JSO_TYPE_PREFIX  = "js";

  private static final int TYPE_INT = 1;
  private static final int TYPE_DOUBLE = 2;
  private static final int TYPE_STRING = 3;
  private static final int TYPE_BOOL = 4;
  private static final int TYPE_JSO = 5;

  private final int type;
  private final int intValue;
  private final double doubleValue;
  private final String stringValue;
  private final Boolean boolValue;
  private final Object jsoValue;

  private final SNodeAccessControl accessControl;

  /**
   * Json primitive objects can be referenced inside a primitive object.
   */
  private SPrimitive container;
  private String containerPath;
  private String valuePath;

  /**
   * the key associated with this value in its parent container
   * if it is a map.
   */
  private String nameKey = null;

  @JsIgnore
  public static String asString(SNode node) {
    try{
      if (node != null && node instanceof SPrimitive) {
        return (String) ((SPrimitive) node).getValue();
      }
    } catch (ClassCastException e)
    {

    }
    return null;
  }

  @JsIgnore
  public static Double asDouble(SNode node) {
    try{
      if (node != null && node instanceof SPrimitive) {
        return (double) ((SPrimitive) node).getValue();
      }
    } catch (ClassCastException e)
    {

    }
    return null;
  }

  @JsIgnore
  public static Integer asInt(SNode node) {
    try{
      if (node != null && node instanceof SPrimitive) {
        return (int) ((SPrimitive) node).getValue();
      }
    } catch (ClassCastException e)
    {

    }
    return null;
  }

  @JsIgnore
  public static Boolean asBoolean(SNode node) {
    try{
      if (node != null && node instanceof SPrimitive) {
        return (boolean) ((SPrimitive) node).getValue();
      }
    } catch (ClassCastException e)
    {

    }
    return null;
  }



  /**
   * Deserialize a SPrimitive value
   * <p>
   * @param s the serialized representation of the primitive value.
   * @return the primitive value or null if is not a valid serialized string
   */
  public static SPrimitive deserialize(String s) {
    Preconditions.checkArgument(s != null && !s.isEmpty(), "Null or empty string");

    SNodeAccessControl acToken = null;
    if (SNodeAccessControl.isToken(s)) {
      int firstSepIndex = s.indexOf(PRIMITIVE_SEPARATOR);
      acToken = SNodeAccessControl.deserialize(s.substring(0, firstSepIndex));
      s = s.substring(firstSepIndex+1);
    } else {
      acToken = new SNodeAccessControl();
    }


    if (s.startsWith(PRIMITIVE_STRING_TYPE_PREFIX+PRIMITIVE_SEPARATOR)) {
      return new SPrimitive(s.substring(2), acToken);
    }

    if (s.startsWith(PRIMITIVE_INTEGER_TYPE_PREFIX+PRIMITIVE_SEPARATOR)) {
      try {
       return new SPrimitive(Integer.parseInt(s.substring(2)), acToken);
      } catch (NumberFormatException e) {
        // Oops
        return null;
      }
    }

    if (s.startsWith(PRIMITIVE_DOUBLE_TYPE_PREFIX+PRIMITIVE_SEPARATOR)) {
      try {
       return new SPrimitive(Double.parseDouble(s.substring(2)), acToken);
      } catch (NumberFormatException e) {
        // Oops
        return null;
      }
    }

    if (s.startsWith(PRIMITIVE_BOOLEAN_TYPE_PREFIX+PRIMITIVE_SEPARATOR)) {
       return new SPrimitive(Boolean.parseBoolean(s.substring(2)), acToken);
    }

    if (s.startsWith(PRIMITIVE_JSO_TYPE_PREFIX+PRIMITIVE_SEPARATOR)) {
      return new SPrimitive(ModelFactory.instance.parseJsonObject(s.substring(3)), acToken);
    }

    return null;
  }


  public String serialize() {

    String token = accessControl.serialize();
    if (!token.isEmpty())
      token += PRIMITIVE_SEPARATOR;


    if (type == TYPE_STRING)
      return token + PRIMITIVE_STRING_TYPE_PREFIX+PRIMITIVE_SEPARATOR+stringValue;

    if (type == TYPE_BOOL)
      return token + PRIMITIVE_BOOLEAN_TYPE_PREFIX+PRIMITIVE_SEPARATOR+Boolean.toString(boolValue);

    if (type == TYPE_INT)
      return token + PRIMITIVE_INTEGER_TYPE_PREFIX+PRIMITIVE_SEPARATOR+Integer.toString(intValue);

    if (type == TYPE_DOUBLE)
      return token + PRIMITIVE_DOUBLE_TYPE_PREFIX+PRIMITIVE_SEPARATOR+Double.toString(doubleValue);

    if (type == TYPE_JSO)
      return token + PRIMITIVE_JSO_TYPE_PREFIX + PRIMITIVE_SEPARATOR
          + ModelFactory.instance.serializeJsonObject(jsoValue);

    return null;
  }


  @JsIgnore
  public SPrimitive(int value, SNodeAccessControl token) {
    type = TYPE_INT;
    intValue = value;
    doubleValue = Double.NaN;
    stringValue = null;
    boolValue = null;
    jsoValue = null;
    accessControl = token;
  }

  @JsIgnore
  public SPrimitive(double value, SNodeAccessControl token) {
    type = TYPE_DOUBLE;
    intValue = Integer.MAX_VALUE;
    doubleValue = value;
    stringValue = null;
    boolValue = null;
    jsoValue = null;
    accessControl = token;
  }

  @JsIgnore
  public SPrimitive(String value, SNodeAccessControl token) {
    type = TYPE_STRING;
    intValue = Integer.MAX_VALUE;
    doubleValue = Double.NaN;
    stringValue = value;
    boolValue = null;
    jsoValue = null;
    accessControl = token;
  }

  @JsIgnore
  public SPrimitive(boolean value, SNodeAccessControl token) {
    type = TYPE_BOOL;
    intValue = Integer.MAX_VALUE;
    doubleValue = Double.NaN;
    stringValue = null;
    boolValue = value;
    jsoValue = null;
    accessControl = token;
  }

  @JsIgnore
  public SPrimitive(Object value, SNodeAccessControl token) {
    type = TYPE_JSO;
    intValue = Integer.MAX_VALUE;
    doubleValue = Double.NaN;
    stringValue = null;
    boolValue = null;

    if (value instanceof SJsonObject) {
      jsoValue = ((SJsonObject) value).getNative();
    } else {
      jsoValue = value;
    }

    accessControl = token;
  }

  @JsIgnore
  public SPrimitive(Object value, SNodeAccessControl token, SPrimitive container,
      String containerPath, String valuePath) {
    type = TYPE_JSO;
    intValue = Integer.MAX_VALUE;
    doubleValue = Double.NaN;
    stringValue = null;
    boolValue = null;

    if (value instanceof SJsonObject) {
      jsoValue = ((SJsonObject) value).getNative();
    } else {
      jsoValue = value;
    }

    accessControl = token;

    this.container = container;
    this.containerPath = containerPath;
    this.valuePath = valuePath;
  }

  @JsProperty
  public int getType() {
    return type;
  }

  @JsProperty
  public Object getValue() {

    if (type == TYPE_STRING)
      return stringValue;

    if (type == TYPE_INT)
      return intValue;

    if (type == TYPE_DOUBLE)
      return doubleValue;

    if (type == TYPE_BOOL)
      return boolValue;

    if (type == TYPE_JSO)
      return jsoValue;

    return null;
  }

  @Override
  public String toString() {
    return "Primitive Value ["+ serialize()+"]";
  }

  @JsIgnore
  public void setNameKey(String nameKey) {
    this.nameKey = nameKey;
  }

  @JsIgnore
  public String getNameKey() {
    return this.nameKey;
  }



  /**
   * Check if this value can be written by a participant
   * @param node
   * @param participantId
   * @return
   */
  @JsIgnore
  public boolean canWrite(ParticipantId participantId) {
    return accessControl.canWrite(participantId);
  }

  /**
   * Check if this value can be read by a participant
   * @param node
   * @param participantId
   * @return
   */
  @JsIgnore
  public boolean canRead(ParticipantId participantId) {
    return accessControl.canRead(participantId);
  }

  protected SNodeAccessControl getNodeAccessControl() {
    return accessControl;
  }

  @JsIgnore
  @Override
  public void accept(SVisitor visitor) {
    visitor.visit(this);
  }


  public SPrimitive getContainer() {
    return this.container;
  }

  public String getContainerPath() {
    return this.containerPath;
  }

  public String getValuePath() {
    return valuePath;

  }

  @JsIgnore
  public SJsonObject asSJson() {
    if (type == TYPE_JSO) {
      return WaveDeps.sJsonFactory.create(this.jsoValue);
    }

    return null;
  }

  //
  // -----------------------------------------------------
  //

  @Override
  public void set(String path, Object value) {
  }

  @Override
  public void push(String path, Object value) {
  }

  @Override
  public Object pop(String path) {
    return null;
  }

  @Override
  public int length(String path) {
    return -1;
  }

  @Override
  public boolean contains(String path, String property) {
    return false;
  }

  @Override
  public void delete(String path) {
  }

  @Override
  public Object get(String path) {
    if (path == null)
      return getValue();

    return null;
  }

  @Override
  public SNode node(String path) throws SException {
    return null;
  }

  @Override
  public SMap asMap() {
    throw new IllegalStateException("Node is not a map");
  }

  @Override
  public SList<? extends SNode> asList() {
    throw new IllegalStateException("Node is not a list");
  }

  @Override
  public String asString() {
    if (type == TYPE_STRING)
      return stringValue;

    throw new IllegalStateException("Node is not a string");
  }

  @Override
  public double asDouble() {
    if (type == TYPE_DOUBLE)
      return doubleValue;

    throw new IllegalStateException("Node is not a number");
  }

  @Override
  public int asInt() {
    if (type == TYPE_DOUBLE)
      return new Double(doubleValue).intValue();

    throw new IllegalStateException("Node is not a number");
  }

  @Override
  public boolean asBoolean() {
    if (type == TYPE_BOOL)
      return boolValue;

    throw new IllegalStateException("Node is not a boolean");
  }

  @Override
  public SText asText() {
    throw new IllegalStateException("Node is not a text");
  }

  public boolean isString() {
    return type == TYPE_STRING;
  }

  public boolean isNumber() {
    return type == TYPE_DOUBLE;
  }

  public boolean isBoolean() {
    return type == TYPE_BOOL;
  }

  public boolean isJso() {
    return type == TYPE_JSO;
  }
}
