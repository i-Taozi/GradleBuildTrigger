/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.waveprotocol.wave.client.account;

import org.waveprotocol.wave.client.common.util.RgbColor;
import org.waveprotocol.wave.model.wave.ParticipantId;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * Profile information for a participant.
 * <p>
 * A profile keeps the activity state of the user
 * in the current wave as long as the color representing her/him.
 * <p>
 * This class is intended to be shared across platforms. Not UI
 * components must be kept here.
 *
 * @author kalman@google.com (Benjamin Kalman)
 * @author pablojan@gmail.com (Pablo Ojanguren)
 */
@JsType(namespace = "swell", name = "Profile")
public interface Profile {

  /**
   * @return the participant id for this profile
   */
  @JsProperty
  ParticipantId getParticipantId();

  @JsIgnore
  void update(Profile profile);

  /**
   * @return the address for this profile, same as {@link #getParticipantId()}
   */
  @JsProperty
  String getAddress();

  /**
   * @return the participant's full name
   */
  @JsProperty
  String getName();

  /**
   * @return the participant's short name
   */
  @JsProperty
  String getShortName();

  /**
   * @return the URL of a participant's avatar image
   */
  @JsProperty
  String getImageUrl();

  /**
   * Set a name for the profile, the change it is not persisted
   * @param name
   */
  void setName(String name);

  /**
   * @return true iff it is the logged in user
   */
  boolean isCurrentSessionProfile();

  @JsProperty
  boolean getAnonymous();

  @JsProperty
  RgbColor getColor();

  @JsIgnore
  void trackActivity(String sessionId, double timestamp);

  @JsIgnore
  void trackActivity(String sessionId);

  @JsProperty
  String getEmail();

  @JsProperty
  String getLocale();

}
