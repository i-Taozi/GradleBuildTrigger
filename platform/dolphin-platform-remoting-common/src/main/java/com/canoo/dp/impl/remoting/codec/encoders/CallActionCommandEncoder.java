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
import com.canoo.dp.impl.remoting.commands.CallActionCommand;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.apiguardian.api.API;

import java.util.Map;

import static com.canoo.dp.impl.remoting.legacy.communication.CommandConstants.CALL_ACTION_COMMAND_ID;
import static com.canoo.dp.impl.remoting.legacy.communication.CommandConstants.CONTROLLER_ID;
import static com.canoo.dp.impl.remoting.legacy.communication.CommandConstants.ID;
import static com.canoo.dp.impl.remoting.legacy.communication.CommandConstants.NAME;
import static com.canoo.dp.impl.remoting.legacy.communication.CommandConstants.PARAMS;
import static com.canoo.dp.impl.remoting.legacy.communication.CommandConstants.VALUE;
import static org.apiguardian.api.API.Status.INTERNAL;

@API(since = "0.x", status = INTERNAL)
public class CallActionCommandEncoder extends AbstractCommandTranscoder<CallActionCommand> {

    @Override
    public JsonObject encode(final CallActionCommand command) {
        Assert.requireNonNull(command, "command");
        final JsonObject jsonCommand = new JsonObject();
        jsonCommand.addProperty(CONTROLLER_ID, command.getControllerId());
        jsonCommand.addProperty(NAME, command.getActionName());

        final JsonArray paramArray = new JsonArray();
        for(final Map.Entry<String, Object> paramEntry : command.getParams().entrySet()) {
            final JsonObject paramObject = new JsonObject();
            paramObject.addProperty(NAME, paramEntry.getKey());
            paramObject.add(VALUE, ValueEncoder.encodeValue(paramEntry.getValue()));
            paramArray.add(paramObject);
        }
        jsonCommand.add(PARAMS, paramArray);

        jsonCommand.addProperty(ID, CALL_ACTION_COMMAND_ID);
        return jsonCommand;
    }

    @Override
    public CallActionCommand decode(final JsonObject jsonObject) {
        Assert.requireNonNull(jsonObject, "jsonObject");
        try {
            final CallActionCommand command = new CallActionCommand();
            command.setControllerId(getStringElement(jsonObject, CONTROLLER_ID));
            command.setActionName(getStringElement(jsonObject, NAME));

            final JsonArray jsonArray = jsonObject.getAsJsonArray(PARAMS);
            if(jsonArray != null) {
                for (final JsonElement jsonElement : jsonArray) {
                    final JsonObject paramObject = jsonElement.getAsJsonObject();
                    command.addParam(getStringElement(paramObject, NAME), ValueEncoder.decodeValue(paramObject.get(VALUE)));
                }
            }
            return command;
        } catch (final Exception ex) {
            throw new JsonParseException("Illegal JSON detected", ex);
        }
    }
}
