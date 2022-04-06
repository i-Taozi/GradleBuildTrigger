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
package com.canoo.dp.impl.platform.projector.client.table;

import com.canoo.dp.impl.platform.projector.table.Column;
import com.canoo.platform.remoting.client.javafx.FXBinder;
import javafx.scene.control.TableColumn;

public abstract class AbstractTableColumn<S, T> extends TableColumn<S, T> {

    public AbstractTableColumn(Column column) {
        FXBinder.bind(textProperty()).to(column.titleProperty());
        FXBinder.bind(editableProperty()).to(column.editableProperty());
        FXBinder.bind(sortableProperty()).to(column.sortableProperty());
    }
}
