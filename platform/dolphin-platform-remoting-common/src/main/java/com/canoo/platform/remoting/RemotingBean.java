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
package com.canoo.platform.remoting;

import org.apiguardian.api.API;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apiguardian.api.API.Status.MAINTAINED;

/**
 * Each class that is part of a Dolphin Platform model must be annotated with {@link RemotingBean}.
 * All fields of the a dolphin bean must be defined as {@link Property} or {@link ObservableList}.
 * Methods in the classes will be ignored.
 *
 * Example:
 * <code>
 *     @RemotingBean
 *     public class MyModel {
 *
 *         private Property<String> name;
 *
 *         public Property<String> getNameProperty() {
 *             return name;
 *         }
 *     }
 *
 * </code>
 *
 * @author Hendrik Ebbers
 *
 */
@Documented
@Inherited
@Retention(RUNTIME)
@Target(ElementType.TYPE)
@API(since = "0.x", status = MAINTAINED)
public @interface RemotingBean {

}
