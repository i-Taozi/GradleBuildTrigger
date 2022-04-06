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
package com.canoo.dp.impl.server.security.spring;

import com.canoo.dp.impl.server.security.DolphinKeycloakSecurityBootstrap;
import com.canoo.platform.server.security.SecurityContext;
import org.apiguardian.api.API;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.annotation.ApplicationScope;

import java.util.function.Supplier;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(since = "0.19.0", status = INTERNAL)
@Configuration
public class SecurityBeanFactory {

    @Bean("security")
    @ApplicationScope
    public SecurityContext createDolphinSession() {
        return DolphinKeycloakSecurityBootstrap.getInstance().getSecurityForCurrentRequest();
    }

    @Bean
    @ApplicationScope
    public RestTemplate restTemplate(final SecurityClientRequestFactory requestFactory) {
        final RestTemplate template = new RestTemplate(requestFactory);
        return template;
    }

    @Bean
    @ApplicationScope
    public SecurityClientRequestFactory securityClientRequestFactory() {
        final Supplier<String> securityTokenSupplier = () -> DolphinKeycloakSecurityBootstrap.getInstance()
                .tokenForCurrentRequest().orElse(null);
        final Supplier<String> realmSupplier = () -> DolphinKeycloakSecurityBootstrap.getInstance()
                .realmForCurrentRequest().orElse(null);
        final Supplier<String> appNameSupplier = () -> DolphinKeycloakSecurityBootstrap.getInstance()
                .appNameForCurrentRequest().orElse(null);
        return new SecurityClientRequestFactory(securityTokenSupplier, realmSupplier, appNameSupplier);
    }
}
