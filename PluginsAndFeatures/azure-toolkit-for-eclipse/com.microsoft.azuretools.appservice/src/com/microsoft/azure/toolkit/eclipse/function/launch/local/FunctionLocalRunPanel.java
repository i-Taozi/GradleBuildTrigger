/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.eclipse.function.launch.local;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.microsoft.azure.toolkit.eclipse.common.component.AzureComboBox;
import com.microsoft.azure.toolkit.eclipse.common.component.AzureFileInput;
import com.microsoft.azure.toolkit.eclipse.common.form.AzureFormPanel;
import com.microsoft.azure.toolkit.eclipse.function.launch.model.FunctionLocalRunConfiguration;
import com.microsoft.azure.toolkit.eclipse.function.ui.FunctionProjectComboBox;
import com.microsoft.azure.toolkit.eclipse.function.utils.FunctionUtils;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;

public class FunctionLocalRunPanel extends Composite implements AzureFormPanel<FunctionLocalRunConfiguration> {
    private FunctionProjectComboBox cbProject;
    private AzureFileInput txtFunctionCli;
    private AzureFileInput txtLocalSettings;
    /**
     * Create the composite.
     * @param parent
     * @param style
     */

    public FunctionLocalRunPanel(Composite parent, int style) {
        super(parent, style);

        Composite comp = this;
        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(comp);
        comp.setLayout(new GridLayout(2, false));

        Label lblProject = new Label(comp, SWT.NONE);
        lblProject.setText("Project:");
        GridDataFactory.swtDefaults().applyTo(lblProject);

        cbProject = new FunctionProjectComboBox(comp);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(cbProject);
        cbProject.setRequired(true);
        cbProject.setLabeledBy(lblProject);
        cbProject.addValueChangedListener(this::setLocalSettingsJsonByProject);

        Label lblFunctionCli = new Label(comp, SWT.NONE);
        lblFunctionCli.setText("Function CLI:");

        txtFunctionCli = new AzureFileInput(comp, SWT.NONE);
        txtFunctionCli.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtFunctionCli.setRequired(true);
        txtFunctionCli.setLabeledBy(lblFunctionCli);

        Label lblLocalSettings = new Label(comp, SWT.NONE);
        lblLocalSettings.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblLocalSettings.setText("Local settings:");

        txtLocalSettings = new AzureFileInput(comp, SWT.NONE);
        txtLocalSettings.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtLocalSettings.setRequired(false);
        txtLocalSettings.setLabeledBy(lblLocalSettings);

        cbProject.refreshItems();
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    @Override
    public void setValue(FunctionLocalRunConfiguration config) {
        if (StringUtils.isNotBlank(config.getProjectName())) {
            this.cbProject.setValue(new AzureComboBox.ItemReference<>(config.getProjectName(), IJavaElement::getElementName));
        }
        if (StringUtils.isNotBlank(config.getFunctionCliPath())) {
            txtFunctionCli.setValue(config.getFunctionCliPath());
        } else {
            try {
                txtFunctionCli.setValue(FunctionUtils.getFuncPath());
            } catch (IOException | InterruptedException e) {
                AzureMessager.getMessager().warning("Cannot find function core tools due to error:" + e.getMessage());
            }
        }

        if (StringUtils.isNotBlank(config.getLocalSettingsJsonPath())) {
            txtLocalSettings.setValue(config.getLocalSettingsJsonPath());
        }
    }

    @Override
    public FunctionLocalRunConfiguration getValue() {
        FunctionLocalRunConfiguration config = new FunctionLocalRunConfiguration();
        if (cbProject.getValue() != null) {
            config.setProjectName(cbProject.getValue().getElementName());
        }
        if (StringUtils.isNotBlank(txtFunctionCli.getValue())) {
            config.setFunctionCliPath(txtFunctionCli.getValue());
        }
        if (StringUtils.isNotBlank(txtLocalSettings.getValue())) {
            config.setLocalSettingsJsonPath(txtLocalSettings.getValue());
        }
        return config;
    }

    @Override
    public List<AzureFormInput<?>> getInputs() {
        return Arrays.asList(this.cbProject, this.txtFunctionCli, this.txtLocalSettings);
    }

    private void setLocalSettingsJsonByProject(final IJavaProject project) {
        if (project == null) {
            return;
        }
        final IFile file = project.getProject().getFile("local.settings.json");
        if (file.exists() && !this.txtLocalSettings.isUserInput()) {
            this.txtLocalSettings.setValue(file.getLocation().toFile().toString());
        }
    }
}
