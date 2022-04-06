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
package com.canoo.dp.impl.platform.projector.client.projection;

import com.canoo.dp.impl.platform.projector.base.WithDescription;
import com.canoo.platform.remoting.client.javafx.FXWrapper;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;

public class Utils {

    public static void registerTooltip(Control node, WithDescription bean) {
        Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bind(FXWrapper.wrapStringProperty(bean.descriptionProperty()));

        FXWrapper.wrapStringProperty(bean.descriptionProperty()).addListener(e -> {
            if (bean.getDescription() == null || bean.getDescription().isEmpty()) {
                node.setTooltip(null);
            } else {
                node.setTooltip(tooltip);
            }
        });

        if (bean.getDescription() == null || bean.getDescription().isEmpty()) {
            node.setTooltip(null);
        } else {
            node.setTooltip(tooltip);
        }
    }
}
