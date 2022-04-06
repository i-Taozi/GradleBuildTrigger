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
package com.canoo.platform.remoting.client;

import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.MAINTAINED;

@API(since = "0.x", status = MAINTAINED)
public class ControllerInitalizationException extends RuntimeException {

    private static final long serialVersionUID = 4212442538247238882L;

    public ControllerInitalizationException() {
    }

    public ControllerInitalizationException(String message) {
        super(message);
    }

    public ControllerInitalizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ControllerInitalizationException(Throwable cause) {
        super(cause);
    }
}
