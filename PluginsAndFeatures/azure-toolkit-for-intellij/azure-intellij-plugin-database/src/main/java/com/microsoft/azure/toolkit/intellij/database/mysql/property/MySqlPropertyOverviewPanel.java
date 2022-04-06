/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.database.mysql.property;

import com.microsoft.azure.toolkit.intellij.common.component.TextFieldUtils;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.mysql.MySqlServer;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.util.Optional;

import static com.microsoft.azure.toolkit.lib.Azure.az;

public class MySqlPropertyOverviewPanel extends JPanel {
    private JPanel rootPanel;

    private JTextField resourceGroupTextField;

    private JTextField serverNameTextField;

    private JTextField statusTextField;

    private JTextField serverAdminLoginNameTextField;

    private JTextField locationTextField;

    private JTextField versionTextField;

    private JTextField subscriptionTextField;

    private JTextField performanceConfigurationsTextField;

    private JTextField subscriptionIDTextField;

    private JTextField sslEnforceStatusTextField;

    MySqlPropertyOverviewPanel() {
        super();
        TextFieldUtils.disableTextBoard(resourceGroupTextField, serverNameTextField, statusTextField, serverAdminLoginNameTextField,
            locationTextField, versionTextField, subscriptionTextField, performanceConfigurationsTextField,
            subscriptionIDTextField, sslEnforceStatusTextField);
        TextFieldUtils.makeTextOpaque(resourceGroupTextField, serverNameTextField, statusTextField, serverAdminLoginNameTextField,
            locationTextField, versionTextField, subscriptionTextField, performanceConfigurationsTextField,
            subscriptionIDTextField, sslEnforceStatusTextField);
    }

    public void setFormData(MySqlServer server) {
        final Subscription subscription = az(AzureAccount.class).account().getSubscription(server.getSubscriptionId());
        if (subscription != null) {
            subscriptionTextField.setText(subscription.getName());
        }
        resourceGroupTextField.setText(server.getResourceGroupName());
        statusTextField.setText(server.getStatus());
        locationTextField.setText(Optional.ofNullable(server.getRegion()).map(Region::getLabel).orElse(""));
        subscriptionIDTextField.setText(server.getSubscriptionId());
        serverNameTextField.setText(StringUtils.firstNonBlank(server.getFullyQualifiedDomainName(), server.getName()));
        serverNameTextField.setCaretPosition(0);
        serverAdminLoginNameTextField.setText(server.getAdminName() + "@" + server.name());
        serverAdminLoginNameTextField.setCaretPosition(0);
        versionTextField.setText(server.getVersion());
        final String skuTier = server.getSkuTier();
        final int skuCapacity = server.getVCore();
        final int storageGB = server.getStorageInMB() / 1024;
        final String performanceConfigurations = skuTier + ", " + skuCapacity + " vCore(s), " + storageGB + " GB";
        performanceConfigurationsTextField.setText(performanceConfigurations);
        sslEnforceStatusTextField.setText(server.getSslEnforceStatus());
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        rootPanel.setVisible(visible);
    }

}
