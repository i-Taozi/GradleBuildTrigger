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
package com.canoo.dp.impl.client.legacy.communication;

import org.apiguardian.api.API;

import java.util.Collections;
import java.util.List;

import static org.apiguardian.api.API.Status.DEPRECATED;

@API(since = "0.x", status = DEPRECATED)
public class CommandBatcher implements ICommandBatcher {

    private final DataflowQueue<List<CommandAndHandler>> waitingBatches;

    public CommandBatcher() {
        this.waitingBatches = new CommandBatcherQueue();
    }

    public void batch(final CommandAndHandler commandAndHandler) {
        waitingBatches.add(Collections.singletonList(commandAndHandler));
    }

    public boolean isEmpty() {
        return waitingBatches.length() == 0;
    }

    public DataflowQueue<List<CommandAndHandler>> getWaitingBatches() {
        return waitingBatches;
    }

}
