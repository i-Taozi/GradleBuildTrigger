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
import com.canoo.platform.core.http.HttpHeader;
import com.canoo.platform.core.http.HttpResponse;

import java.util.Collections;
import java.util.List;

public class HttpResponseImpl<V> implements HttpResponse<V> {

    private final List<HttpHeader> headers;

    private final int statusCode;

    private final V content;

    private final long contentSize;

    public HttpResponseImpl(final List<HttpHeader> headers, final int statusCode, final V content, final long contentSize) {
        this.headers = Collections.unmodifiableList(Assert.requireNonNull(headers, "headers"));
        this.statusCode = statusCode;
        this.content = content;
        this.contentSize = contentSize;
    }

    @Override
    public List<HttpHeader> getHeaders() {
        return headers;
    }

    @Override
    public V getContent() {
        return content;
    }

    @Override
    public long getContentSize() {
        return contentSize;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }
}
