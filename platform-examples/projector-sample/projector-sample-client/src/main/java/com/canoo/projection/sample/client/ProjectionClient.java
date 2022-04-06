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
package com.canoo.projection.sample.client;

import com.canoo.dp.impl.platform.projector.client.base.ClientActionSupport;
import com.canoo.dp.impl.platform.projector.client.base.DefaultClientActionSupport;
import com.canoo.dp.impl.platform.projector.client.projection.Projector;
import com.canoo.platform.remoting.client.ClientContext;
import com.canoo.platform.remoting.client.javafx.DolphinPlatformApplication;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.MalformedURLException;
import java.net.URL;

public class ProjectionClient extends DolphinPlatformApplication {
    @Override
    protected URL getServerEndpoint() throws MalformedURLException {
        return new URL("http://localhost:8080/dolphin");
    }

    @Override
    protected void start(final Stage primaryStage, final ClientContext clientContext) throws Exception {
        ClientActionSupport actionSupport = new DefaultClientActionSupport();
        Projector projector = new Projector(clientContext, actionSupport);
        projector.create("SimpleFormController").handle((p, e) -> {
            if(e != null) {
                e.printStackTrace();
            }
            if(p != null) {
                Platform.runLater(() -> primaryStage.setScene(new Scene(p)));
                primaryStage.show();
            }
            return null;
        });
    }
}
