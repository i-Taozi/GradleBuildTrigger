/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.tooling.msservices.serviceexplorer.azure.arm;

import com.microsoft.azure.toolkit.lib.common.model.ResourceGroup;
import com.microsoft.azuretools.core.mvp.model.ResourceEx;
import com.microsoft.azuretools.core.mvp.ui.base.MvpView;
import java.util.List;

public interface ResourceManagementModuleView extends MvpView {
    void renderChildren(List<ResourceEx<ResourceGroup>> resourceExes);
}
