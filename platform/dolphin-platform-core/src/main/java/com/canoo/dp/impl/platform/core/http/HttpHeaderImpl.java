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
package com.canoo.dp.impl.platform.core.http;

import com.canoo.platform.core.http.HttpHeader;

public class HttpHeaderImpl implements HttpHeader {

    private final String name;

    private final String content;

    public HttpHeaderImpl(final String name, final String content) {
        //No null checks since the HTTP version is defined as header with no / null name
        this.name = name;
        this.content = content;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return name + ":" + content;
    }
}
