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
package com.canoo.dp.impl.server.security;

import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.platform.core.PlatformConfiguration;
import com.canoo.platform.server.security.SecurityContext;
import com.canoo.platform.server.security.User;
import org.apiguardian.api.API;
import org.keycloak.adapters.servlet.KeycloakOIDCFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import java.util.EnumSet;
import java.util.Optional;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(since = "0.19.0", status = INTERNAL)
public class DolphinKeycloakSecurityBootstrap {

    private final static Logger LOG = LoggerFactory.getLogger(DolphinKeycloakSecurityBootstrap.class);

    private final static DolphinKeycloakSecurityBootstrap INSTANCE = new DolphinKeycloakSecurityBootstrap();

    private KeycloakSecurityContextExtractFilter extractFilter;

    public void init(final ServletContext servletContext, final PlatformConfiguration configuration) {
        Assert.requireNonNull(servletContext, "servletContext");
        Assert.requireNonNull(configuration, "configuration");

        final KeycloakConfiguration keycloakConfiguration = new KeycloakConfiguration(configuration);
        if(keycloakConfiguration.isSecurityActive()) {
            if (LOG.isInfoEnabled()) {
                keycloakConfiguration.getSecureEndpoints().stream().forEach(e -> {
                    LOG.info("Adding security to the following endpoint: {}", e);
                });
            }
            this.extractFilter = new KeycloakSecurityContextExtractFilter();
            DolphinKeycloakConfigResolver.setConfiguration(keycloakConfiguration);

            final FilterRegistration.Dynamic keycloakSecurityFilter = servletContext.addFilter(SecurityServerConstants.FILTER_NAME, new KeycloakOIDCFilter());
            keycloakSecurityFilter.setInitParameter(SecurityServerConstants.KEYCLOAK_CONFIG_RESOLVER_PROPERTY_NAME, DolphinKeycloakConfigResolver.class.getName());
            keycloakSecurityFilter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, keycloakConfiguration.getSecureEndpointsArray());

            final FilterRegistration.Dynamic keycloakExtractorFilter = servletContext.addFilter(SecurityServerConstants.EXTRACTOR_FILTER_NAME, extractFilter);
            keycloakExtractorFilter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
        }
    }

    public SecurityContext getSecurityForCurrentRequest() {
        return new SecurityContext() {
            @Override
            public User getUser() {
                return extractFilter.getSecurity().getUser();
            }

            @Override
            public void accessDenied() {
                extractFilter.getSecurity().accessDenied();
            }
        };
    }

    public Optional<String> tokenForCurrentRequest() {
        return extractFilter.token();
    }

    public Optional<String> realmForCurrentRequest() {
        return extractFilter.realm();
    }

    public Optional<String> appNameForCurrentRequest() {
        return extractFilter.appName();
    }

    public static DolphinKeycloakSecurityBootstrap getInstance() {
        return INSTANCE;
    }
}
