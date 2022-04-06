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
package com.canoo.dp.impl.server.legacy.action;

import com.canoo.dp.impl.remoting.legacy.communication.Command;
import com.canoo.dp.impl.server.legacy.DTO;
import com.canoo.dp.impl.server.legacy.ServerAttribute;
import com.canoo.dp.impl.server.legacy.ServerModelStore;
import org.apiguardian.api.API;

import java.util.List;

import static org.apiguardian.api.API.Status.INTERNAL;

/**
 * Common superclass for all actions that need access to
 * the ServerModelStore, e.g. to work with the server model store.
 */
@API(since = "0.x", status = INTERNAL)
public abstract class DolphinServerAction implements ServerAction {

    private ServerModelStore serverModelStore;

    private List<Command> dolphinResponse;

    public void presentationModel(final String id, final String presentationModelType, final DTO dto) {
        ServerModelStore.presentationModelCommand(dolphinResponse, id, presentationModelType, dto);
    }

    public void changeValue(final ServerAttribute attribute, final String value) {
        ServerModelStore.changeValueCommand(dolphinResponse, attribute, value);
    }

    public ServerModelStore getServerModelStore() {
        return serverModelStore;
    }

    @Deprecated
    public void setServerModelStore(final ServerModelStore serverModelStore) {
        this.serverModelStore = serverModelStore;
    }

    public List<Command> getDolphinResponse() {
        return dolphinResponse;
    }

    public void setDolphinResponse(final List<Command> dolphinResponse) {
        this.dolphinResponse = dolphinResponse;
    }

}
