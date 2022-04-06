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
package com.canoo.dp.impl.platform.projector.client.graph;

import com.canoo.dp.impl.platform.projector.graph.GraphDataBean;
import com.canoo.dp.impl.platform.projector.graph.GraphMetadata;
import com.canoo.dp.impl.platform.projector.graph.GraphType;
import com.canoo.dp.impl.platform.projector.metadata.MetadataUtilities;
import com.canoo.platform.core.functional.Binding;
import com.canoo.platform.core.functional.Subscription;
import com.canoo.platform.remoting.client.javafx.FXBinder;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hendrikebbers on 17.10.16.
 */
public class GraphComponent extends StackPane {

    private final ObjectProperty<GraphDataBean> data = new SimpleObjectProperty<>();

    private final List<Binding> bindings = new ArrayList<>();

    private Subscription metadataSubscription;

    public GraphComponent() {
        data.addListener((obs, oldValue, newValue) -> onUpdate());
    }

    public GraphComponent(GraphDataBean data) {
        this();
        this.data.set(data);
    }

    private void onUpdate() {
        getChildren().clear();

        bindings.forEach(b -> b.unbind());
        bindings.clear();


        if(metadataSubscription != null) {
            metadataSubscription.unsubscribe();
        }
        metadataSubscription = MetadataUtilities.addListenerToMetadata(data.get(), () -> {
            onUpdate();
        });

        GraphDataBean currentBean = data.get();
        if (currentBean != null) {
            if (GraphType.PIE.equals(GraphMetadata.getGraphType(data.get()))) {
                PieChart chart = new PieChart();
                bindings.add(FXBinder.bind(chart.dataProperty().get()).to(currentBean.getValues(), valueBean -> {
                    PieChart.Data data = new PieChart.Data(valueBean.getName(), valueBean.getValue());
                    bindings.add(FXBinder.bind(data.nameProperty()).to(valueBean.nameProperty()));
                    bindings.add(FXBinder.bind(data.pieValueProperty()).to(valueBean.valueProperty()));
                    return data;
                }));
                getChildren().add(chart);
            } else {
                BarChart<String, Number> barChart = new BarChart<>(new CategoryAxis(), new NumberAxis());
                XYChart.Series<String, Number> defaulSeries = new XYChart.Series<>();
                barChart.getData().add(defaulSeries);
                bindings.add(FXBinder.bind(defaulSeries.dataProperty().get()).to(currentBean.getValues(), valueBean -> {
                    XYChart.Data<String, Number> data = new XYChart.Data<>(valueBean.getName(), valueBean.getValue());
                    bindings.add(FXBinder.bind(data.XValueProperty()).to(valueBean.nameProperty()));
                    bindings.add(FXBinder.bind(data.YValueProperty()).to(valueBean.valueProperty()));
                    return data;
                }));
                getChildren().add(barChart);
            }
        }
    }
}
