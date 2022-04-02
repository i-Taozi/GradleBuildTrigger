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

import io.opencensus.common.Function;
import io.opencensus.common.Scope;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracestate;
import io.opencensus.trace.Tracing;
import java.io.StringWriter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.StringLayout;
import org.apache.logging.log4j.core.appender.WriterAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for Log4j log correlation. */
@RunWith(JUnit4.class)
public final class OpenCensusLog4jLogCorrelationTest {
  private static final Tracer tracer = Tracing.getTracer();

  private static final String TEST_PATTERN =
      "traceId=%X{traceId} spanId=%X{spanId} sampled=%X{traceSampled} %-5level - %msg";

  private static final Tracestate EMPTY_TRACESTATE = Tracestate.builder().build();

  private static final Logger logger =
      (Logger) LogManager.getLogger(OpenCensusLog4jLogCorrelationTest.class);

  // Reconfigures Log4j using the given arguments and runs the function with the given SpanContext
  // in scope.
  private static String logWithSpanAndLog4jConfiguration(
      String log4jPattern, SpanContext spanContext, Function<Logger, Void> loggingFunction) {
    StringWriter output = new StringWriter();
    StringLayout layout = PatternLayout.newBuilder().withPattern(log4jPattern).build();
    Appender appender =
        WriterAppender.newBuilder()
            .setTarget(output)
            .setLayout(layout)
            .setName("TestAppender")
            .build();
    ((LoggerContext) LogManager.getContext(false)).updateLoggers();
    appender.start();
    logger.addAppender(appender);
    logger.setLevel(Level.ALL);
    try {
      logWithSpan(spanContext, loggingFunction, logger);
      return output.toString();
    } finally {
      logger.removeAppender(appender);
    }
  }

  private static void logWithSpan(
      SpanContext spanContext, Function<Logger, Void> loggingFunction, Logger logger) {
    Scope scope = tracer.withSpan(new TestSpan(spanContext));
    try {
      loggingFunction.apply(logger);
    } finally {
      scope.close();
    }
  }

  @Test
  public void addSampledSpanToLogEntryWithAllSpans() {
    String log =
        logWithSpanAndLog4jConfiguration(
            TEST_PATTERN,
            SpanContext.create(
                TraceId.fromLowerBase16("b9718fe3d82d36fce0e6a1ada1c21db0"),
                SpanId.fromLowerBase16("75159dde8c503fee"),
                TraceOptions.builder().setIsSampled(true).build(),
                EMPTY_TRACESTATE),
            new Function<Logger, Void>() {
              @Override
              public Void apply(Logger logger) {
                logger.warn("message #1");
                return null;
              }
            });
    assertThat(log)
        .isEqualTo(
            "traceId=b9718fe3d82d36fce0e6a1ada1c21db0 spanId=75159dde8c503fee "
                + "sampled=true WARN  - message #1");
  }

  @Test
  public void addNonSampledSpanToLogEntryWithAllSpans() {
    String log =
        logWithSpanAndLog4jConfiguration(
            TEST_PATTERN,
            SpanContext.create(
                TraceId.fromLowerBase16("cd7061dfa9d312cdcc42edab3feab51b"),
                SpanId.fromLowerBase16("117d42d4c7acd066"),
                TraceOptions.builder().setIsSampled(false).build(),
                EMPTY_TRACESTATE),
            new Function<Logger, Void>() {
              @Override
              public Void apply(Logger logger) {
                logger.info("message #2");
                return null;
              }
            });
    assertThat(log)
        .isEqualTo(
            "traceId=cd7061dfa9d312cdcc42edab3feab51b spanId=117d42d4c7acd066 sampled=false INFO  "
                + "- message #2");
  }

  @Test
  public void addBlankSpanToLogEntryWithAllSpans() {
    String log =
        logWithSpanAndLog4jConfiguration(
            TEST_PATTERN,
            SpanContext.INVALID,
            new Function<Logger, Void>() {
              @Override
              public Void apply(Logger logger) {
                logger.fatal("message #3");
                return null;
              }
            });
    assertThat(log)
        .isEqualTo(
            "traceId=00000000000000000000000000000000 spanId=0000000000000000 sampled=false FATAL "
                + "- message #3");
  }

  @Test
  public void preserveOtherKeyValuePairs() {
    String log =
        logWithSpanAndLog4jConfiguration(
            "%X{traceId} %X{myTestKey} %-5level - %msg",
            SpanContext.create(
                TraceId.fromLowerBase16("c95329bb6b7de41afbc51a231c128f97"),
                SpanId.fromLowerBase16("bf22ea74d38eddad"),
                TraceOptions.builder().setIsSampled(true).build(),
                EMPTY_TRACESTATE),
            new Function<Logger, Void>() {
              @Override
              public Void apply(Logger logger) {
                String key = "myTestKey";
                ThreadContext.put(key, "myTestValue");
                try {
                  logger.error("message #4");
                } finally {
                  ThreadContext.remove(key);
                }
                return null;
              }
            });
    assertThat(log).isEqualTo("c95329bb6b7de41afbc51a231c128f97 myTestValue ERROR - message #4");
  }

  @Test
  public void overwriteExistingTracingKey() {
    String log =
        logWithSpanAndLog4jConfiguration(
            TEST_PATTERN,
            SpanContext.create(
                TraceId.fromLowerBase16("18e4ae44273a0c44e0c9ea4380792c66"),
                SpanId.fromLowerBase16("199a7e16daa000a7"),
                TraceOptions.builder().setIsSampled(true).build(),
                EMPTY_TRACESTATE),
            new Function<Logger, Void>() {
              @Override
              public Void apply(Logger logger) {
                ThreadContext.put(
                    OpenCensusTraceContextDataInjector.TRACE_ID_CONTEXT_KEY, "existingTraceId");
                try {
                  logger.error("message #5");
                } finally {
                  ThreadContext.remove(OpenCensusTraceContextDataInjector.TRACE_ID_CONTEXT_KEY);
                }
                return null;
              }
            });
    assertThat(log)
        .isEqualTo(
            "traceId=18e4ae44273a0c44e0c9ea4380792c66 spanId=199a7e16daa000a7 "
                + "sampled=true ERROR - message #5");
  }
}
