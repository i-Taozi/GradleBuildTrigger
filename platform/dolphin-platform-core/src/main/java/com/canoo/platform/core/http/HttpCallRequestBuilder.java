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
package com.canoo.platform.core.http;

import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.platform.core.DolphinRuntimeException;
import org.apiguardian.api.API;

import java.io.UnsupportedEncodingException;

import static com.canoo.dp.impl.platform.core.http.HttpHeaderConstants.CHARSET;
import static com.canoo.dp.impl.platform.core.http.HttpHeaderConstants.RAW_MIME_TYPE;
import static com.canoo.dp.impl.platform.core.http.HttpHeaderConstants.TEXT_MIME_TYPE;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(since = "0.x", status = EXPERIMENTAL)
public interface HttpCallRequestBuilder {

    HttpCallRequestBuilder withHeader(String name, String content);

    default HttpCallResponseBuilder withContent(final byte[] content) {
        return withContent(content, RAW_MIME_TYPE);
    }

    HttpCallResponseBuilder withContent(byte[] content, String contentType);

    default HttpCallResponseBuilder withContent(final String content) {
        return withContent(content, TEXT_MIME_TYPE);
    }

    default HttpCallResponseBuilder withContent(final String content, final String contentType) {
        Assert.requireNonNull(content, "content");
        withHeader( "charset", CHARSET);
        try {
            return withContent(content.getBytes(CHARSET), contentType);
        } catch (UnsupportedEncodingException e) {
            throw new DolphinRuntimeException("Encoding error", e);
        }
    }

    <I> HttpCallResponseBuilder withContent(I content);

    HttpCallResponseBuilder withoutContent();
}
