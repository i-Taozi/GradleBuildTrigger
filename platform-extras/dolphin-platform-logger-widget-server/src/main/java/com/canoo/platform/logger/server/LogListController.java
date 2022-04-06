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
package com.canoo.platform.logger.server;

import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.platform.logger.model.LogEntryBean;
import com.canoo.platform.logger.model.LogListBean;
import com.canoo.platform.logger.model.LoggerSearchRequest;
import com.canoo.platform.logger.server.service.LoggerRepository;
import com.canoo.platform.logging.spi.LogMessage;
import com.canoo.platform.remoting.BeanManager;
import com.canoo.platform.remoting.server.RemotingAction;
import com.canoo.platform.remoting.server.RemotingController;
import com.canoo.platform.remoting.server.RemotingModel;
import org.slf4j.event.Level;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.canoo.platform.logger.LoggerRemotingConstants.LOG_LIST_CONTROLLER_NAME;
import static com.canoo.platform.logger.LoggerRemotingConstants.UPDATE_ACTION;

@RemotingController(LOG_LIST_CONTROLLER_NAME)
public class LogListController {

    private final BeanManager beanManager;

    private final LoggerRepository repository;

    @RemotingModel
    private LogListBean model;

    @Inject
    public LogListController(final BeanManager beanManager, final LoggerRepository repository) {
        this.beanManager = Assert.requireNonNull(beanManager, "beanManager");
        this.repository = Assert.requireNonNull(repository, "repository");
    }

    protected final void update(final LoggerSearchRequest request) {
        Assert.requireNonNull(request, "request");

        model.getEntries().clear();
        repository.search(request).
                map(m -> convert(m)).
                forEach(b -> model.getEntries().add(b));
    }

    @RemotingAction(UPDATE_ACTION)
    public void update() {
        final ZonedDateTime startDate = ZonedDateTime.now().minusDays(1);
        final ZonedDateTime endDateTime = ZonedDateTime.now();
        final Set<Level> level = new HashSet<>(Arrays.asList(Level.values()));
        final int maxResults = 100;
        final LoggerSearchRequest request = new LoggerSearchRequest(startDate, endDateTime, level, maxResults);
        update(request);
    }

    private LogEntryBean convert(final LogMessage logMessage) {
        Assert.requireNonNull(logMessage, "logMessage");

        final LogEntryBean bean = beanManager.create(LogEntryBean.class);
        bean.setLoggerName(logMessage.getLoggerName());
        bean.setLogLevel(logMessage.getLevel());
        bean.setMessage(logMessage.getMessage());
        bean.setLogTimestamp(logMessage.getTimestamp());
        bean.setExceptionClass(logMessage.getExceptionClass());
        bean.setExceptionMessage(logMessage.getExceptionMessage());
        bean.setThreadName(logMessage.getThreadName());
        bean.getMarker().addAll(logMessage.getMarker());

        return bean;
    }
}
