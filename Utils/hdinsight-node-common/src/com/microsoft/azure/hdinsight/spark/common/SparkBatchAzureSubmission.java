/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.hdinsight.spark.common;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

import com.microsoft.azuretools.adauth.AuthException;
import com.microsoft.azuretools.adauth.PromptBehavior;
import com.microsoft.azuretools.authmanage.AuthMethodManager;
import com.microsoft.azuretools.authmanage.CommonSettings;
import com.microsoft.azuretools.azurecommons.helpers.NotNull;
import com.microsoft.azuretools.azurecommons.helpers.Nullable;
import com.microsoft.azuretools.sdkmanage.AzureManager;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

public class SparkBatchAzureSubmission extends SparkBatchSubmission {
    @NotNull
    private final String tenantId;
    @NotNull
    private final String accountName;
    @NotNull
    private final String clusterId;
    @Nullable
    private URI livyUri;

    public SparkBatchAzureSubmission(@NotNull String tenantId, @NotNull String accountName, @NotNull String clusterId, @Nullable URI livyUri) {
        super();
        this.tenantId = tenantId;
        this.accountName = accountName;
        this.clusterId = clusterId;
        this.livyUri = livyUri;
    }

    @Override
    public void setUsernamePasswordCredential(String username, String password) {
        throw new UnsupportedOperationException("Azure does not support UserName/Password credential");
    }

    @NotNull
    private String getResourceEndpoint() {
        String endpoint = CommonSettings.getAdEnvironment().dataLakeEndpointResourceId();

        return endpoint != null ? endpoint : "https://datalake.azure.net/";
    }

    @NotNull
    String getAccessToken() throws IOException {
        AzureManager azureManager = AuthMethodManager.getInstance().getAzureManager();
        // not signed in
        if (azureManager == null) {
            throw new AuthException("Not signed in. Can't send out the request.");
        }

        return azureManager.getAccessToken(getTenantId(), getResourceEndpoint(), PromptBehavior.Auto);
    }

    @NotNull
    @Override
    public CloseableHttpClient getHttpClient() throws IOException {
        return HttpClients.custom()
                .useSystemProperties()
                .setDefaultHeaders(Arrays.asList(
                        new BasicHeader("Authorization", "Bearer " + getAccessToken()),
                        new BasicHeader("x-ms-kobo-account-name", getAccountName())))
                .setSSLSocketFactory(getSSLSocketFactory())
                .build();
    }

    @NotNull
    public String getAccountName() {
        return accountName;
    }

    @NotNull
    public String getTenantId() {
        return tenantId;
    }

    @Nullable
    public URI getLivyUri() {
        return livyUri;
    }

    @NotNull
    public String getClusterId() {
        return clusterId;
    }
}
