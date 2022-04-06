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

import static org.apiguardian.api.API.Status.MAINTAINED;

/**
 * This exception is thrown if an error occurs in the protocol (request / response body)
 *
 * @author Hendrik Ebbers
 */
@API(since = "0.x", status = MAINTAINED)
public class DolphinRemotingException extends Exception {

    private static final long serialVersionUID = 1934440187016337212L;

    /**
     * Constructor
     * @param message the detailed message
     */
    public DolphinRemotingException(String message) {
        super(message);
    }

    /**
     * Constructor
     * @param message the detailed message
     * @param cause the cause
     */
    public DolphinRemotingException(String message, Throwable cause) {
        super(message, cause);
    }

}
