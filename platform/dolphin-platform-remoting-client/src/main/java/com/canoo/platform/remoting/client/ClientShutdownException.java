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

/**
 * This exception is thrown when a client can't disconnect
 */
@API(since = "0.x", status = MAINTAINED)
public class ClientShutdownException extends Exception {

    private static final long serialVersionUID = 8453749162802045073L;

    /**
     * constructor
     * @param message the error message
     */
    public ClientShutdownException(String message) {
        super(message);
    }

    /**
     * constructor
     * @param message the error message
     * @param cause the cause
     */
    public ClientShutdownException(String message, Throwable cause) {
        super(message, cause);
    }

}
