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
package com.canoo.dp.impl.remoting.legacy.communication;

import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.DEPRECATED;

//Deprecated because the only type that can change is the qualifier. We should name it to QualifierChangedCommand or something
@Deprecated
@API(since = "0.x", status = DEPRECATED)
public final class AttributeMetadataChangedCommand extends Command {

    private String attributeId;

    private String metadataName;

    private Object value;

    public AttributeMetadataChangedCommand() {
        super(CommandConstants.ATTRIBUTE_METADATA_CHANGED_COMMAND_ID);

    }

    public AttributeMetadataChangedCommand(final String attributeId, final String metadataName, final Object value) {
        this();
        this.attributeId = attributeId;
        this.metadataName = metadataName;
        this.value = value;
    }

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(final String attributeId) {
        this.attributeId = attributeId;
    }

    public String getMetadataName() {
        return metadataName;
    }

    public void setMetadataName(final String metadataName) {
        this.metadataName = metadataName;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(final Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return super.toString() + " attr:" + attributeId + ", metadataName:" + metadataName + " value:" + String.valueOf(value);
    }
}
