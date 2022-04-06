/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.eclipse.springcloud.deployment;

import com.microsoft.azure.toolkit.eclipse.common.artifact.AzureArtifact;
import com.microsoft.azure.toolkit.eclipse.common.component.AzureArtifactComboBox;
import com.microsoft.azure.toolkit.eclipse.common.component.AzureComboBox;
import com.microsoft.azure.toolkit.eclipse.common.component.SubscriptionComboBox;
import com.microsoft.azure.toolkit.eclipse.springcloud.component.SpringCloudAppComboBox;
import com.microsoft.azure.toolkit.eclipse.springcloud.component.SpringCloudClusterComboBox;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.form.AzureForm;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.springcloud.AzureSpringCloud;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudApp;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudCluster;
import com.microsoft.azure.toolkit.lib.springcloud.config.SpringCloudAppConfig;
import com.microsoft.azure.toolkit.lib.springcloud.config.SpringCloudDeploymentConfig;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SpringCloudDeploymentConfigurationPanel extends Composite implements AzureForm<SpringCloudAppConfig> {
    private AzureArtifactComboBox selectorArtifact;
    private SubscriptionComboBox selectorSubscription;
    private SpringCloudClusterComboBox selectorCluster;
    private SpringCloudAppComboBox selectorApp;

    public SpringCloudDeploymentConfigurationPanel(Composite parent) {
        super(parent, SWT.NONE);
        $$$setupUI$$$();
        this.init();
    }

    private void init() {
        this.selectorArtifact.addValueChangedListener(this::onArtifactChanged);
        this.selectorSubscription.addValueChangedListener(this::onSubscriptionChanged);
        this.selectorCluster.addValueChangedListener(this::onClusterChanger);
        this.selectorSubscription.setRequired(true);
        this.selectorCluster.setRequired(true);
        this.selectorApp.setRequired(true);
        this.selectorArtifact.setRequired(true);
        this.selectorSubscription.refreshItems();
    }

    private void onArtifactChanged(AzureArtifact azureArtifact) {
        //TODO(andxu): disable auto build when choosing file
    }

    private void onSubscriptionChanged(Subscription subscription) {
        this.selectorCluster.setSubscription(subscription);
    }

    private void onClusterChanger(SpringCloudCluster cluster) {
        this.selectorApp.setCluster(cluster);
    }

    public synchronized void setValue(SpringCloudAppConfig appConfig) {
        AzureTaskManager.getInstance().runOnPooledThread(() -> {
            final SpringCloudCluster cluster = Azure.az(AzureSpringCloud.class).cluster(appConfig.getClusterName());
            if (Objects.nonNull(cluster) && !cluster.app(appConfig.getAppName()).exists()) {
                AzureTaskManager.getInstance().runLater(() -> {
                    this.selectorApp.addLocalItem(cluster.app(appConfig));
                });
            }
        });
        final SpringCloudDeploymentConfig deploymentConfig = appConfig.getDeployment();
        Optional.ofNullable(deploymentConfig.getArtifact()).map(a -> ((WrappedAzureArtifact) a))
            .ifPresent((a -> this.selectorArtifact.setValue(a.getArtifact())));
        Optional.ofNullable(appConfig.getSubscriptionId())
            .ifPresent((id -> this.selectorSubscription.setValue(new AzureComboBox.ItemReference<>(id, Subscription::getId))));
        Optional.ofNullable(appConfig.getClusterName())
            .ifPresent((id -> this.selectorCluster.setValue(new AzureComboBox.ItemReference<>(id, SpringCloudCluster::name))));
        Optional.ofNullable(appConfig.getAppName())
            .ifPresent((id -> this.selectorApp.setValue(new AzureComboBox.ItemReference<>(id, SpringCloudApp::name))));
    }

    @Nullable
    public SpringCloudAppConfig getValue() {
        SpringCloudAppConfig appConfig = this.selectorApp.getValue().entity().getConfig();
        if (Objects.isNull(appConfig)) {
            appConfig = SpringCloudAppConfig.builder()
                .deployment(SpringCloudDeploymentConfig.builder().build())
                .build();
        }
        this.getValue(appConfig);
        return appConfig;
    }

    public SpringCloudAppConfig getValue(SpringCloudAppConfig appConfig) {
        final SpringCloudDeploymentConfig deploymentConfig = appConfig.getDeployment();
        appConfig.setSubscriptionId(this.selectorSubscription.getValue().getId());
        appConfig.setClusterName(this.selectorCluster.getValue().name());
        appConfig.setAppName(this.selectorApp.getValue().name());
        final AzureArtifact artifact = this.selectorArtifact.getValue();
        deploymentConfig.setArtifact(new WrappedAzureArtifact(artifact));
        return appConfig;
    }

    @Override
    public List<AzureFormInput<?>> getInputs() {
        return Arrays.asList(
            this.selectorArtifact,
            this.selectorSubscription,
            this.selectorCluster,
            this.selectorApp
        );
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
    private void $$$setupUI$$$() {
        setLayout(new GridLayout(2, false));

        Label lblArtifact = new Label(this, SWT.NONE);
        GridData gd_lblArtifact = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lblArtifact.widthHint = 100;
        lblArtifact.setLayoutData(gd_lblArtifact);
        lblArtifact.setText("Artifact:");
        selectorArtifact = new AzureArtifactComboBox(this);
        selectorArtifact.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        selectorArtifact.setLabeledBy(lblArtifact);

        Label lblSubscription = new Label(this, SWT.NONE);
        lblSubscription.setText("Subscription:");
        selectorSubscription = new SubscriptionComboBox(this);
        selectorSubscription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        selectorSubscription.setLabeledBy(lblSubscription);

        Label lblService = new Label(this, SWT.NONE);
        lblService.setText("Service:");
        selectorCluster = new SpringCloudClusterComboBox(this);
        selectorCluster.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        selectorCluster.setLabeledBy(lblService);

        Label lblApp = new Label(this, SWT.NONE);
        lblApp.setText("App:");
        selectorApp = new SpringCloudAppComboBox(this);
        selectorApp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        selectorApp.setLabeledBy(lblApp);
    }
}
