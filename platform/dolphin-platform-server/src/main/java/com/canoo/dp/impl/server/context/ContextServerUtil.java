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

import com.canoo.dp.impl.platform.core.Assert;
import com.canoo.dp.impl.platform.core.context.ContextImpl;
import com.canoo.platform.core.context.Context;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class ContextServerUtil {

    private ContextServerUtil() {}

    public static Context createPortTag(final HttpServletRequest request) {
        Assert.requireNonNull(request, "request");
        return new ContextImpl("port", String.valueOf(request.getServerPort()));
    }

    public static Context createMethodTag(final HttpServletRequest request) {
        Assert.requireNonNull(request, "request");
        final String method = Optional.ofNullable(request.getMethod()).orElse("UNKNOWN");
        return new ContextImpl("method", method.isEmpty() ? "UNKNOWN" : method);
    }

    public static Context createUriTag(final HttpServletRequest request) {
        Assert.requireNonNull(request, "request");
        final String uri = Optional.ofNullable(request.getRequestURI()).orElse("/");
        return new ContextImpl("uri", uri.isEmpty() ? "root" : uri);
    }

    public static Context createContextPathTag(final HttpServletRequest request) {
        Assert.requireNonNull(request, "request");
        final String path = Optional.ofNullable(request.getContextPath()).orElse("/");
        return new ContextImpl("contextPath", path.isEmpty() ? "" : path);
    }

}
