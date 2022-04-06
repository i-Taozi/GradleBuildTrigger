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
package com.canoo.dp.impl.server.context;

import com.canoo.dp.impl.remoting.commands.CallActionCommand;
import com.canoo.dp.impl.remoting.commands.CreateContextCommand;
import com.canoo.dp.impl.remoting.commands.CreateControllerCommand;
import com.canoo.dp.impl.remoting.commands.DestroyContextCommand;
import com.canoo.dp.impl.remoting.commands.DestroyControllerCommand;
import com.canoo.dp.impl.remoting.legacy.commands.InterruptLongPollCommand;
import com.canoo.dp.impl.remoting.legacy.commands.StartLongPollCommand;
import com.canoo.dp.impl.remoting.legacy.communication.Command;
import com.canoo.dp.impl.server.beans.PostConstructInterceptor;
import com.canoo.dp.impl.server.client.ClientSessionProvider;
import com.canoo.dp.impl.server.client.HttpClientSessionImpl;
import com.canoo.dp.impl.server.config.RemotingConfiguration;
import com.canoo.dp.impl.server.controller.ControllerRepository;
import com.canoo.dp.impl.server.controller.ControllerValidationException;
import com.canoo.dp.impl.server.legacy.communication.CommandHandler;
import com.canoo.dp.impl.server.scanner.DefaultClasspathScanner;
import com.canoo.impl.server.util.HttpSessionMock;
import com.canoo.platform.server.client.ClientSession;
import com.canoo.platform.server.spi.components.ManagedBeanFactory;
import org.testng.annotations.Test;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class DolphinContextTest {

    @Test
    public void testUniqueId() throws ControllerValidationException {
        //given:
        List<DolphinContext> contextList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            DolphinContext dolphinContext = createContext();
            contextList.add(dolphinContext);
        }

        //then:
        assertEquals(contextList.size(), 1000);
        while (!contextList.isEmpty()) {
            DolphinContext dolphinContext = contextList.remove(0);
            for (DolphinContext toCompare : contextList) {
                assertFalse(dolphinContext.getId().equals(toCompare.getId()));
                assertTrue(dolphinContext.hashCode() != toCompare.hashCode());
                assertFalse(dolphinContext.equals(toCompare));
            }
        }
    }

    @Test
    public void testUniqueBeanManager() throws ControllerValidationException {
        //given:
        List<DolphinContext> contextList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            DolphinContext dolphinContext = createContext();
            contextList.add(dolphinContext);
        }

        //then:
        while (!contextList.isEmpty()) {
            DolphinContext dolphinContext = contextList.remove(0);
            for (DolphinContext toCompare : contextList) {
                assertFalse(dolphinContext.getBeanManager().equals(toCompare.getBeanManager()));
            }
        }
    }

    @Test
    public void testUniqueDolphin() throws ControllerValidationException {
        //given:
        List<DolphinContext> contextList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            DolphinContext dolphinContext = createContext();
            contextList.add(dolphinContext);
        }

        //then:
        while (!contextList.isEmpty()) {
            DolphinContext dolphinContext = contextList.remove(0);
            for (DolphinContext toCompare : contextList) {
                assertFalse(dolphinContext.getServerModelStore().equals(toCompare.getServerModelStore()));
            }
        }
    }

    @Test
    public void testUniqueDolphinSession() throws ControllerValidationException {
        //given:
        List<DolphinContext> contextList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            DolphinContext dolphinContext = createContext();
            contextList.add(dolphinContext);
        }

        //then:
        while (!contextList.isEmpty()) {
            DolphinContext dolphinContext = contextList.remove(0);
            for (DolphinContext toCompare : contextList) {
                assertFalse(dolphinContext.getClientSession().equals(toCompare.getClientSession()));
            }
        }
    }

    @Test
    public void testGetterReturnValue() throws ControllerValidationException {
        //given:
        DolphinContext dolphinContext = createContext();

        //then:
        assertNotNull(dolphinContext.getId());
        assertNotNull(dolphinContext.getBeanManager());
        assertNotNull(dolphinContext.getClientSession());
        assertNotNull(dolphinContext.getServerModelStore());
    }

    @Test
    public void testNewDolphinCommands() throws ControllerValidationException {
        //given:
        DolphinContext dolphinContext = createContext();

        //then:
        Map<Class<? extends Command>, List<CommandHandler>> dolphinActions = dolphinContext.getServerConnector().getRegistry().getActions();
        assertNotNull(dolphinActions.containsKey(CreateContextCommand.class));
        assertNotNull(dolphinActions.containsKey(DestroyContextCommand.class));
        assertNotNull(dolphinActions.containsKey(CreateControllerCommand.class));
        assertNotNull(dolphinActions.containsKey(DestroyControllerCommand.class));
        assertNotNull(dolphinActions.containsKey(CallActionCommand.class));
        assertNotNull(dolphinActions.containsKey(StartLongPollCommand.class));
        assertNotNull(dolphinActions.containsKey(InterruptLongPollCommand.class));
    }

    private final DefaultClasspathScanner classpathScanner = new DefaultClasspathScanner("com.canoo.dolphin");

    private DolphinContext createContext() throws ControllerValidationException {
        final ClientSession session = new HttpClientSessionImpl(new HttpSessionMock());
        return new DolphinContext(new RemotingConfiguration(), session, new ClientSessionProvider() {
            @Override
            public ClientSession getCurrentClientSession() {
                return session;
            }
        }, new ManagedBeanFactoryMock(), new ControllerRepository(classpathScanner), v -> {});
    }

    private class ManagedBeanFactoryMock implements ManagedBeanFactory {

        @Override
        public void init(ServletContext servletContext) {

        }

        @Override
        public <T> T createDependentInstance(Class<T> cls) {
            return null;
        }

        @Override
        public <T> T createDependentInstance(Class<T> cls, PostConstructInterceptor<T> interceptor) {
            return null;
        }

        @Override
        public <T> void destroyDependentInstance(T instance, Class<T> cls) {

        }


    }
}
