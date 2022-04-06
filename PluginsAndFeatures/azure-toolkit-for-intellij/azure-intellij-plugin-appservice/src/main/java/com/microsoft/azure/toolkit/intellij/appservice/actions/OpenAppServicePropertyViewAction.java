/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.intellij.appservice.actions;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.microsoft.azure.toolkit.intellij.common.AzureFileType;
import com.microsoft.azure.toolkit.intellij.common.AzureIcons;
import com.microsoft.azure.toolkit.intellij.legacy.function.FunctionAppPropertyViewProvider;
import com.microsoft.azure.toolkit.intellij.legacy.webapp.DeploymentSlotPropertyViewProvider;
import com.microsoft.azure.toolkit.intellij.legacy.webapp.WebAppPropertyViewProvider;
import com.microsoft.azure.toolkit.lib.appservice.service.impl.FunctionApp;
import com.microsoft.azure.toolkit.lib.appservice.service.impl.WebApp;
import com.microsoft.azure.toolkit.lib.appservice.service.impl.WebAppDeploymentSlot;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.tooling.msservices.serviceexplorer.AzureIconSymbol;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

// todo: migrate to new property view framework
@Deprecated
public class OpenAppServicePropertyViewAction {
    private static final String UNABLE_TO_OPEN_BROWSER = "Unable to open external web browser";
    private static final String UNABLE_TO_OPEN_EDITOR_WINDOW = "Unable to open new editor window";
    private static final String CANNOT_GET_FILE_EDITOR_MANAGER = "Cannot get FileEditorManager";

    public static final Key<String> SUBSCRIPTION_ID = new Key<>("subscriptionId");
    public static final Key<String> RESOURCE_ID = new Key<>("resourceId");
    public static final Key<String> WEBAPP_ID = new Key<>("webAppId");
    public static final Key<String> SLOT_NAME = new Key<>("slotName");

    public void openWebAppPropertyView(@NotNull WebApp webApp, @Nullable Project project) {
        final String sid = webApp.subscriptionId();
        final String webAppId = webApp.id();
        final FileEditorManager fileEditorManager = getFileEditorManager(sid, webAppId, project);
        if (fileEditorManager == null) {
            return;
        }
        final String type = WebAppPropertyViewProvider.TYPE;
        LightVirtualFile itemVirtualFile = searchExistingFile(fileEditorManager, type, webAppId);
        if (itemVirtualFile == null) {
            itemVirtualFile = createVirtualFile(webApp.name(), sid, webAppId);
            itemVirtualFile.setFileType(new AzureFileType(type, AzureIcons.getIcon(AzureIconSymbol.WebApp.MODULE.getPath())));
        }
        final LightVirtualFile finalItemVirtualFile = itemVirtualFile;
        AzureTaskManager.getInstance().runLater(() -> fileEditorManager.openFile(finalItemVirtualFile, true /*focusEditor*/, true /*searchForOpen*/));
    }

    public void openDeploymentSlotPropertyView(@NotNull WebAppDeploymentSlot slot, @Nullable Project project) {
        final String sid = slot.subscriptionId();
        final String resourceId = slot.id();
        final FileEditorManager fileEditorManager = getFileEditorManager(sid, resourceId, project);
        if (fileEditorManager == null) {
            return;
        }
        final String type = DeploymentSlotPropertyViewProvider.TYPE;

        LightVirtualFile itemVirtualFile = searchExistingFile(fileEditorManager, type, resourceId);
        if (itemVirtualFile == null) {
            final Map<Key, String> userData = new HashMap<>();
            userData.put(SUBSCRIPTION_ID, sid);
            userData.put(RESOURCE_ID, resourceId);
            userData.put(WEBAPP_ID, slot.webApp().id());
            userData.put(SLOT_NAME, slot.name());
            itemVirtualFile = createVirtualFile(slot.webApp().name() + "-" + slot.name(), userData);
            itemVirtualFile.setFileType(new AzureFileType(type, AzureIcons.getIcon(AzureIconSymbol.DeploymentSlot.MODULE.getPath())));
        }
        final LightVirtualFile finalItemVirtualFile = itemVirtualFile;
        AzureTaskManager.getInstance().runLater(() -> fileEditorManager.openFile(finalItemVirtualFile, true /*focusEditor*/, true /*searchForOpen*/));
    }

    public void openFunctionAppPropertyView(FunctionApp functionApp, @Nullable Project project) {
        final String subscriptionId = functionApp.subscriptionId();
        final String functionApId = functionApp.id();
        final FileEditorManager fileEditorManager = getFileEditorManager(subscriptionId, functionApId, project);
        if (fileEditorManager == null) {
            return;
        }
        final String type = FunctionAppPropertyViewProvider.TYPE;
        LightVirtualFile itemVirtualFile = searchExistingFile(fileEditorManager, type, functionApId);
        if (itemVirtualFile == null) {
            itemVirtualFile = createVirtualFile(functionApp.name(), subscriptionId, functionApId);
            itemVirtualFile.setFileType(new AzureFileType(type, AzureIcons.getIcon(AzureIconSymbol.FunctionApp.MODULE.getPath())));
        }
        final LightVirtualFile finalItemVirtualFile = itemVirtualFile;
        AzureTaskManager.getInstance().runLater(() -> fileEditorManager.openFile(finalItemVirtualFile, true /*focusEditor*/, true /*searchForOpen*/));
    }

    protected FileEditorManager getFileEditorManager(final String sid, final String webAppId, final Project project) {
        if (StringUtils.isAllEmpty(sid, webAppId)) {
            return null;
        }
        final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        if (fileEditorManager == null) {
            AzureMessager.getMessager().error(CANNOT_GET_FILE_EDITOR_MANAGER, UNABLE_TO_OPEN_EDITOR_WINDOW);
            return null;
        }
        return fileEditorManager;
    }

    private LightVirtualFile searchExistingFile(FileEditorManager fileEditorManager, String fileType, String resourceId) {
        LightVirtualFile virtualFile = null;
        for (VirtualFile editedFile : fileEditorManager.getOpenFiles()) {
            String fileResourceId = editedFile.getUserData(RESOURCE_ID);
            if (fileResourceId != null && fileResourceId.equals(resourceId) &&
                    editedFile.getFileType().getName().equals(fileType)) {
                virtualFile = (LightVirtualFile) editedFile;
                break;
            }
        }
        return virtualFile;
    }

    private LightVirtualFile createVirtualFile(String name, String sid, String resId) {
        LightVirtualFile itemVirtualFile = new LightVirtualFile(name);
        itemVirtualFile.putUserData(SUBSCRIPTION_ID, sid);
        itemVirtualFile.putUserData(RESOURCE_ID, resId);
        return itemVirtualFile;
    }

    private LightVirtualFile createVirtualFile(String name, Map<Key, String> userData) {
        LightVirtualFile itemVirtualFile = new LightVirtualFile(name);
        for (final Map.Entry<Key, String> data : userData.entrySet()) {
            itemVirtualFile.putUserData(data.getKey(), data.getValue());
        }
        return itemVirtualFile;
    }
}
