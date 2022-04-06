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
package com.canoo.dp.impl.client.legacy;


import com.canoo.dp.impl.remoting.legacy.RemotingConstants;
import com.canoo.dp.impl.remoting.legacy.core.BaseAttribute;
import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.DEPRECATED;

/**
 * A client side (remote) ClientAttribute is considered a remote representation of a ServerAttribute.
 * Changes to a remote ClientAttribute are sent to the server. This happens by using a dedicated
 * One can bind against a ClientAttribute in two ways
 * a) as a PropertyChangeListener
 * b) through the valueProperty() method for JavaFx
 */
@API(since = "0.x", status = DEPRECATED)
public class ClientAttribute extends BaseAttribute {

    public ClientAttribute(final String propertyName, final Object initialValue, final String qualifier) {
        super(propertyName, initialValue, qualifier);
    }

    public ClientAttribute(final String propertyName, final Object initialValue) {
        this(propertyName, initialValue, null);
    }

    public String getOrigin() {
        return RemotingConstants.CLIENT_ORIGIN;
    }

}
