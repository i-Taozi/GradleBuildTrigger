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

import com.canoo.dp.impl.remoting.legacy.communication.ValueChangedCommand;
import com.canoo.dp.impl.server.legacy.ServerAttribute;
import com.canoo.dp.impl.server.legacy.communication.ActionRegistry;
import com.canoo.dp.impl.server.legacy.communication.CommandHandler;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(since = "0.x", status = INTERNAL)
public class StoreValueChangeAction extends DolphinServerAction {

    private static final Logger LOG = LoggerFactory.getLogger(StoreValueChangeAction.class);

    public void registerIn(final ActionRegistry registry) {
        registry.register(ValueChangedCommand.class, new CommandHandler<ValueChangedCommand>() {
            @Override
            public void handleCommand(final ValueChangedCommand command, final List response) {
                final ServerAttribute attribute = getServerModelStore().findAttributeById(command.getAttributeId());
                if (attribute != null) {
                    attribute.silently(new Runnable() {
                        @Override
                        public void run() {
                            attribute.setValue(command.getNewValue());
                        }

                    });
                } else {
                    LOG.error("S: cannot find attribute with id '{}' to change value from to '{}'.", command.getAttributeId(), command.getNewValue());
                }
            }
        });
    }
}
