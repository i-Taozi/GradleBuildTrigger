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
package com.canoo.dp.impl.server.legacy;

import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.dp.impl.remoting.legacy.commands.InterruptLongPollCommand;
import com.canoo.dp.impl.remoting.legacy.communication.Codec;
import com.canoo.dp.impl.remoting.legacy.communication.Command;
import com.canoo.dp.impl.server.legacy.action.CreatePresentationModelAction;
import com.canoo.dp.impl.server.legacy.action.DeletePresentationModelAction;
import com.canoo.dp.impl.server.legacy.action.DolphinServerAction;
import com.canoo.dp.impl.server.legacy.action.StoreAttributeAction;
import com.canoo.dp.impl.server.legacy.action.StoreValueChangeAction;
import com.canoo.dp.impl.server.legacy.communication.ActionRegistry;
import com.canoo.dp.impl.server.legacy.communication.CommandHandler;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(since = "0.x", status = INTERNAL)
public class ServerConnector {

    private static final Logger LOG = LoggerFactory.getLogger(ServerConnector.class);

    private final ActionRegistry registry = new ActionRegistry();

    private final List<DolphinServerAction> dolphinServerActions = new ArrayList<DolphinServerAction>();

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    @Deprecated
    private Codec codec;

    private ServerModelStore serverModelStore;

    /**
     * doesn't fail on missing commands
     **/
    public List<Command> receive(final Command command) {

        Assert.requireNonNull(command, "command");
        LOG.trace("Received command of type {}", command.getClass().getSimpleName());
        List<Command> response = new LinkedList();// collecting parameter pattern

        if (!(command instanceof InterruptLongPollCommand)) {// signal commands must not update thread-confined state
            for (DolphinServerAction it : dolphinServerActions) {
                it.setDolphinResponse(response);// todo: can be deleted as soon as all action refer to the SMS
            }

            serverModelStore.setCurrentResponse(response);
        }


        List<CommandHandler> actions = registry.getActionsFor(command.getClass());
        if (actions.isEmpty()) {
            LOG.warn("There is no server action registered for received command type {}, known commands types are {}", command.getClass().getSimpleName(), registry.getActions().keySet());
            return response;
        }

        // copying the list of actions allows an Action to unregister itself
        // avoiding ConcurrentModificationException to be thrown by the loop
        List<CommandHandler> actionsCopy = new ArrayList<CommandHandler>();
        actionsCopy.addAll(actions);
        try {
            for (CommandHandler action : actionsCopy) {
                action.handleCommand(command, response);
            }

        } catch (Exception exception) {
            throw exception;
        }

        return response;
    }

    public void register(final DolphinServerAction action) {
        action.setServerModelStore(serverModelStore);
        dolphinServerActions.add(action);
        action.registerIn(registry);
    }

    public void registerDefaultActions() {
        if (initialized.getAndSet(true)) {
            LOG.warn("Attempt to initialize default actions more than once!");
            return;
        }
        register(new StoreValueChangeAction());
        register(new StoreAttributeAction());
        register(new CreatePresentationModelAction());
        register(new DeletePresentationModelAction());
    }

    @Deprecated
    public Codec getCodec() {
        return codec;
    }

    @Deprecated
    public void setCodec(final Codec codec) {
        this.codec = codec;
    }

    public void setServerModelStore(final ServerModelStore serverModelStore) {
        this.serverModelStore = serverModelStore;
    }

    public ActionRegistry getRegistry() {
        return registry;
    }

    @Deprecated
    public int getRegistrationCount() {
        return dolphinServerActions.size();
    }

}
