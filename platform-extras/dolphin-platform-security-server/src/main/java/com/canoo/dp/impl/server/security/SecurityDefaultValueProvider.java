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

import com.canoo.platform.server.spi.ConfigurationProviderAdapter;
import org.apiguardian.api.API;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.canoo.dp.impl.security.SecurityConstants.AUTH_ENDPOINT_PROPERTY_DEFAULT_VALUE;
import static com.canoo.dp.impl.security.SecurityConstants.AUTH_ENDPOINT_PROPERTY_NAME;
import static com.canoo.dp.impl.server.security.SecurityServerConstants.SECURE_ENDPOINTS_PROPERTY_DEFAULT_VALUE;
import static com.canoo.dp.impl.server.security.SecurityServerConstants.SECURE_ENDPOINTS_PROPERTY_NAME;
import static com.canoo.dp.impl.server.security.SecurityServerConstants.SECURITY_ACTIVE_PROPERTY_DEFAULT_VALUE;
import static com.canoo.dp.impl.server.security.SecurityServerConstants.SECURITY_ACTIVE_PROPERTY_NAME;
import static com.canoo.dp.impl.server.security.SecurityServerConstants.CORS_PROPERTY_DEFAULT_VALUE;
import static com.canoo.dp.impl.server.security.SecurityServerConstants.CORS_PROPERTY_NAME;
import static com.canoo.dp.impl.server.security.SecurityServerConstants.REALMS_PROPERTY_NAME;
import static org.apiguardian.api.API.Status.INTERNAL;

@API(since = "0.19.0", status = INTERNAL)
public class SecurityDefaultValueProvider extends ConfigurationProviderAdapter {

    @Override
    public Map<String, String> getStringProperties() {
        final HashMap<String, String> ret = new HashMap<>();
        ret.put(AUTH_ENDPOINT_PROPERTY_NAME, AUTH_ENDPOINT_PROPERTY_DEFAULT_VALUE);
        return ret;
    }

    @Override
    public Map<String, Boolean> getBooleanProperties() {
        final HashMap<String, Boolean> ret = new HashMap<>();
        ret.put(SECURITY_ACTIVE_PROPERTY_NAME, SECURITY_ACTIVE_PROPERTY_DEFAULT_VALUE);
        ret.put(CORS_PROPERTY_NAME, CORS_PROPERTY_DEFAULT_VALUE);
        return ret;
    }

    @Override
    public Map<String, List<String>> getListProperties() {
        final HashMap<String, List<String>> ret = new HashMap<>();
        ret.put(SECURE_ENDPOINTS_PROPERTY_NAME, Collections.<String>singletonList(SECURE_ENDPOINTS_PROPERTY_DEFAULT_VALUE));
        ret.put(REALMS_PROPERTY_NAME, Collections.emptyList());
        return ret;
    }
}
