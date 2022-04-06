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
package com.canoo.dp.impl.server.event;

import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.platform.core.PlatformConfiguration;
import org.apiguardian.api.API;

import java.io.Serializable;

import static com.canoo.dp.impl.server.event.DistributedEventBusConfigProvider.DEFAULT_HAZELCAST_CONNECTION_ATTEMPT_COUNT;
import static com.canoo.dp.impl.server.event.DistributedEventBusConfigProvider.DEFAULT_HAZELCAST_CONNECTION_ATTEMPT_PERIOD;
import static com.canoo.dp.impl.server.event.DistributedEventBusConfigProvider.DEFAULT_HAZELCAST_CONNECTION_TIMEOUT;
import static com.canoo.dp.impl.server.event.DistributedEventBusConfigProvider.DEFAULT_HAZELCAST_GROUP_NAME;
import static com.canoo.dp.impl.server.event.DistributedEventBusConfigProvider.DEFAULT_HAZELCAST_PORT;
import static com.canoo.dp.impl.server.event.DistributedEventBusConfigProvider.DEFAULT_HAZELCAST_SERVER;
import static com.canoo.dp.impl.server.event.DistributedEventBusConfigProvider.HAZELCAST_CONNECTION_ATTEMPT_COUNT;
import static com.canoo.dp.impl.server.event.DistributedEventBusConfigProvider.HAZELCAST_CONNECTION_ATTEMPT_PERIOD;
import static com.canoo.dp.impl.server.event.DistributedEventBusConfigProvider.HAZELCAST_CONNECTION_TIMEOUT;
import static com.canoo.dp.impl.server.event.DistributedEventBusConfigProvider.HAZELCAST_GROUP_NAME;
import static com.canoo.dp.impl.server.event.DistributedEventBusConfigProvider.HAZELCAST_SERVER_NAME;
import static com.canoo.dp.impl.server.event.DistributedEventBusConfigProvider.HAZELCAST_SERVER_PORT;
import static org.apiguardian.api.API.Status.INTERNAL;

@API(since = "0.x", status = INTERNAL)
public class HazelcastConfig implements Serializable {

    private final PlatformConfiguration configuration;

    public HazelcastConfig(final PlatformConfiguration configuration) {
        this.configuration = Assert.requireNonNull(configuration, "configuration");
    }

    public String getServerName() {
        return configuration.getProperty(HAZELCAST_SERVER_NAME, DEFAULT_HAZELCAST_SERVER);
    }

    public String getServerPort() {
        return configuration.getProperty(HAZELCAST_SERVER_PORT, DEFAULT_HAZELCAST_PORT);
    }

    public String getGroupName() {
        return configuration.getProperty(HAZELCAST_GROUP_NAME, DEFAULT_HAZELCAST_GROUP_NAME);
    }

    public int getConnectionAttemptLimit() {
        return configuration.getIntProperty(HAZELCAST_CONNECTION_ATTEMPT_COUNT, DEFAULT_HAZELCAST_CONNECTION_ATTEMPT_COUNT);
    }

    public int getConnectionAttemptPeriod() {
        return configuration.getIntProperty(HAZELCAST_CONNECTION_ATTEMPT_PERIOD, DEFAULT_HAZELCAST_CONNECTION_ATTEMPT_PERIOD);
    }

    public int getConnectionTimeout() {
        return configuration.getIntProperty(HAZELCAST_CONNECTION_TIMEOUT, DEFAULT_HAZELCAST_CONNECTION_TIMEOUT);
    }

}
