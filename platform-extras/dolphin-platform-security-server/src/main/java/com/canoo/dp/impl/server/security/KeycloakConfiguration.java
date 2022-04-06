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
import org.apiguardian.api.API;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.canoo.dp.impl.security.SecurityConstants.APPLICATION_PROPERTY_NAME;
import static com.canoo.dp.impl.security.SecurityConstants.AUTH_ENDPOINT_PROPERTY_DEFAULT_VALUE;
import static com.canoo.dp.impl.security.SecurityConstants.AUTH_ENDPOINT_PROPERTY_NAME;
import static com.canoo.dp.impl.security.SecurityConstants.REALM_PROPERTY_NAME;
import static com.canoo.dp.impl.server.security.SecurityServerConstants.CORS_PROPERTY_DEFAULT_VALUE;
import static com.canoo.dp.impl.server.security.SecurityServerConstants.CORS_PROPERTY_NAME;
import static com.canoo.dp.impl.server.security.SecurityServerConstants.LOGIN_ENDPOINTS_ACTIVE_PROPERTY_NAME;
import static com.canoo.dp.impl.server.security.SecurityServerConstants.LOGIN_ENDPOINTS_PROPERTY_DEFAULT_VALUE;
import static com.canoo.dp.impl.server.security.SecurityServerConstants.LOGIN_ENDPOINTS_PROPERTY_NAME;
import static com.canoo.dp.impl.server.security.SecurityServerConstants.LOGOUT_ENDPOINTS_PROPERTY_DEFAULT_VALUE;
import static com.canoo.dp.impl.server.security.SecurityServerConstants.LOGOUT_ENDPOINTS_PROPERTY_NAME;
import static com.canoo.dp.impl.server.security.SecurityServerConstants.REALMS_PROPERTY_NAME;
import static com.canoo.dp.impl.server.security.SecurityServerConstants.SECURE_ENDPOINTS_PROPERTY_NAME;
import static com.canoo.dp.impl.server.security.SecurityServerConstants.SECURITY_ACTIVE_PROPERTY_NAME;
import static org.apiguardian.api.API.Status.INTERNAL;

@API(since = "0.19.0", status = INTERNAL)
public class KeycloakConfiguration implements Serializable {

    private final String realmName;

    private final List<String> realmNames;

    private final boolean securityActive;

    private final boolean loginEndpointActive;

    private final String applicationName;

    private final String authEndpoint;

    private final String loginEndpoint;

    private final String logoutEndpoint;

    private final List<String> secureEndpoints = new ArrayList<>();

    private final boolean cors;

    public KeycloakConfiguration(final PlatformConfiguration platformConfiguration) {
        Assert.requireNonNull(platformConfiguration, "platformConfiguration");
        this.realmName = platformConfiguration.getProperty(REALM_PROPERTY_NAME);
        final List<String> realmNames = new ArrayList<>(platformConfiguration.getListProperty(REALMS_PROPERTY_NAME, Collections.emptyList()));
        if (this.realmName != null && !this.realmName.isEmpty() && !realmNames.contains(this.realmName)) {
            realmNames.add(this.realmName);
        }
        this.realmNames = Collections.unmodifiableList(realmNames);
        this.applicationName = platformConfiguration.getProperty(APPLICATION_PROPERTY_NAME);
        this.authEndpoint = platformConfiguration.getProperty(AUTH_ENDPOINT_PROPERTY_NAME, AUTH_ENDPOINT_PROPERTY_DEFAULT_VALUE) + "/auth";
        this.secureEndpoints.addAll(platformConfiguration.getListProperty(SECURE_ENDPOINTS_PROPERTY_NAME, Collections.emptyList()));
        this.securityActive = platformConfiguration.getBooleanProperty(SECURITY_ACTIVE_PROPERTY_NAME, false);
        this.loginEndpointActive = platformConfiguration.getBooleanProperty(LOGIN_ENDPOINTS_ACTIVE_PROPERTY_NAME, true);
        this.loginEndpoint = platformConfiguration.getProperty(LOGIN_ENDPOINTS_PROPERTY_NAME, LOGIN_ENDPOINTS_PROPERTY_DEFAULT_VALUE);
        this.logoutEndpoint = platformConfiguration.getProperty(LOGOUT_ENDPOINTS_PROPERTY_NAME, LOGOUT_ENDPOINTS_PROPERTY_DEFAULT_VALUE);
        this.cors  = platformConfiguration.getBooleanProperty(CORS_PROPERTY_NAME, CORS_PROPERTY_DEFAULT_VALUE);

    }

    public String getRealmName() {
        return realmName;
    }

    public List<String> getRealmNames() {
        return realmNames;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getAuthEndpoint() {
        return authEndpoint;
    }

    public List<String> getSecureEndpoints() {
        return Collections.unmodifiableList(secureEndpoints);
    }

    public String[] getSecureEndpointsArray() {
        return secureEndpoints.toArray(new String[secureEndpoints.size()]);
    }

    public void setSecureEndpoints(final List<String> endpoints) {
        Assert.requireNonNull(endpoints, "endpoints");
        this.secureEndpoints.clear();
        this.secureEndpoints.addAll(endpoints);
    }

    public boolean isSecurityActive() {
        return securityActive;
    }

    public boolean isLoginEndpointActive() {
        return loginEndpointActive;
    }

    public String getLoginEndpoint() {
        return loginEndpoint;
    }

    public boolean isCors() {
        return cors;
    }

    public String getLogoutEndpoint() {
        return logoutEndpoint;
    }
}
