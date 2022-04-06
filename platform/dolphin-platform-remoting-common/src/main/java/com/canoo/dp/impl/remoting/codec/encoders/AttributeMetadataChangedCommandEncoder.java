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
import com.canoo.dp.impl.remoting.legacy.communication.AttributeMetadataChangedCommand;
import com.google.gson.JsonObject;
import org.apiguardian.api.API;

import static com.canoo.dp.impl.remoting.legacy.communication.CommandConstants.ATTRIBUTE_ID;
import static com.canoo.dp.impl.remoting.legacy.communication.CommandConstants.ATTRIBUTE_METADATA_CHANGED_COMMAND_ID;
import static com.canoo.dp.impl.remoting.legacy.communication.CommandConstants.ID;
import static com.canoo.dp.impl.remoting.legacy.communication.CommandConstants.NAME;
import static com.canoo.dp.impl.remoting.legacy.communication.CommandConstants.VALUE;
import static org.apiguardian.api.API.Status.DEPRECATED;

@Deprecated
@API(since = "0.x", status = DEPRECATED)
public class AttributeMetadataChangedCommandEncoder extends AbstractCommandTranscoder<AttributeMetadataChangedCommand> {

    @Override
    public JsonObject encode(final AttributeMetadataChangedCommand command) {
        Assert.requireNonNull(command, "command");
        final JsonObject jsonCommand = new JsonObject();
        jsonCommand.addProperty(ID, ATTRIBUTE_METADATA_CHANGED_COMMAND_ID);
        jsonCommand.addProperty(ATTRIBUTE_ID, command.getAttributeId());
        jsonCommand.addProperty(NAME, command.getMetadataName());
        jsonCommand.add(VALUE, ValueEncoder.encodeValue(command.getValue()));
        return jsonCommand;
    }

    @Override
    public AttributeMetadataChangedCommand decode(final JsonObject jsonObject) {
        final AttributeMetadataChangedCommand command = new AttributeMetadataChangedCommand();
        command.setAttributeId(getStringElement(jsonObject, ATTRIBUTE_ID));
        command.setMetadataName(getStringElement(jsonObject, NAME));
        command.setValue(ValueEncoder.decodeValue(jsonObject.get(VALUE)));
        return command;
    }
}
