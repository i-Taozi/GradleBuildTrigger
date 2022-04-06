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
package com.canoo.dp.impl.platform.client.http;

import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.dp.impl.platform.core.http.ConnectionUtils;
import com.canoo.dp.impl.platform.core.http.HttpClientConnection;
import com.canoo.dp.impl.platform.core.http.HttpHeaderImpl;
import com.canoo.platform.client.ClientConfiguration;
import com.canoo.platform.core.DolphinRuntimeException;
import com.canoo.platform.core.functional.Promise;
import com.canoo.platform.core.http.ByteArrayProvider;
import com.canoo.platform.core.http.ConnectionException;
import com.canoo.platform.core.http.HttpCallResponseBuilder;
import com.canoo.platform.core.http.HttpException;
import com.canoo.platform.core.http.HttpHeader;
import com.canoo.platform.core.http.HttpResponse;
import com.canoo.platform.core.http.HttpURLConnectionHandler;
import com.google.gson.Gson;
import org.apiguardian.api.API;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.canoo.dp.impl.platform.core.http.HttpHeaderConstants.ACCEPT_CHARSET_HEADER;
import static com.canoo.dp.impl.platform.core.http.HttpHeaderConstants.ACCEPT_HEADER;
import static com.canoo.dp.impl.platform.core.http.HttpHeaderConstants.CHARSET;
import static com.canoo.dp.impl.platform.core.http.HttpHeaderConstants.JSON_MIME_TYPE;
import static org.apiguardian.api.API.Status.INTERNAL;

@API(since = "0.x", status = INTERNAL)
public class HttpCallResponseBuilderImpl implements HttpCallResponseBuilder {

    private final HttpClientConnection connection;

    private final Gson gson;

    private final AtomicBoolean handled = new AtomicBoolean(false);

    private final List<HttpURLConnectionHandler> requestHandlers;

    private final List<HttpURLConnectionHandler> responseHandlers;

    private final ByteArrayProvider dataProvider;

    private final ClientConfiguration configuration;

    public HttpCallResponseBuilderImpl(final HttpClientConnection connection, final ByteArrayProvider dataProvider, final Gson gson, final List<HttpURLConnectionHandler> requestHandlers, final List<HttpURLConnectionHandler> responseHandlers, final ClientConfiguration configuration) {
        this.connection = Assert.requireNonNull(connection, "connection");
        this.dataProvider = Assert.requireNonNull(dataProvider, "dataProvider");
        this.gson = Assert.requireNonNull(gson, "gson");
        this.configuration = Assert.requireNonNull(configuration, "configuration");

        Assert.requireNonNull(requestHandlers, "requestHandlers");
        this.requestHandlers = Collections.unmodifiableList(requestHandlers);

        Assert.requireNonNull(responseHandlers, "responseHandlers");
        this.responseHandlers = Collections.unmodifiableList(responseHandlers);
    }

    @Override
    public Promise<HttpResponse<InputStream>, HttpException> streamBytes() {
        return createExecutor();
    }

    @Override
    public Promise<HttpResponse<ByteArrayProvider>, HttpException> readBytes() {
        final ResponseContentConverter<ByteArrayProvider> converter = b -> new SimpleByteArrayProvider(b);
        return createExecutor(converter);
    }

    @Override
    public Promise<HttpResponse<String>, HttpException> readString() {
        connection.addRequestHeader(new HttpHeaderImpl(ACCEPT_CHARSET_HEADER, CHARSET));

        final ResponseContentConverter<String> converter = b -> new String(b, CHARSET);
        return createExecutor(converter);
    }

    @Override
    public <R> Promise<HttpResponse<R>, HttpException> readObject(final Class<R> responseType) {
        Assert.requireNonNull(responseType, "responseType");

        connection.addRequestHeader(new HttpHeaderImpl(ACCEPT_CHARSET_HEADER, CHARSET));
        connection.addRequestHeader(new HttpHeaderImpl(ACCEPT_HEADER, JSON_MIME_TYPE));

        final ResponseContentConverter<R> converter = b -> gson.fromJson(new String(b, CHARSET), responseType);
        return createExecutor(converter);
    }

    @Override
    public Promise<HttpResponse<Void>, HttpException> withoutResult() {
        final ResponseContentConverter<Void> converter = b -> null;
        return createExecutor(converter);
    }

    @Override
    public Promise<HttpResponse<ByteArrayProvider>, HttpException> readBytes(final String contentType) {
        Assert.requireNonNull(contentType, "contentType");

        connection.addRequestHeader(new HttpHeaderImpl(ACCEPT_HEADER, contentType));
        return readBytes();
    }

    @Override
    public Promise<HttpResponse<String>, HttpException> readString(final String contentType) {
        Assert.requireNonNull(contentType, "contentType");

        connection.addRequestHeader(new HttpHeaderImpl(ACCEPT_HEADER, contentType));
        return readString();
    }

    private <R> Promise<HttpResponse<R>, HttpException> createExecutor(final ResponseContentConverter<R> converter) {
        return new HttpCallExecutorImpl<>(configuration, () -> handleRequest(converter));
    }

    private Promise<HttpResponse<InputStream>, HttpException> createExecutor() {
        return new HttpCallExecutorImpl<>(configuration, () -> handleRequest());
    }

    private <R> HttpResponse<R> handleRequest(final ResponseContentConverter<R> converter) throws HttpException {
        final HttpResponse<InputStream> response = handleRequest();
        try {
            final InputStream inputStream = response.getContent();
            final R content = converter.convert(ConnectionUtils.readContent(inputStream));
            return new HttpResponseImpl<>(response.getHeaders(), response.getStatusCode(), content, response.getContentSize());
        } catch (IOException e) {
            throw new ConnectionException("No response from server", e);
        } catch (Exception e) {
            throw new HttpException("Can not handle response", e);
        }
    }

    private HttpResponse<InputStream> handleRequest() throws HttpException {
        if (handled.get()) {
            throw new DolphinRuntimeException("Http call already handled");
        }
        handled.set(true);

        requestHandlers.forEach(h -> h.handle(connection.getConnection()));
        final byte[] rawBytes = dataProvider.get();
        try {
            connection.writeRequestContent(rawBytes);
        } catch (final IOException e) {
            throw new ConnectionException("Can not connect to server", e);
        }

        try {
            int responseCode = connection.readResponseCode();
            responseHandlers.forEach(h -> h.handle(connection.getConnection()));
            final List<HttpHeader> headers = connection.getResponseHeaders();
            return new HttpResponseImpl<>(headers, responseCode, connection.getContentStream(), connection.getContentSize());
        } catch (IOException e) {
            throw new ConnectionException("No response from server", e);
        } catch (Exception e) {
            throw new HttpException("Can not handle response", e);
        }
    }

}
