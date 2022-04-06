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

package org.waveprotocol.wave.client.doodad.selection;

import org.waveprotocol.wave.client.common.util.RgbColor;

import com.google.gwt.dom.client.Element;

/**
 * UI interface for the caret marker
 */
public interface CaretView {


  /**
   * Interface for dealing with marker doodads
   */
  public interface CaretViewFactory {

    /**
     * @return a new marker view
     */
    CaretView create();

    /**
     * Associate a marker with the given element
     *
     * Note that this is not really type safe - the E parameter is more for
     * documentation.
     */
    void setMarker(Object element, CaretView marker);
  }

  /** Update the label of a user's caret */
  void setName(String name);

  /**
   * Set marker's display color
   */
  void setColor(RgbColor color);

  /**
   * Set the current displayed IME composition state for the user's marker
   */
  void setCompositionState(String state);


  void attachToParent(Element parent);
}
