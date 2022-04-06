// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: BloodTestMessage.proto at 9:1
package com.eveningoutpost.dexdrip.messages;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;
import java.io.IOException;
import java.lang.Double;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

public final class BloodTestMessage extends Message<BloodTestMessage, BloodTestMessage.Builder> {
  public static final ProtoAdapter<BloodTestMessage> ADAPTER = new ProtoAdapter_BloodTestMessage();

  private static final long serialVersionUID = 0L;

  public static final Long DEFAULT_TIMESTAMP = 0L;

  public static final Double DEFAULT_MGDL = 0.0d;

  public static final Long DEFAULT_CREATED_TIMESTAMP = 0L;

  public static final Long DEFAULT_STATE = 0L;

  public static final String DEFAULT_SOURCE = "";

  public static final String DEFAULT_UUID = "";

  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#SINT64"
  )
  public final Long timestamp;

  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#DOUBLE"
  )
  public final Double mgdl;

  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#SINT64"
  )
  public final Long created_timestamp;

  @WireField(
      tag = 4,
      adapter = "com.squareup.wire.ProtoAdapter#SINT64"
  )
  public final Long state;

  @WireField(
      tag = 5,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String source;

  @WireField(
      tag = 6,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String uuid;

  public BloodTestMessage(Long timestamp, Double mgdl, Long created_timestamp, Long state, String source, String uuid) {
    this(timestamp, mgdl, created_timestamp, state, source, uuid, ByteString.EMPTY);
  }

  public BloodTestMessage(Long timestamp, Double mgdl, Long created_timestamp, Long state, String source, String uuid, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.timestamp = timestamp;
    this.mgdl = mgdl;
    this.created_timestamp = created_timestamp;
    this.state = state;
    this.source = source;
    this.uuid = uuid;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.timestamp = timestamp;
    builder.mgdl = mgdl;
    builder.created_timestamp = created_timestamp;
    builder.state = state;
    builder.source = source;
    builder.uuid = uuid;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof BloodTestMessage)) return false;
    BloodTestMessage o = (BloodTestMessage) other;
    return Internal.equals(unknownFields(), o.unknownFields())
        && Internal.equals(timestamp, o.timestamp)
        && Internal.equals(mgdl, o.mgdl)
        && Internal.equals(created_timestamp, o.created_timestamp)
        && Internal.equals(state, o.state)
        && Internal.equals(source, o.source)
        && Internal.equals(uuid, o.uuid);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (timestamp != null ? timestamp.hashCode() : 0);
      result = result * 37 + (mgdl != null ? mgdl.hashCode() : 0);
      result = result * 37 + (created_timestamp != null ? created_timestamp.hashCode() : 0);
      result = result * 37 + (state != null ? state.hashCode() : 0);
      result = result * 37 + (source != null ? source.hashCode() : 0);
      result = result * 37 + (uuid != null ? uuid.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (timestamp != null) builder.append(", timestamp=").append(timestamp);
    if (mgdl != null) builder.append(", mgdl=").append(mgdl);
    if (created_timestamp != null) builder.append(", created_timestamp=").append(created_timestamp);
    if (state != null) builder.append(", state=").append(state);
    if (source != null) builder.append(", source=").append(source);
    if (uuid != null) builder.append(", uuid=").append(uuid);
    return builder.replace(0, 2, "BloodTestMessage{").append('}').toString();
  }

  public static final class Builder extends Message.Builder<BloodTestMessage, Builder> {
    public Long timestamp;

    public Double mgdl;

    public Long created_timestamp;

    public Long state;

    public String source;

    public String uuid;

    public Builder() {
    }

    public Builder timestamp(Long timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    public Builder mgdl(Double mgdl) {
      this.mgdl = mgdl;
      return this;
    }

    public Builder created_timestamp(Long created_timestamp) {
      this.created_timestamp = created_timestamp;
      return this;
    }

    public Builder state(Long state) {
      this.state = state;
      return this;
    }

    public Builder source(String source) {
      this.source = source;
      return this;
    }

    public Builder uuid(String uuid) {
      this.uuid = uuid;
      return this;
    }

    @Override
    public BloodTestMessage build() {
      return new BloodTestMessage(timestamp, mgdl, created_timestamp, state, source, uuid, buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_BloodTestMessage extends ProtoAdapter<BloodTestMessage> {
    ProtoAdapter_BloodTestMessage() {
      super(FieldEncoding.LENGTH_DELIMITED, BloodTestMessage.class);
    }

    @Override
    public int encodedSize(BloodTestMessage value) {
      return (value.timestamp != null ? ProtoAdapter.SINT64.encodedSizeWithTag(1, value.timestamp) : 0)
          + (value.mgdl != null ? ProtoAdapter.DOUBLE.encodedSizeWithTag(2, value.mgdl) : 0)
          + (value.created_timestamp != null ? ProtoAdapter.SINT64.encodedSizeWithTag(3, value.created_timestamp) : 0)
          + (value.state != null ? ProtoAdapter.SINT64.encodedSizeWithTag(4, value.state) : 0)
          + (value.source != null ? ProtoAdapter.STRING.encodedSizeWithTag(5, value.source) : 0)
          + (value.uuid != null ? ProtoAdapter.STRING.encodedSizeWithTag(6, value.uuid) : 0)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, BloodTestMessage value) throws IOException {
      if (value.timestamp != null) ProtoAdapter.SINT64.encodeWithTag(writer, 1, value.timestamp);
      if (value.mgdl != null) ProtoAdapter.DOUBLE.encodeWithTag(writer, 2, value.mgdl);
      if (value.created_timestamp != null) ProtoAdapter.SINT64.encodeWithTag(writer, 3, value.created_timestamp);
      if (value.state != null) ProtoAdapter.SINT64.encodeWithTag(writer, 4, value.state);
      if (value.source != null) ProtoAdapter.STRING.encodeWithTag(writer, 5, value.source);
      if (value.uuid != null) ProtoAdapter.STRING.encodeWithTag(writer, 6, value.uuid);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public BloodTestMessage decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.timestamp(ProtoAdapter.SINT64.decode(reader)); break;
          case 2: builder.mgdl(ProtoAdapter.DOUBLE.decode(reader)); break;
          case 3: builder.created_timestamp(ProtoAdapter.SINT64.decode(reader)); break;
          case 4: builder.state(ProtoAdapter.SINT64.decode(reader)); break;
          case 5: builder.source(ProtoAdapter.STRING.decode(reader)); break;
          case 6: builder.uuid(ProtoAdapter.STRING.decode(reader)); break;
          default: {
            FieldEncoding fieldEncoding = reader.peekFieldEncoding();
            Object value = fieldEncoding.rawProtoAdapter().decode(reader);
            builder.addUnknownField(tag, fieldEncoding, value);
          }
        }
      }
      reader.endMessage(token);
      return builder.build();
    }

    @Override
    public BloodTestMessage redact(BloodTestMessage value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
