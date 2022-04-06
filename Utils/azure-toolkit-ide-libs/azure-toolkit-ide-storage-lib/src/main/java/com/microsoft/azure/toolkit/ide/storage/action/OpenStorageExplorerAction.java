/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.storage.action;

import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperationBundle;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.storage.StorageAccount;

public class OpenStorageExplorerAction {
    public static void openStorageBrowser(StorageAccount account) {
        final AzureString title = AzureOperationBundle.title("storage.open_storage_explorer.account", account.name());
        AzureTaskManager.getInstance().runInBackground(new AzureTask<>(title, () -> {
            final String url = account.portalUrl() + "/storageExplorer";
            AzureActionManager.getInstance().getAction(ResourceCommonActionsContributor.OPEN_URL).handle(url);
        }));
    }
}
