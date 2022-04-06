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
package com.canoo.dp.impl.platform.client;

import com.canoo.dp.impl.platform.core.SimpleDolphinPlatformThreadFactory;
import com.canoo.dp.impl.platform.core.SimpleUncaughtExceptionHandler;
import com.canoo.dp.impl.platform.core.http.DefaultHttpURLConnectionFactory;
import com.canoo.platform.client.ClientConfiguration;
import com.canoo.platform.core.SimpleConfiguration;
import com.canoo.platform.core.http.HttpURLConnectionFactory;
import org.apiguardian.api.API;

import java.net.CookieManager;
import java.net.CookieStore;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(since = "0.20", status = INTERNAL)
public class DefaultClientConfiguration extends SimpleConfiguration implements ClientConfiguration {

    public DefaultClientConfiguration(final Properties prop) {
        super(prop);
        setUncaughtExceptionHandler(new SimpleUncaughtExceptionHandler());
        setUiUncaughtExceptionHandler(new SimpleUncaughtExceptionHandler());

        setBackgroundExecutor(Executors.newCachedThreadPool(new SimpleDolphinPlatformThreadFactory()));
        setCookieStore(new CookieManager().getCookieStore());
        setHttpURLConnectionFactory(new DefaultHttpURLConnectionFactory());
    }

    @Override
    public <T> T getObjectProperty(final String key) {
        return (T) getInternalProperties().get(key);
    }

    @Override
    public <T> T getObjectProperty(final String key, final T defaultValue) {
        if (containsProperty(key)) {
            return getObjectProperty(key);
        } else {
            return defaultValue;
        }
    }

    @Override
    public <T> void setObjectProperty(final String key, final T value) {
        getInternalProperties().put(key, value);
    }

    @Override
    public Executor getUiExecutor() {
        return getObjectProperty(UI_EXECUTOR);
    }

    @Override
    public ExecutorService getBackgroundExecutor() {
        return getObjectProperty(BACKGROUND_EXECUTOR);
    }

    @Override
    public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return getObjectProperty(UNCAUGHT_EXCEPTION_HANDLER);
    }

    @Override
    public Thread.UncaughtExceptionHandler getUiUncaughtExceptionHandler() {
        return getObjectProperty(UI_UNCAUGHT_EXCEPTION_HANDLER);
    }

    @Override
    public CookieStore getCookieStore() {
        return getObjectProperty(COOKIE_STORE);
    }

    @Override
    public HttpURLConnectionFactory getHttpURLConnectionFactory() {
        return getObjectProperty(CONNECTION_FACTORY);
    }

    @Override
    public void setUiExecutor(final Executor executor) {
        setObjectProperty(UI_EXECUTOR, executor);
    }

    @Override
    public void setBackgroundExecutor(final ExecutorService service) {
        setObjectProperty(BACKGROUND_EXECUTOR, service);
    }

    @Override
    public void setUncaughtExceptionHandler(final Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        setObjectProperty(UNCAUGHT_EXCEPTION_HANDLER, uncaughtExceptionHandler);
    }

    @Override
    public void setUiUncaughtExceptionHandler(final Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        setObjectProperty(UI_UNCAUGHT_EXCEPTION_HANDLER, uncaughtExceptionHandler);
    }

    @Override
    public void setHttpURLConnectionFactory(final HttpURLConnectionFactory httpURLConnectionFactory) {
        setObjectProperty(CONNECTION_FACTORY, httpURLConnectionFactory);
    }

    @Override
    public void setCookieStore(final CookieStore cookieStore) {
        setObjectProperty(COOKIE_STORE, cookieStore);
    }

    @Override
    public void setStringProperty(final String key, final String value) {
        setObjectProperty(key, value);
    }

}
