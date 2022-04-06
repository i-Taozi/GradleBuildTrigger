/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.legacy.webapp.runner.webappconfig;

import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.microsoft.azure.toolkit.intellij.common.RunProcessHandlerMessenger;
import com.microsoft.azure.toolkit.lib.appservice.service.IWebAppBase;
import com.microsoft.azure.toolkit.lib.common.exception.AzureExecutionException;
import com.microsoft.azure.toolkit.intellij.common.AzureArtifact;
import com.microsoft.azure.toolkit.intellij.common.AzureArtifactManager;
import com.microsoft.azure.toolkit.intellij.legacy.common.AzureRunProfileState;
import com.microsoft.azure.toolkit.intellij.legacy.webapp.runner.Constants;
import com.microsoft.azure.toolkit.lib.appservice.AzureAppService;
import com.microsoft.azure.toolkit.lib.appservice.entity.WebAppEntity;
import com.microsoft.azure.toolkit.lib.appservice.service.IAppService;
import com.microsoft.azure.toolkit.lib.appservice.service.impl.WebApp;
import com.microsoft.azure.toolkit.lib.appservice.service.impl.WebAppDeploymentSlot;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azuretools.core.mvp.model.webapp.AzureWebAppMvpModel;
import com.microsoft.azuretools.telemetry.TelemetryConstants;
import com.microsoft.azuretools.telemetrywrapper.Operation;
import com.microsoft.azuretools.telemetrywrapper.TelemetryManager;
import com.microsoft.azuretools.utils.WebAppUtils;
import com.microsoft.intellij.RunProcessHandler;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.model.MavenConstants;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.microsoft.azure.toolkit.intellij.common.AzureBundle.message;

public class WebAppRunState extends AzureRunProfileState<IAppService> {
    private File artifact;
    private WebAppConfiguration webAppConfiguration;
    private final IntelliJWebAppSettingModel webAppSettingModel;

    /**
     * Place to execute the Web App deployment task.
     */
    public WebAppRunState(Project project, WebAppConfiguration webAppConfiguration) {
        super(project);
        this.webAppConfiguration = webAppConfiguration;
        this.webAppSettingModel = webAppConfiguration.getModel();
    }

    @Nullable
    @Override
    @AzureOperation(name = "webapp.deploy_artifact.app", params = {"this.webAppConfiguration.getWebAppName()"}, type = AzureOperation.Type.ACTION)
    public IAppService executeSteps(@NotNull RunProcessHandler processHandler, @NotNull Operation operation) throws Exception {
        final RunProcessHandlerMessenger messenger = new RunProcessHandlerMessenger(processHandler);
        AzureMessager.getContext().setMessager(messenger);
        artifact = new File(getTargetPath());
        if (!artifact.exists()) {
            throw new FileNotFoundException(message("webapp.deploy.error.noTargetFile", artifact.getAbsolutePath()));
        }
        final IWebAppBase deployTarget = getOrCreateDeployTargetFromAppSettingModel(processHandler);
        updateApplicationSettings(deployTarget, processHandler);
        AzureWebAppMvpModel.getInstance().deployArtifactsToWebApp(deployTarget, artifact, webAppSettingModel.isDeployToRoot(), processHandler);
        return deployTarget;
    }

    private void updateApplicationSettings(IAppService deployTarget, RunProcessHandler processHandler) {
        final Map<String, String> applicationSettings = webAppConfiguration.getApplicationSettings();
        if (MapUtils.isEmpty(applicationSettings)) {
            return;
        }
        if (deployTarget instanceof WebApp) {
            processHandler.setText("Updating application settings...");
            WebApp webApp = (WebApp) deployTarget;
            webApp.update().withAppSettings(applicationSettings).commit();
            processHandler.setText("Update application settings successfully.");
        } else if (deployTarget instanceof WebAppDeploymentSlot) {
            processHandler.setText("Updating deployment slot application settings...");
            AzureWebAppMvpModel.getInstance().updateDeploymentSlotAppSettings((WebAppDeploymentSlot) deployTarget, applicationSettings);
            processHandler.setText("Update deployment slot application settings successfully.");
        }
    }

    private boolean isDeployToSlot() {
        return !webAppSettingModel.isCreatingNew() && webAppSettingModel.isDeployToSlot();
    }

    @AzureOperation(name = "webapp.open_browser", type = AzureOperation.Type.ACTION)
    private void openWebAppInBrowser(String url, RunProcessHandler processHandler) {
        try {
            Desktop.getDesktop().browse(new URL(url).toURI());
        } catch (final IOException | URISyntaxException e) {
            processHandler.println(e.getMessage(), ProcessOutputTypes.STDERR);
        }
    }

    @Override
    protected Operation createOperation() {
        return TelemetryManager.createOperation(TelemetryConstants.WEBAPP, TelemetryConstants.DEPLOY_WEBAPP);
    }

    @Override
    @AzureOperation(name = "webapp.open_public_url.app", type = AzureOperation.Type.ACTION)
    protected void onSuccess(IAppService result, @NotNull RunProcessHandler processHandler) {
        updateConfigurationDataModel(result);
        final String fileName = FileNameUtils.getBaseName(artifact.getName());
        final String fileType = FileNameUtils.getExtension(artifact.getName());
        final String url = getUrl(result, fileName, fileType);
        processHandler.setText(message("appService.deploy.hint.succeed"));
        processHandler.setText("URL: " + url);
        if (webAppSettingModel.isOpenBrowserAfterDeployment()) {
            openWebAppInBrowser(url, processHandler);
        }
        processHandler.notifyComplete();
    }

    @Override
    protected Map<String, String> getTelemetryMap() {
        final Map<String, String> properties = new HashMap<>();
        properties.put("artifactType", webAppConfiguration.getAzureArtifactType() == null ? null : webAppConfiguration.getAzureArtifactType().name());
        properties.putAll(webAppSettingModel.getTelemetryProperties(Collections.EMPTY_MAP));
        return properties;
    }

    @NotNull
    private IWebAppBase getOrCreateDeployTargetFromAppSettingModel(@NotNull RunProcessHandler processHandler) throws Exception {
        final AzureAppService azureAppService = AzureWebAppMvpModel.getInstance().getAzureAppServiceClient(webAppSettingModel.getSubscriptionId());
        final WebApp webApp = getOrCreateWebappFromAppSettingModel(azureAppService, processHandler);
        if (!isDeployToSlot()) {
            return webApp;
        }
        // todo: add new boolean indicator instead of comparing string values
        if (StringUtils.equals(webAppSettingModel.getSlotName(), Constants.CREATE_NEW_SLOT)) {
            return createDeploymentSlot(webApp, processHandler);
        } else {
            return webApp.deploymentSlot(webAppSettingModel.getSlotName());
        }
    }

    private WebApp getOrCreateWebappFromAppSettingModel(AzureAppService azureAppService, RunProcessHandler processHandler) throws Exception {
        final WebAppEntity entity = WebAppEntity.builder().id(webAppSettingModel.getWebAppId())
                                                .subscriptionId(webAppSettingModel.getSubscriptionId())
                                                .resourceGroup(webAppSettingModel.getResourceGroup())
                                                .name(webAppSettingModel.getWebAppName()).build();
        final WebApp webApp = azureAppService.webapp(entity);
        if (webApp.exists()) {
            return webApp;
        }
        if (webAppSettingModel.isCreatingNew()) {
            processHandler.setText(message("webapp.deploy.hint.creatingWebApp"));
            return AzureWebAppMvpModel.getInstance().createWebAppFromSettingModel(webAppSettingModel);
        } else {
            processHandler.setText(message("appService.deploy.hint.failed"));
            throw new Exception(message("webapp.deploy.error.noWebApp"));
        }
    }

    @AzureOperation(
        name = "webapp.get_artifact.app",
        params = {"this.webAppConfiguration.getName()"},
        type = AzureOperation.Type.SERVICE
    )
    private String getTargetPath() throws AzureExecutionException {
        final AzureArtifact azureArtifact =
                AzureArtifactManager.getInstance(project).getAzureArtifactById(webAppConfiguration.getAzureArtifactType(),
                                                                               webAppConfiguration.getArtifactIdentifier());
        if (Objects.isNull(azureArtifact)) {
            final String error = String.format("selected artifact[%s] not found", webAppConfiguration.getArtifactIdentifier());
            throw new AzureExecutionException(error);
        }
        return AzureArtifactManager.getInstance(project).getFileForDeployment(azureArtifact);
    }

    @AzureOperation(
        name = "webapp.create_deployment.app",
        params = {"this.webAppConfiguration.getName()"},
        type = AzureOperation.Type.SERVICE
    )
    private WebAppDeploymentSlot createDeploymentSlot(final WebApp webApp,
                                                       @NotNull RunProcessHandler processHandler) {
        processHandler.setText(message("webapp.deploy.hint.creatingDeploymentSlot"));
        try {
            return AzureWebAppMvpModel.getInstance().createDeploymentSlotFromSettingModel(webApp, webAppSettingModel);
        } catch (final RuntimeException e) {
            processHandler.setText(message("webapp.deploy.error.noWebApp"));
            throw e;
        }
    }

    @NotNull
    private String getUrl(@NotNull IAppService webApp, @NotNull String fileName, @NotNull String fileType) {
        String url = "https://" + webApp.hostName();
        if (Comparing.equal(fileType, MavenConstants.TYPE_WAR) && !webAppSettingModel.isDeployToRoot()) {
            url += "/" + WebAppUtils.encodeURL(fileName.replaceAll("#", StringUtils.EMPTY)).replaceAll("\\+", "%20");
        }
        return url;
    }

    private void updateConfigurationDataModel(@NotNull IAppService app) {
        webAppSettingModel.setCreatingNew(false);
        // todo: add flag to indicate create new slot or not
        if (app instanceof WebAppDeploymentSlot) {
            webAppSettingModel.setSlotName(app.name());
            webAppSettingModel.setNewSlotConfigurationSource(AzureWebAppMvpModel.DO_NOT_CLONE_SLOT_CONFIGURATION);
            webAppSettingModel.setNewSlotName("");
            webAppSettingModel.setWebAppId(((WebAppDeploymentSlot) app).webApp().id());
        } else {
            webAppSettingModel.setWebAppId(app.id());
        }
        webAppSettingModel.setWebAppName("");
        webAppSettingModel.setResourceGroup("");
        webAppSettingModel.setAppServicePlanName("");
    }
}
