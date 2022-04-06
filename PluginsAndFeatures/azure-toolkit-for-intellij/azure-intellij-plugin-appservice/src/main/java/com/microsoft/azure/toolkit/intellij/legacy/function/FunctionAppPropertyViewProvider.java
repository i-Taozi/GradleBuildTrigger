/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.legacy.function;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.microsoft.azure.toolkit.intellij.appservice.actions.OpenAppServicePropertyViewAction;
import com.microsoft.azure.toolkit.intellij.legacy.webapp.WebAppBasePropertyViewProvider;
import org.jetbrains.annotations.NotNull;

public class FunctionAppPropertyViewProvider extends WebAppBasePropertyViewProvider {
    public static final String TYPE = "FUNCTION_APP_PROPERTY";

    @Override
    protected String getType() {
        return TYPE;
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        final String subscriptionId = virtualFile.getUserData(OpenAppServicePropertyViewAction.SUBSCRIPTION_ID);
        final String functionAppId = virtualFile.getUserData(OpenAppServicePropertyViewAction.RESOURCE_ID);
        return FunctionAppPropertyView.create(project, subscriptionId, functionAppId, virtualFile);
    }
}
