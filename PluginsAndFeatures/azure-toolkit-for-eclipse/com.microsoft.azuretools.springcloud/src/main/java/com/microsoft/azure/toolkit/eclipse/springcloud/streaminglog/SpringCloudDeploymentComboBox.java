/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.springcloud.streaminglog;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.swt.widgets.Composite;

import com.microsoft.azure.toolkit.eclipse.common.component.AzureComboBox;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudApp;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudDeploymentInstanceEntity;

public class SpringCloudDeploymentComboBox extends AzureComboBox<SpringCloudDeploymentInstanceEntity> {

    private SpringCloudApp app;

    public SpringCloudDeploymentComboBox(Composite parent) {
        super(parent, false);
    }

    public void setSpringCloudApp(final SpringCloudApp app) {
        if (Objects.equals(app, this.app)) {
            return;
        }
        this.app = app;
        if (app == null) {
            this.clear();
            return;
        }
        this.refreshItems();
    }

    @Override
    protected List<? extends SpringCloudDeploymentInstanceEntity> loadItems() {
        return Optional.ofNullable(app).map(SpringCloudApp::activeDeployment)
                .map(deployment -> deployment.entity().getInstances()).orElse(Collections.emptyList());
    }

    @Override
    protected String getItemText(Object item) {
        return item instanceof SpringCloudDeploymentInstanceEntity
                ? ((SpringCloudDeploymentInstanceEntity) item).getName()
                : super.getItemText(item);
    }

}
