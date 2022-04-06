/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.database.postgre.creation;

import com.google.common.base.Preconditions;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.ide.common.model.DraftResourceGroup;
import com.microsoft.azure.toolkit.intellij.common.AzureDialog;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.form.AzureForm;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessageBundle;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.common.utils.Utils;
import com.microsoft.azure.toolkit.lib.database.DatabaseServerConfig;
import com.microsoft.azure.toolkit.lib.postgre.AzurePostgreSql;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Date;
import java.util.List;

import static com.microsoft.azure.toolkit.lib.Azure.az;

public class PostgreSqlCreationDialog extends AzureDialog<DatabaseServerConfig> {
    private static final String DIALOG_TITLE = "Create Azure Database for PostgreSQL";
    private JPanel rootPanel;
    private PostgreSqlCreationBasicPanel basic;
    private PostgreSqlCreationAdvancedPanel advanced;

    private boolean advancedMode;
    private JCheckBox checkboxMode;

    public PostgreSqlCreationDialog(@Nullable Project project) {
        super(project);
        init();
    }

    @Override
    protected void init() {
        super.init();
        advanced.setVisible(false);
    }

    @Override
    public AzureForm<DatabaseServerConfig> getForm() {
        return this.advancedMode ? advanced : basic;
    }

    @Override
    protected String getDialogTitle() {
        return DIALOG_TITLE;
    }

    @Override
    protected JComponent createDoNotAskCheckbox() {
        this.checkboxMode = new JCheckBox(AzureMessageBundle.message("common.moreSetting").toString());
        this.checkboxMode.setVisible(true);
        this.checkboxMode.setSelected(false);
        this.checkboxMode.addActionListener(e -> this.toggleAdvancedMode(this.checkboxMode.isSelected()));
        return this.checkboxMode;
    }

    protected void toggleAdvancedMode(boolean advancedMode) {
        this.advancedMode = advancedMode;
        if (advancedMode) {
            advanced.setValue(basic.getValue());
        } else {
            basic.setValue(advanced.getValue());
        }
        advanced.setVisible(advancedMode);
        basic.setVisible(!advancedMode);
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return advancedMode ? advanced.getServerNameTextField() : basic.getServerNameTextField();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return rootPanel;
    }

    private void createUIComponents() {
        final DatabaseServerConfig config = getDefaultConfig();
        basic = new PostgreSqlCreationBasicPanel(config);
        advanced = new PostgreSqlCreationAdvancedPanel(config);
    }

    public static DatabaseServerConfig getDefaultConfig() {
        final String defaultNameSuffix = DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
        final DatabaseServerConfig config = new DatabaseServerConfig();
        final List<Subscription> selectedSubscriptions = az(AzureAccount.class).account().getSelectedSubscriptions();
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(selectedSubscriptions), "There are no subscriptions in your account.");
        final Subscription subscription = selectedSubscriptions.get(0);
        final DraftResourceGroup resourceGroup = new DraftResourceGroup(subscription, "rs-" + defaultNameSuffix);
        config.setSubscription(subscription);
        config.setResourceGroup(resourceGroup);
        config.setName("postgresql-" + defaultNameSuffix);
        config.setAdminName(StringUtils.EMPTY);
        config.setAdminPassword(StringUtils.EMPTY);
        config.setRegion(Utils.selectFirstOptionIfCurrentInvalid("region",
            az(AzurePostgreSql.class).forSubscription(subscription.getId()).listSupportedRegions(),
            Region.US_EAST));
        config.setVersion("11"); // default to 11
        return config;
    }
}
