/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.intellij.common.component.resourcegroup;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.toolkit.intellij.common.AzureTextInput;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.resource.AzureGroup;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class ResourceGroupNameTextField extends AzureTextInput {

    private static final Pattern PATTERN = Pattern.compile("^[-\\w._()]+$");
    private static final String INVALID_CHARACTERS = "Resource group names only allow alphanumeric characters, periods, underscores, hyphens and parenthesis " +
        "and cannot end in a period.";
    public static final String INVALID_LENGTH = "Resource group names only allow up to 90 characters.";
    public static final String CONFLICT_NAME = "A resource group with the same name already exists in the selected subscription %s";
    @Setter
    private Subscription subscription;

    public ResourceGroupNameTextField() {
        super();
        this.addValidator(this::doValidateValue);
        this.setRequired(true);
    }

    public AzureValidationInfo doValidateValue() {
        final String value = this.getValue();
        // validate length
        final int maxLength = 90;
        if (StringUtils.length(value) < 1) {
            return AzureValidationInfo.error("The value must not be empty.", this);
        } else if (StringUtils.length(value) > maxLength) {
            return AzureValidationInfo.error(INVALID_LENGTH, this);
        }
        // validate special character
        if (value.endsWith(".") || !PATTERN.matcher(value).matches()) {
            return AzureValidationInfo.error(INVALID_CHARACTERS, this);
        }
        // validate availability
        try {
            if (!Azure.az(AzureGroup.class).checkNameAvailability(subscription.getId(), value)) {
                return AzureValidationInfo.error(String.format(CONFLICT_NAME, subscription.getName()), this);
            }
        } catch (final CloudException e) {
            return AzureValidationInfo.error(e.getMessage(), this);
        }
        return AzureValidationInfo.success(this);
    }
}
