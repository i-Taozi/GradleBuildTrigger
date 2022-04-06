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
package com.canoo.dp.impl.remoting.codec.encoders;

import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.dp.impl.remoting.legacy.communication.EmptyCommand;
import com.google.gson.JsonObject;
import org.apiguardian.api.API;

import static com.canoo.dp.impl.remoting.legacy.communication.CommandConstants.EMPTY_COMMAND_ID;
import static com.canoo.dp.impl.remoting.legacy.communication.CommandConstants.ID;
import static org.apiguardian.api.API.Status.DEPRECATED;

@Deprecated
@API(since = "0.x", status = DEPRECATED)
public class EmptyCommandEncoder extends AbstractCommandTranscoder<EmptyCommand> {
    @Override
    public JsonObject encode(final EmptyCommand command) {
        Assert.requireNonNull(command, "command");
        final JsonObject jsonCommand = new JsonObject();
        jsonCommand.addProperty(ID, EMPTY_COMMAND_ID);
        return jsonCommand;
    }

    @Override
    public EmptyCommand decode(final JsonObject jsonObject) {
        return new EmptyCommand();
    }
}
