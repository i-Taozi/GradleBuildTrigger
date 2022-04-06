/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.tooling.msservices.helpers.azure.sdk;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.applicationinsights.v2015_05_01.ApplicationInsightsComponent;
import com.microsoft.azure.management.applicationinsights.v2015_05_01.ApplicationType;
import com.microsoft.azure.management.applicationinsights.v2015_05_01.implementation.InsightsManager;
import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.KnownWindowsVirtualMachineImage;
import com.microsoft.azure.management.compute.OperatingSystemTypes;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.logging.Log;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.storage.StorageAccount;
import com.microsoft.azuretools.authmanage.AuthMethodManager;
import com.microsoft.azuretools.authmanage.models.SubscriptionDetail;
import com.microsoft.azuretools.azurecommons.helpers.NotNull;
import com.microsoft.azuretools.azurecommons.helpers.Nullable;
import com.microsoft.azuretools.sdkmanage.AzureManager;
import com.microsoft.tooling.msservices.model.vm.VirtualNetwork;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AzureSDKManager {

    private static final String INSIGHTS_REGION_LIST_URL = "https://management.azure.com/providers/microsoft.insights?api-version=2015-05-01";

    public static VirtualMachine createVirtualMachine(String subscriptionId, @NotNull String name,
                                                      @NotNull String resourceGroup, boolean withNewResourceGroup,
                                                      @NotNull String size, @NotNull String region,
                                                      final VirtualMachineImage vmImage, Object knownImage,
                                                      boolean isKnownImage, final StorageAccount storageAccount,
                                                      final Network network, VirtualNetwork newNetwork,
                                                      boolean withNewNetwork, @NotNull String subnet,
                                                      @Nullable PublicIPAddress pip, boolean withNewPip,
                                                      @Nullable AvailabilitySet availabilitySet,
                                                      boolean withNewAvailabilitySet, @NotNull final String username,
                                                      @Nullable final String password, @Nullable String publicKey)
            throws Exception {
        AzureManager azureManager = AuthMethodManager.getInstance().getAzureManager();
        Azure azure = azureManager.getAzure(subscriptionId);
        boolean isWindows;
        if (isKnownImage) {
            isWindows = knownImage instanceof KnownWindowsVirtualMachineImage;
        } else {
            isWindows = vmImage.osDiskImage().operatingSystem() == OperatingSystemTypes.WINDOWS;
        }
        // ------ Resource Group ------
        VirtualMachine.DefinitionStages.WithGroup withGroup = azure.virtualMachines().define(name)
                .withRegion(region);
        Creatable<ResourceGroup> newResourceGroup = null;
        VirtualMachine.DefinitionStages.WithNetwork withNetwork;
        if (withNewResourceGroup) {
            newResourceGroup = azure.resourceGroups().define(resourceGroup).withRegion(region);
            withNetwork = withGroup.withNewResourceGroup(newResourceGroup);
        } else {
            withNetwork = withGroup.withExistingResourceGroup(resourceGroup);
        }
        // ------ Virtual Network -----
        VirtualMachine.DefinitionStages.WithPublicIPAddress withPublicIpAddress;
        if (withNewNetwork) {
            Network.DefinitionStages.WithGroup networkWithGroup = azure.networks().define(newNetwork.name).withRegion(region);
            Creatable<Network> newVirtualNetwork;
            if (withNewResourceGroup) {
                newVirtualNetwork = networkWithGroup.withNewResourceGroup(newResourceGroup)
                        .withAddressSpace(newNetwork.addressSpace)
                        .withSubnet(newNetwork.subnet.name, newNetwork.subnet.addressSpace);
            } else {
                newVirtualNetwork = networkWithGroup.withExistingResourceGroup(resourceGroup)
                        .withAddressSpace(newNetwork.addressSpace)
                        .withSubnet(newNetwork.subnet.name, newNetwork.subnet.addressSpace);
            }
            withPublicIpAddress = withNetwork.withNewPrimaryNetwork(newVirtualNetwork).withPrimaryPrivateIPAddressDynamic();
            //withPublicIpAddress = withNetwork.withNewPrimaryNetwork("10.0.0.0/28").
            //.withPrimaryPrivateIpAddressDynamic();
        } else {
            withPublicIpAddress = withNetwork.withExistingPrimaryNetwork(network)
                    .withSubnet(subnet)
                    .withPrimaryPrivateIPAddressDynamic();
        }
        // ------ Public IP Address------
        VirtualMachine.DefinitionStages.WithOS withOS;
        if (pip == null) {
            if (withNewPip) {
                withOS = withPublicIpAddress.withNewPrimaryPublicIPAddress(name + "pip");
            } else {
                withOS = withPublicIpAddress.withoutPrimaryPublicIPAddress();
            }
        } else {
            withOS = withPublicIpAddress.withExistingPrimaryPublicIPAddress(pip);
        }
        // ------ OS and credentials -----
        VirtualMachine.DefinitionStages.WithCreate withCreate;
        if (isWindows) {
            VirtualMachine.DefinitionStages.WithWindowsAdminUsernameManagedOrUnmanaged withWindowsAdminUsername;
            if (isKnownImage) {
                withWindowsAdminUsername = withOS.withPopularWindowsImage((KnownWindowsVirtualMachineImage) knownImage);
            } else {
                withWindowsAdminUsername = withOS.withSpecificWindowsImageVersion(vmImage.imageReference());
            }
            withCreate = withWindowsAdminUsername.withAdminUsername(username).withAdminPassword(password).withUnmanagedDisks();
        } else {
            VirtualMachine.DefinitionStages.WithLinuxRootPasswordOrPublicKeyManagedOrUnmanaged withLinuxRootPasswordOrPublicKey;
            if (isKnownImage) {
                withLinuxRootPasswordOrPublicKey = withOS.withPopularLinuxImage((KnownLinuxVirtualMachineImage) knownImage).withRootUsername(username);
            } else {
                withLinuxRootPasswordOrPublicKey = withOS.withSpecificLinuxImageVersion(vmImage.imageReference()).withRootUsername(username);
            }
            VirtualMachine.DefinitionStages.WithLinuxCreateManagedOrUnmanaged withLinuxCreate;
            // we assume either password or public key is not empty
            if (password != null && !password.isEmpty()) {
                withLinuxCreate = withLinuxRootPasswordOrPublicKey.withRootPassword(password);
                if (publicKey != null) {
                    withLinuxCreate = withLinuxCreate.withSsh(publicKey);
                }
            } else {
                withLinuxCreate = withLinuxRootPasswordOrPublicKey.withSsh(publicKey);
            }
            withCreate = withLinuxCreate.withUnmanagedDisks();
        }
        withCreate = withCreate.withSize(size);
        // ---- Storage Account --------
        com.microsoft.azure.management.storage.StorageAccount existedStorageAccount = azure.storageAccounts().getById(storageAccount.id());
        withCreate = withCreate.withExistingStorageAccount(existedStorageAccount);
        if (withNewAvailabilitySet) {
            withCreate = withCreate.withNewAvailabilitySet(name + "as");
        } else if (availabilitySet != null) {
            withCreate = withCreate.withExistingAvailabilitySet(availabilitySet);
        }
        return withCreate.create();
    }

    public static List<ApplicationInsightsComponent> getInsightsResources(@NotNull String subscriptionId) throws IOException {
        final InsightsManager insightsManager = getInsightsManagerClient(subscriptionId);
        return insightsManager == null ? Collections.emptyList() : new ArrayList<>(insightsManager.components().list());
    }

    public static List<ApplicationInsightsComponent> getInsightsResources(@NotNull SubscriptionDetail subscription) throws IOException {
        return getInsightsResources(subscription.getSubscriptionId());
    }

    // SDK will return existing application insights component when you create new one with existing name
    // Use this method in case SDK service update their behavior
    public static ApplicationInsightsComponent getOrCreateApplicationInsights(@NotNull String subscriptionId,
                                                                              @NotNull String resourceGroupName,
                                                                              @NotNull String resourceName,
                                                                              @NotNull String location) throws IOException {
        final InsightsManager insightsManager = getInsightsManagerClient(subscriptionId);
        if (insightsManager == null) {
            return null;
        }
        ApplicationInsightsComponent component = null;
        try {
            component = insightsManager.components().getByResourceGroup(resourceGroupName, resourceName);
        } catch (Exception e) {
            // SDK will throw exception when resource not found
        }
        return component != null ? component : createInsightsResource(subscriptionId, resourceGroupName, resourceName, location);
    }

    public static ApplicationInsightsComponent createInsightsResource(@NotNull String subscriptionId,
                                                                      @NotNull String resourceGroupName,
                                                                      @NotNull String resourceName,
                                                                      @NotNull String location) throws IOException {
        final InsightsManager insightsManager = getInsightsManagerClient(subscriptionId);
        if (insightsManager == null) { // not signed in
            return null;
        }
        final Azure azure = AuthMethodManager.getInstance().getAzureClient(subscriptionId);
        if (!azure.resourceGroups().contain(resourceGroupName)) {
            azure.resourceGroups().define(resourceGroupName).withRegion(location).create();
        }
        return insightsManager.components()
                .define(resourceName)
                .withRegion(location)
                .withExistingResourceGroup(resourceGroupName)
                .withApplicationType(ApplicationType.WEB)
                .withKind("web")
                .create();
    }

    public static ApplicationInsightsComponent createInsightsResource(@NotNull Subscription subscription,
                                                                      @NotNull String resourceGroupName,
                                                                      boolean isNewGroup,
                                                                      @NotNull String resourceName,
                                                                      @NotNull String location) throws IOException {
        return createInsightsResource(subscription.getId(), resourceGroupName, resourceName, location);
    }

    public static List<String> getLocationsForInsights(String subscriptionId) throws IOException {
        final HttpGet request = new HttpGet(INSIGHTS_REGION_LIST_URL);
        final AzureManager azureManager = AuthMethodManager.getInstance().getAzureManager();
        if (azureManager == null) {
            return Collections.emptyList();
        }
        Subscription subscription = com.microsoft.azure.toolkit.lib.Azure.az(AzureAccount.class).account().getSubscription(subscriptionId);
        final String accessToken = azureManager.getAccessToken(subscription.getTenantId());
        request.setHeader("Authorization", String.format("Bearer %s", accessToken));
        final CloseableHttpResponse response = HttpClients.createDefault().execute(request);
        final InputStream responseStream = response.getEntity().getContent();
        try (final InputStreamReader isr = new InputStreamReader(responseStream)) {
            final Gson gson = new Gson();
            final JsonObject jsonContent = (gson).fromJson(isr, JsonObject.class);
            final JsonArray jsonResourceTypes = jsonContent.getAsJsonArray("resourceTypes");
            for (int i = 0; i < jsonResourceTypes.size(); ++i) {
                Object obj = jsonResourceTypes.get(i);
                if (obj instanceof JsonObject) {
                    JsonObject jsonResourceType = (JsonObject) obj;
                    String resourceType = jsonResourceType.get("resourceType").getAsString();
                    if (resourceType.equalsIgnoreCase("components")) {
                        JsonArray jsonLocations = jsonResourceType.getAsJsonArray("locations");
                        return gson.fromJson(jsonLocations, new ArrayList().getClass());
                    }
                }
            }
        } catch (IOException | JsonParseException e) {
            Log.error(e);
        }
        return Collections.emptyList();
    }

    // TODO: AI SDK doesn't provide a method to list regions which are available regions to create AI,
    // we are requiring the SDK to provide that API, before SDK side fix, we will use our own impl
    public static List<String> getLocationsForInsights(SubscriptionDetail subscription) throws IOException {
        return getLocationsForInsights(subscription.getSubscriptionId());
    }

    @NotNull
    private static InsightsManager getInsightsManagerClient(@NotNull String subscriptionId)
            throws IOException {
        final AzureManager azureManager = AuthMethodManager.getInstance().getAzureManager();
        return azureManager == null ? null : azureManager.getInsightsManager(subscriptionId);
    }
}
