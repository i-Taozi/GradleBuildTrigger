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
package com.canoo.platform.logger.client.util;

import com.canoo.impl.dp.logging.DolphinLoggerFactory;
import com.canoo.platform.logging.spi.LogMessage;
import com.canoo.platform.core.functional.Subscription;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LogClientUtil {

    public static BoundLogList<LogMessage> createObservableListFromLocalCache() {
        final ObservableList<LogMessage> list = FXCollections.observableArrayList(DolphinLoggerFactory.getLogCache());
        final Subscription subscription = DolphinLoggerFactory.addListener(l -> {
            final List<LogMessage> currentCache = Collections.unmodifiableList(DolphinLoggerFactory.getLogCache());
            Platform.runLater(() -> {
                final List<LogMessage> toRemove = list.stream().
                        filter(e -> !currentCache.contains(l)).
                        collect(Collectors.toList());

                list.removeAll(toRemove);

                final List<LogMessage> toAdd = currentCache.stream().
                        filter(e -> !list.contains(e)).
                        collect(Collectors.toList());

                list.addAll(toAdd);
            });
        });
        return new BoundLogList(subscription, list);
    }

}
