/*
 * Copyright 2017 Dev Labs
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

package bg.devlabs.fullscreenvideoview;

/**
 * Contains constants used across the application.
 */
class Constants {
    // Time values
    public static final int ONE_HOUR_SECONDS = 3600;
    public static final int ONE_MINUTE_SECONDS = 60;
    public static final int ONE_SECOND_MILLISECONDS = 1000;
    public static final int ONE_HOUR_MILLISECONDS = 3600000;
    public static final int FAST_FORWARD_DURATION = 15000;
    public static final int REWIND_DURATION = 5000;
    public static final long ONE_MILLISECOND = 1000L;
    public static final int DEFAULT_CONTROLLER_TIMEOUT = 3000;
    // View tags
    public static final String VIEW_TAG_CLICKED = "view_tag:clicked";
    // Media player error codes
    public static final int MEDIA_ERROR_GENERAL = 1000;
}
