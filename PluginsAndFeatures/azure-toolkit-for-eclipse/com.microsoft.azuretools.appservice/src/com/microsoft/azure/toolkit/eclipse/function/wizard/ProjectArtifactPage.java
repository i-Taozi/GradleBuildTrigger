/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.eclipse.function.wizard;

import com.microsoft.azure.toolkit.eclipse.common.component.AzWizardPageWrapper;
import com.microsoft.azure.toolkit.eclipse.common.component.AzureTextInput;
import com.microsoft.azure.toolkit.ide.appservice.model.FunctionArtifactModel;
import com.microsoft.azure.toolkit.lib.common.form.AzureForm;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class ProjectArtifactPage extends AzWizardPageWrapper implements AzureForm<FunctionArtifactModel> {
    private AzureTextInput txtGroupId;
    private Composite container;
    private AzureTextInput txtArtifact;
    private AzureTextInput txtVersion;
    private AzureTextInput txtPackageName;
    private static final String PACKAGE_NAME_REGEX = "[a-zA-Z]([\\.a-zA-Z0-9_])*";
    private static final String GROUP_ARTIFACT_ID_REGEX = "[0-9a-zA-Z]([\\.a-zA-Z0-9\\-_])*";
    private static final String VERSION_REGEX = "[0-9]([\\.a-zA-Z0-9\\-_])*";

    public ProjectArtifactPage() {
        super("Artifact Page");
        setTitle("New Azure Function Project");
        setDescription("Provide the group id and artifact id");
        setControl(txtGroupId);
    }

    public void setDefaultValueFirstTime() {
        FunctionProjectPage previousPage = (FunctionProjectPage) getWizard().getPreviousPage(this);

        if (StringUtils.isBlank(txtArtifact.getValue())) {
            this.txtArtifact.setValue(previousPage.getProjectName(), true);
        }
    }

    private static void addValidator(String propertyName, AzureTextInput input, Predicate<String> validator, String description) {
        input.setRequired(true);
        input.addValidator(() -> validateProperties(propertyName, input, validator, description));
    }

    private static AzureValidationInfo validateProperties(String propertyName, AzureTextInput input, Predicate<String> validator, String description) {
        String text = input.getValue();

        if (text.isEmpty()) {
            return AzureValidationInfo.error(propertyName + " is required.", input);
        }

        if (!validator.test(text)) {
            return AzureValidationInfo.error(String.format("Invalid %s: %s, it shall %s", propertyName, text, description), input);
        }
        return AzureValidationInfo.success(input);
    }

    @Override
    public AzureForm<FunctionArtifactModel> getForm() {
        return this;
    }

    @Override
    public FunctionArtifactModel getValue() {
        FunctionArtifactModel model = new FunctionArtifactModel();
        model.setGroupId(this.txtGroupId.getText().trim());
        model.setArtifactId(this.txtArtifact.getText().trim());
        model.setVersion(this.txtVersion.getText().trim());
        model.setPackageName(this.txtPackageName.getText().trim());
        return model;
    }

    @Override
    public void setValue(@Nonnull FunctionArtifactModel value) {
        // todo: Diff user input and defualt values in SWT input components
        this.txtGroupId.setValue(value.getGroupId(), true);
        this.txtArtifact.setValue(value.getArtifactId(), true);
        this.txtVersion.setValue(value.getVersion(), true);
        this.txtPackageName.setValue(value.getPackageName(), true);
    }

    @Override
    public List<AzureFormInput<?>> getInputs() {
        return Arrays.asList(txtArtifact, txtGroupId, txtVersion, txtPackageName);
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            setDefaultValueFirstTime();

        }
    }

    @Override
    public void createControl(Composite parent) {
        container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 2;
        Label lblGroup = new Label(container, SWT.NONE);
        lblGroup.setText("Group Id:");

        txtGroupId = new AzureTextInput(container);
        txtGroupId.setText("");
        txtGroupId.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Label lblArtifact = new Label(container, SWT.NONE);
        lblArtifact.setText("Artifact Id:");
        setControl(container);

        txtArtifact = new AzureTextInput(container);
        txtArtifact.setText("");
        txtArtifact.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblVersion = new Label(container, SWT.NONE);
        lblVersion.setText("Version:");

        txtVersion = new AzureTextInput(container);
        txtVersion.setText("");
        txtVersion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblPackageName = new Label(container, SWT.NONE);
        lblPackageName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblPackageName.setText("Package Name:");

        txtPackageName = new AzureTextInput(container);
        txtPackageName.setText("");
        txtPackageName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        addValidator("group id", txtGroupId, ProjectArtifactPage::isValidGroupIdArtifactId,
                "contain only letters, numbers, the period, the underscore and start only with letters.");
        addValidator("artifact id", txtArtifact, ProjectArtifactPage::isValidGroupIdArtifactId,
                "contain only letters, numbers, the hyphen, the period, the underscore and start only with letters, numbers.");
        addValidator("version", txtVersion, ProjectArtifactPage::isValidVersion,
                "contain only letters, numbers, the hyphen, the period, the underscore and start only with numbers.");
        addValidator("package name", txtPackageName, ProjectArtifactPage::isValidJavaPackageName,
                "contain only letters, numbers, the period, the underscore and start only with letters.");
        FunctionArtifactModel defaultModel = FunctionArtifactModel.getDefaultFunctionArtifactModel();
        defaultModel.setArtifactId(StringUtils.EMPTY);
        setValue(defaultModel);
    }

    private static boolean isValidJavaPackageName(String packageName) {
        return packageName != null && packageName.matches(PACKAGE_NAME_REGEX);
    }

    private static boolean isValidVersion(String version) {
        return version != null && version.matches(VERSION_REGEX);
    }

    private static boolean isValidGroupIdArtifactId(String name) {
        return name != null && name.matches(GROUP_ARTIFACT_ID_REGEX);
    }

}