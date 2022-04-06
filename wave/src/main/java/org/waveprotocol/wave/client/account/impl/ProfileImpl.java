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

package org.waveprotocol.wave.client.account.impl;

import java.util.List;

import org.waveprotocol.wave.client.account.Profile;
import org.waveprotocol.wave.client.common.util.RgbColor;
import org.waveprotocol.wave.model.util.CollectionUtils;
import org.waveprotocol.wave.model.wave.ParticipantId;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

/**
 * A {@link Profile} which determines all properties from just a
 * {@link ParticipantId} given on construction.
 *
 * @author kalman@google.com (Benjamin Kalman)
 * @author pablojan@gmail.com (Pablo Ojanguren)
 *
 */
public final class ProfileImpl implements Profile {

  private final static String ANONYMOUS_NAME = "Anonymous";

  private static String capitalize(String s) {
    return s.isEmpty() ? s : (Character.toUpperCase(s.charAt(0))) + s.substring(1);
  }

  private static String buildName(ParticipantId id) {

    List<String> names = CollectionUtils.newArrayList();
    String nameWithoutDomain = id.getName();

    if (id.isAnonymous()) {
      return ANONYMOUS_NAME;
    } else if (nameWithoutDomain != null && !nameWithoutDomain.isEmpty()) {

      // Include empty names from fragment, so split with a -ve.
      for (String fragment : nameWithoutDomain.split("[._]", -1)) {
        if (!fragment.isEmpty()) {
          names.add(capitalize(fragment));
        }
      }
      // ParticipantId normalization implies names can not be empty.
      assert !names.isEmpty();
      return Joiner.on(' ').join(names);
    } else {
      return "";
    }
  }


  private final ParticipantId id;
  private final AbstractProfileManager manager;

  private String name = "";
  private String imageUrl = null;
  private String shortName = "";
  private String email;
  private String locale;
  private final RgbColor color;

  public ProfileImpl(ParticipantId id, AbstractProfileManager manager, RgbColor color) {
    this.id = id;
    this.shortName = buildName(id);
    this.name = buildName(id);
    this.manager = manager;
    this.color = color;
  }

  @Override
  public void update(Profile profile) {

    Preconditions.checkArgument(id.equals(profile.getParticipantId()));

    imageUrl = profile.getImageUrl() != null ? profile.getImageUrl() : null;
    email = profile.getEmail();
    name = profile.getName();
    shortName = profile.getParticipantId().getName();
    locale = profile.getLocale();

  }

  @Override
  public ParticipantId getParticipantId() {
    return id;
  }

  @Override
  public String getAddress() {
    return id.getAddress();
  }


  @Override
  public String getImageUrl() {
    return imageUrl;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getShortName() {
    return shortName;
  }


  @Override
  public String toString() {
    return "ProfileImpl [id=" + id + ", "+name+ "]";
  }

  @Override
  public void setName(String name) {
    if (this.name.equals(name)) return;
    this.name = name;
    manager.updateProfile(this);
  }


  @Override
  public boolean isCurrentSessionProfile() {
    return manager.getCurrentParticipantId().equals(id);
  }

  @Override
  public boolean getAnonymous() {
    return id.isAnonymous();
  }

  @Override
  public RgbColor getColor() {
    return color;
  }

  @Override
  public void trackActivity(String sessionId, double timestamp) {
    // TODO implement, move track activity logic from session to profile
  }

  @Override
  public void trackActivity(String sessionId) {
    // TODO implement, move track activity logic from session to profile
  }

  @Override
  public String getEmail() {
    return email;
  }

  @Override
  public String getLocale() {
    return locale;
  }

}
