/*
 * Copyright 2015-2018 Canoo Engineering AG.
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
package com.canoo.dp.impl.platform.server.metrics.module;

import com.canoo.dp.impl.platform.core.context.ContextManagerImpl;
import com.canoo.dp.impl.platform.metrics.MetricsImpl;
import com.canoo.dp.impl.platform.metrics.TagUtil;
import com.canoo.dp.impl.platform.server.metrics.servlet.MetricsHttpSessionListener;
import com.canoo.dp.impl.platform.server.metrics.servlet.MetricsServlet;
import com.canoo.dp.impl.platform.server.metrics.servlet.RequestMetricsFilter;
import com.canoo.platform.core.PlatformConfiguration;
import com.canoo.platform.server.spi.AbstractBaseModule;
import com.canoo.platform.server.spi.ModuleDefinition;
import com.canoo.platform.server.spi.ServerCoreComponents;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import java.util.EnumSet;
import java.util.List;

import static com.canoo.dp.impl.platform.server.metrics.module.MetricsConfigConstants.METRICS_ACTIVE_PROPERTY;
import static com.canoo.dp.impl.platform.server.metrics.module.MetricsConfigConstants.METRICS_ENDPOINT_PROPERTY;
import static com.canoo.dp.impl.platform.server.metrics.module.MetricsConfigConstants.METRICS_NOOP_PROPERTY;
import static com.canoo.dp.impl.platform.server.metrics.module.MetricsConfigConstants.METRICS_SERVLET_FILTER_NAME;
import static com.canoo.dp.impl.platform.server.metrics.module.MetricsConfigConstants.METRICS_SERVLET_NAME;
import static com.canoo.dp.impl.platform.server.metrics.module.MetricsConfigConstants.MODULE_NAME;
import static com.canoo.dp.impl.server.servlet.ServletConstants.ALL_URL_MAPPING;
import static org.apiguardian.api.API.Status.INTERNAL;

@API(since = "1.0.0", status = INTERNAL)
@ModuleDefinition
public class MetricsModule extends AbstractBaseModule {

    private final static Logger LOG = LoggerFactory.getLogger(MetricsModule.class);

    @Override
    protected String getActivePropertyName() {
        return METRICS_ACTIVE_PROPERTY;
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public void initialize(final ServerCoreComponents coreComponents) {
        final PlatformConfiguration configuration = coreComponents.getConfiguration();
        final ServletContext servletContext = coreComponents.getInstance(ServletContext.class);

        if(!configuration.getBooleanProperty(METRICS_NOOP_PROPERTY, true)) {

            final PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

            final List<Tag> tagList = TagUtil.convertTags(ContextManagerImpl.getInstance().getGlobalContexts());

            new ClassLoaderMetrics(tagList).bindTo(prometheusRegistry);
            new JvmMemoryMetrics(tagList).bindTo(prometheusRegistry);
            new JvmGcMetrics(tagList).bindTo(prometheusRegistry);
            new ProcessorMetrics(tagList).bindTo(prometheusRegistry);
            new JvmThreadMetrics(tagList).bindTo(prometheusRegistry);

            servletContext.addFilter(METRICS_SERVLET_FILTER_NAME, new RequestMetricsFilter())
                    .addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, ALL_URL_MAPPING);

            servletContext.addListener(new MetricsHttpSessionListener());

            servletContext.addServlet(METRICS_SERVLET_NAME, new MetricsServlet(prometheusRegistry))
                    .addMapping(configuration.getProperty(METRICS_ENDPOINT_PROPERTY));

            MetricsImpl.getInstance().init(prometheusRegistry);
        }
    }
}
