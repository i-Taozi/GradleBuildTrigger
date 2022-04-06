/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.deploymentslot;

import com.microsoft.azure.toolkit.lib.appservice.model.OperatingSystem;
import com.microsoft.azure.toolkit.lib.appservice.service.impl.WebApp;
import com.microsoft.azure.toolkit.lib.appservice.service.impl.WebAppDeploymentSlot;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azuretools.azurecommons.helpers.Nullable;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.serviceexplorer.*;
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.base.WebAppBaseNode;
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.base.WebAppBaseState;

import java.util.List;

public class DeploymentSlotNode extends WebAppBaseNode {
    private static final String ACTION_SWAP_WITH_PRODUCTION = "Swap with production";
    private static final String LABEL = "Slot";

    private final WebApp webApp;
    private final WebAppDeploymentSlot slot;

    public DeploymentSlotNode(final WebAppDeploymentSlot deploymentSlot, final DeploymentSlotModule parent) {
        super(parent, LABEL, deploymentSlot);
        this.webApp = deploymentSlot.webApp();
        this.slot = deploymentSlot;
    }

    @Override
    public @Nullable AzureIconSymbol getIconSymbol() {
        boolean isLinux = slot.getRuntime().getOperatingSystem() != OperatingSystem.WINDOWS;
        boolean running = WebAppBaseState.RUNNING.equals(state);
        boolean updating = WebAppBaseState.UPDATING.equals(state);
        if (isLinux) {
            return running ? AzureIconSymbol.DeploymentSlot.RUNNING_ON_LINUX :
                    updating ? AzureIconSymbol.DeploymentSlot.UPDATING_ON_LINUX : AzureIconSymbol.DeploymentSlot.STOPPED_ON_LINUX;
        } else {
            return running ? AzureIconSymbol.DeploymentSlot.RUNNING :
                    updating ? AzureIconSymbol.DeploymentSlot.UPDATING : AzureIconSymbol.DeploymentSlot.STOPPED;
        }
    }

    public String getWebAppId() {
        return this.webApp.id();
    }

    public String getWebAppName() {
        return this.webApp.name();
    }

    @Override
    public List<NodeAction> getNodeActions() {
        getNodeActionByName(ACTION_SWAP_WITH_PRODUCTION).setEnabled(this.state == WebAppBaseState.RUNNING);
        return super.getNodeActions();
    }

    @Override
    protected void loadActions() {
        addAction(initActionBuilder(this::start).withAction(AzureActionEnum.START).withBackgroudable(true).build());
        addAction(initActionBuilder(this::stop).withAction(AzureActionEnum.STOP).withBackgroudable(true).build());
        addAction(initActionBuilder(this::restart).withAction(AzureActionEnum.RESTART).withBackgroudable(true).build());
        addAction(initActionBuilder(this::delete).withAction(AzureActionEnum.DELETE).withBackgroudable(true).withPromptable(true).build());
        addAction(initActionBuilder(this::openInPortal).withAction(AzureActionEnum.OPEN_IN_PORTAL).withBackgroudable(true).build());
        addAction(initActionBuilder(this::openInBrowser).withAction(AzureActionEnum.OPEN_IN_BROWSER).withBackgroudable(true).build());
        addAction(initActionBuilder(this::showProperties).withAction(AzureActionEnum.SHOW_PROPERTIES).withBackgroudable(true).build());
        addAction(ACTION_SWAP_WITH_PRODUCTION, initActionBuilder(this::swap).withBackgroudable(true).build("Swapping"));
        super.loadActions();
    }

    private BasicActionBuilder initActionBuilder(Runnable runnable) {
        return new BasicActionBuilder(runnable)
                .withModuleName(DeploymentSlotModule.MODULE_NAME)
                .withInstanceName(name);
    }

    @Override
    protected void onNodeClick(NodeActionEvent e) {
        // RefreshableNode refresh itself when the first time being clicked.
        // The deployment slot node is just a single node for the time being.
        // Override the function to do noting to disable the auto refresh functionality.
    }

    @Override
    @AzureOperation(name = "webapp.refresh_deployment.deployment|app", params = {"this.slot.name()", "this.webApp.name()"}, type = AzureOperation.Type.ACTION)
    protected void refreshItems() {
        super.refreshItems();
        if (slot.exists()) {
            this.renderNode(WebAppBaseState.fromString(slot.state()));
        } else {
            parent.removeNode(subscriptionId, name, this);
        }
    }

    @AzureOperation(name = "webapp.start_deployment.deployment|app", params = {"this.slot.name()", "this.webApp.name()"}, type = AzureOperation.Type.ACTION)
    private void start() {
        slot.start();
        this.renderNode(WebAppBaseState.RUNNING);
    }

    @AzureOperation(name = "webapp.stop_deployment.deployment|app", params = {"this.slot.name()", "this.webApp.name()"}, type = AzureOperation.Type.ACTION)
    private void stop() {
        slot.stop();
        this.renderNode(WebAppBaseState.STOPPED);
    }

    @AzureOperation(name = "webapp.restart_deployment.deployment|app", params = {"this.slot.name()", "this.webApp.name()"}, type = AzureOperation.Type.ACTION)
    private void restart() {
        slot.restart();
        this.renderNode(WebAppBaseState.RUNNING);
    }

    @AzureOperation(name = "webapp.delete_deployment.deployment|app", params = {"this.slot.name()", "this.webApp.name()"}, type = AzureOperation.Type.ACTION)
    private void delete() {
        this.getParent().removeNode(this.getSubscriptionId(), this.getName(), DeploymentSlotNode.this);
    }

    @AzureOperation(name = "webapp.swap_deployment.deployment|app", params = {"this.slot.name()", "this.webApp.name()"}, type = AzureOperation.Type.ACTION)
    private void swap() {
        webApp.swap(slot.name());
    }

    @AzureOperation(name = "webapp.open_portal_deployment.deployment", params = {"this.slot.name()"}, type = AzureOperation.Type.ACTION)
    private void openInPortal() {
        this.openResourcesInPortal(this.slot.subscriptionId(), this.slot.id());
    }

    @AzureOperation(name = "webapp|deployment.open_browser", params = {"this.slot.name()", "this.webApp.name()"}, type = AzureOperation.Type.ACTION)
    private void openInBrowser() {
        DefaultLoader.getUIHelper().openInBrowser("https://" + slot.hostName());
    }

    @AzureOperation(name = "webapp|deployment.show_properties", params = {"this.slot.name()", "this.webApp.name()"}, type = AzureOperation.Type.ACTION)
    private void showProperties() {
        DefaultLoader.getUIHelper().openDeploymentSlotPropertyView(DeploymentSlotNode.this);
    }

}
