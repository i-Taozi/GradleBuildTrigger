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
package com.canoo.dp.impl.remoting.legacy;

import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.DEPRECATED;

@API(since = "0.x", status = DEPRECATED)
public interface RemotingConstants {

    String CLIENT_ORIGIN = "C";

    String SERVER_ORIGIN = "S";

    String CLIENT_PM_AUTO_ID_SUFFIX = "-AUTO-CLT";

    String SERVER_PM_AUTO_ID_SUFFIX = "-AUTO-SRV";

    String SOURCE_SYSTEM = "@@@ SOURCE_SYSTEM @@@";

    String SOURCE_SYSTEM_CLIENT = "client";

    String SOURCE_SYSTEM_SERVER = "server";

}
