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
package com.canoo.dp.impl.remoting.legacy.core;

import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.DEPRECATED;

/**
 * The value may be null as long as the BaseAttribute is used as a "placeholder".
 */
@API(since = "0.x", status = DEPRECATED)
public abstract class BaseAttribute extends AbstractObservable implements Attribute {

    private static long instanceCount = 0;

    private final String propertyName;

    private Object value;

    private PresentationModel presentationModel;

    private String id ;

    private String qualifier; // application specific semantics apply

    public BaseAttribute(String propertyName, Object value) {
        this(propertyName, value, null);
    }

    public BaseAttribute(String propertyName, Object value, String qualifier) {
        this.id = (instanceCount++) + getOrigin();
        this.propertyName = propertyName;
        this.value = value;
        this.qualifier = qualifier;
    }

    /**
     * @return 'C' for client or 'S' for server
     */
    public abstract String getOrigin();

    public void setPresentationModel(PresentationModel presentationModel) {
        if (this.presentationModel != null) {
            throw new IllegalStateException("You can not set a presentation model for an attribute that is already bound.");
        }
        this.presentationModel = presentationModel;
    }

    @Override
    public PresentationModel getPresentationModel() {
        return this.presentationModel;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object newValue) {
        if (isDifferent(value, newValue)) {
            firePropertyChange(VALUE_NAME, value, value = newValue);
        }
    }

    private boolean isDifferent(Object oldValue, Object newValue) {
        return oldValue == null ? newValue != null : !oldValue.equals(newValue);
    }

    public String toString() {
        return new StringBuilder()
                .append(id)
                .append(" : ")
                .append(propertyName)
                .append(" (")
                .append(qualifier).append(") ")
                .append(value).toString();
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        firePropertyChange(QUALIFIER_NAME, this.qualifier, this.qualifier = qualifier);
    }
}
