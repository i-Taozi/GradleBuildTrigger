/*
 * Copyright 2018, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.contrib.logcorrelation.log4j2;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Lists;
import io.opencensus.common.Scope;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracestate;
import io.opencensus.trace.Tracing;
import java.util.Collections;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.util.SortedArrayStringMap;
import org.apache.logging.log4j.util.StringMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link OpenCensusTraceContextDataInjector}. */
@RunWith(JUnit4.class)
public final class OpenCensusTraceContextDataInjectorTest {
  static final Tracestate EMPTY_TRACESTATE = Tracestate.builder().build();

  private final Tracer tracer = Tracing.getTracer();

  @Test
  public void traceIdKey() {
    assertThat(OpenCensusTraceContextDataInjector.TRACE_ID_CONTEXT_KEY).isEqualTo("traceId");
  }

  @Test
  public void spanIdKey() {
    assertThat(OpenCensusTraceContextDataInjector.SPAN_ID_CONTEXT_KEY).isEqualTo("spanId");
  }

  @Test
  public void traceSampledKey() {
    assertThat(OpenCensusTraceContextDataInjector.TRACE_SAMPLED_CONTEXT_KEY)
        .isEqualTo("traceSampled");
  }

  @Test
  public void insertConfigurationProperties() {
    assertThat(
            new OpenCensusTraceContextDataInjector()
                .injectContextData(
                    Lists.newArrayList(
                        Property.createProperty("property1", "value1"),
                        Property.createProperty("property2", "value2")),
                    new SortedArrayStringMap())
                .toMap())
        .containsExactly(
            "property1",
            "value1",
            "property2",
            "value2",
            "traceId",
            "00000000000000000000000000000000",
            "spanId",
            "0000000000000000",
            "traceSampled",
            "false");
  }

  @Test
  public void handleEmptyConfigurationProperties() {
    assertContainsOnlyDefaultTracingEntries(
        new OpenCensusTraceContextDataInjector()
            .injectContextData(Collections.<Property>emptyList(), new SortedArrayStringMap()));
  }

  @Test
  public void handleNullConfigurationProperties() {
    assertContainsOnlyDefaultTracingEntries(
        new OpenCensusTraceContextDataInjector()
            .injectContextData(null, new SortedArrayStringMap()));
  }

  private static void assertContainsOnlyDefaultTracingEntries(StringMap stringMap) {
    assertThat(stringMap.toMap())
        .containsExactly(
            "traceId",
            "00000000000000000000000000000000",
            "spanId",
            "0000000000000000",
            "traceSampled",
            "false");
  }

  @Test
  public void rawContextDataWithTracingData() {
    OpenCensusTraceContextDataInjector plugin = new OpenCensusTraceContextDataInjector();
    SpanContext spanContext =
        SpanContext.create(
            TraceId.fromLowerBase16("e17944156660f55b8cae5ce3f45d4a40"),
            SpanId.fromLowerBase16("fc3d2ba0d283b66a"),
            TraceOptions.builder().setIsSampled(true).build(),
            EMPTY_TRACESTATE);
    Scope scope = tracer.withSpan(new TestSpan(spanContext));
    try {
      String key = "myTestKey";
      ThreadContext.put(key, "myTestValue");
      try {
        assertThat(plugin.rawContextData().toMap())
            .containsExactly(
                "myTestKey",
                "myTestValue",
                "traceId",
                "e17944156660f55b8cae5ce3f45d4a40",
                "spanId",
                "fc3d2ba0d283b66a",
                "traceSampled",
                "true");
      } finally {
        ThreadContext.remove(key);
      }
    } finally {
      scope.close();
    }
  }
}
