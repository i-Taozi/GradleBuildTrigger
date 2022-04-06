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
package com.canoo.platform.server.spring;

import com.canoo.dp.impl.server.spring.ClientScopeImpl;
import org.apiguardian.api.API;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.apiguardian.api.API.Status.MAINTAINED;

/**
 * A custom scope that is bound to the lifecycle of a {@link com.canoo.platform.server.client.ClientSession}
 *
 * @author Hendrik Ebbers
 */
@Scope(ClientScopeImpl.CLIENT_SCOPE)
@Documented
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@API(since = "0.x", status = MAINTAINED)
public @interface ClientScope {

    /**
     * Alias for {@link Scope#proxyMode}.
     */
    @AliasFor(annotation = Scope.class)
    ScopedProxyMode proxyMode() default ScopedProxyMode.DEFAULT;
}
