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
package com.canoo.platform.reactive;

import com.canoo.platform.core.functional.Subscription;
import com.canoo.platform.remoting.Property;

/**
 * A transformed property is a {@link Property} that can be created by using
 * the methods of {@link ReactiveTransormations}. Since a {@link TransformedProperty} is based on a transformation
 * the value of such a property can not be set manually.
 * @param <T> type of the property
 */
public interface TransformedProperty<T> extends Property<T>, Subscription {

}
