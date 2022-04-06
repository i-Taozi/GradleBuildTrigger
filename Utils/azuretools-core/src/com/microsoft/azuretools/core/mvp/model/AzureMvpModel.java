/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.core.mvp.model;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.toolkit.lib.appservice.model.PricingTier;
import com.microsoft.azure.management.resources.Deployment;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import com.microsoft.azure.toolkit.lib.common.model.ResourceGroup;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.resource.AzureGroup;
import com.microsoft.azuretools.authmanage.AuthMethodManager;
import com.microsoft.azuretools.sdkmanage.AzureManager;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.microsoft.azure.toolkit.lib.Azure.az;
import static com.microsoft.azure.toolkit.lib.appservice.model.PricingTier.WEB_APP_PRICING;

public class AzureMvpModel {

    public static final String CANNOT_GET_RESOURCE_GROUP = "Cannot get Resource Group.";
    public static final String APPLICATION_LOG_NOT_ENABLED = "Application log is not enabled.";
    private static final PricingTier PREMIUM_P1V3 = new PricingTier("PremiumV3", "P1v3");
    private static final PricingTier PREMIUM_P2V3 = new PricingTier("PremiumV3", "P2v3");
    private static final PricingTier PREMIUM_P3V3 = new PricingTier("PremiumV3", "P3v3");
    private static final List<PricingTier> V3_PRICING_LIST = Arrays.asList(PREMIUM_P1V3, PREMIUM_P2V3, PREMIUM_P3V3);

    private AzureMvpModel() {
    }

    public static AzureMvpModel getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public static String getSegment(String id, String segment) {
        if (StringUtils.isEmpty(id)) {
            return null;
        }
        final String[] attributes = id.split("/");
        int pos = ArrayUtils.indexOf(attributes, segment);
        if (pos >= 0) {
            return attributes[pos + 1];
        }
        return null;
    }

    /**
     * Get subscription by subscriptionId.
     *
     * @param sid Subscription Id
     * @return Instance of Subscription
     */
    @AzureOperation(
        name = "account.get_subscription_detail.subscription",
        params = {"sid"},
        type = AzureOperation.Type.SERVICE
    )
    public Subscription getSubscriptionById(String sid) {
        final AzureManager azureManager = AuthMethodManager.getInstance().getAzureManager();
        return azureManager.getSubscriptionById(sid);
    }

    /**
     * Get list of selected Subscriptions.
     *
     * @return List of Subscription instances
     */
    @AzureOperation(
        name = "account.get_subscription_detail",
        type = AzureOperation.Type.SERVICE
    )
    public List<Subscription> getSelectedSubscriptions() {
        final List<Subscription> ret = new ArrayList<>();
        final AzureManager azureManager = AuthMethodManager.getInstance().getAzureManager();
        ret.addAll(azureManager.getSelectedSubscriptions());
        Collections.sort(ret, getComparator(Subscription::getName));
        return ret;
    }

    /**
     * List all the resource groups in specific subscription.
     * @return
     */
    @AzureOperation(
        name = "arm.list_resource_groups",
        type = AzureOperation.Type.SERVICE
    )
    public List<ResourceEx<ResourceGroup>> getResourceGroups(String sid) {
        List<ResourceEx<ResourceGroup>> resourceGroups = new ArrayList<>();
        resourceGroups.addAll(az(AzureGroup.class).list(sid).stream().map(r -> new ResourceEx<>(r, sid)).collect(Collectors.toList()));
        Collections.sort(resourceGroups, getComparator((ResourceEx<ResourceGroup> resourceGroupResourceEx) ->
                resourceGroupResourceEx.getResource().getName()));
        return resourceGroups;
    }

    /**
     * List all the resource groups in selected subscriptions.
     * @return
     */
    @AzureOperation(
            name = "arm.list_resource_groups",
            type = AzureOperation.Type.SERVICE
    )
    public List<ResourceEx<ResourceGroup>> getResourceGroups() {
        List<ResourceEx<ResourceGroup>> resourceGroups = new ArrayList<>();
        resourceGroups.addAll(az(AzureGroup.class).list().stream().map(r -> new ResourceEx<>(r, r.getSubscriptionId())).collect(Collectors.toList()));
        Collections.sort(resourceGroups, getComparator((ResourceEx<ResourceGroup> resourceGroupResourceEx) ->
                resourceGroupResourceEx.getResource().getName()));
        return resourceGroups;
    }

    /**
     *
     * @param rgName resource group name
     * @param sid subscription id
     * @return
     */
    @AzureOperation(
        name = "arm.delete_resource_group.rg",
        params = {"rgName"},
        type = AzureOperation.Type.SERVICE
    )
    public void deleteResourceGroup(String rgName, String sid) {
        az(AzureGroup.class).delete(sid, rgName);
    }

    /**
     * List Resource Group by Subscription ID.
     *
     * @param sid subscription Id
     * @return List of ResourceGroup instances
     */
    @AzureOperation(
        name = "arm.list_resource_groups.subscription",
        params = {"sid"},
        type = AzureOperation.Type.SERVICE
    )
    public List<ResourceGroup> getResourceGroupsBySubscriptionId(String sid) {
        List<ResourceGroup> ret = new ArrayList<>(az(AzureGroup.class).list(sid));
        Collections.sort(ret, getComparator(ResourceGroup::getName));
        return ret;
    }

    /**
     * Get Resource Group by Subscription ID and Resource Group name.
     */
    @AzureOperation(
        name = "arm.get_resource_group.rg|subscription",
        params = {"name", "sid"},
        type = AzureOperation.Type.SERVICE
    )
    public ResourceGroup getResourceGroupBySubscriptionIdAndName(String sid, String name) throws Exception {
        try {
            return az(AzureGroup.class).get(sid, name);
        } catch (Exception e) {
            throw new Exception(CANNOT_GET_RESOURCE_GROUP);
        }
    }

    @AzureOperation(
        name = "arm.list_deployments",
        type = AzureOperation.Type.SERVICE
    )
    public List<Deployment> listAllDeployments() {
        List<Deployment> deployments = new ArrayList<>();
        List<Subscription> subs = getSelectedSubscriptions();
        Observable.from(subs).flatMap((sub) ->
            Observable.create((subscriber) -> {
                List<Deployment> sidDeployments = listDeploymentsBySid(sub.getId());
                synchronized (deployments) {
                    deployments.addAll(sidDeployments);
                }
                subscriber.onCompleted();
            }).subscribeOn(Schedulers.io()), subs.size()).subscribeOn(Schedulers.io()).toBlocking().subscribe();
        Collections.sort(deployments, getComparator(Deployment::name));
        return deployments;
    }

    @AzureOperation(
        name = "arm.list_deployments.subscription",
        params = {"sid"},
        type = AzureOperation.Type.SERVICE
    )
    public List<Deployment> listDeploymentsBySid(String sid) {
        Azure azure = AuthMethodManager.getInstance().getAzureClient(sid);
        List<Deployment> deployments = azure.deployments().list();
        Collections.sort(deployments, getComparator(Deployment::name));
        return deployments;
    }

    /**
     * Get deployment by resource group name
     * @param rgName
     * @return
     */
    @AzureOperation(
        name = "arm.list_deployments.rg|subscription",
        params = {"rgName", "sid"},
        type = AzureOperation.Type.SERVICE
    )
    public List<ResourceEx<Deployment>> getDeploymentByRgName(String sid, String rgName) {
        List<ResourceEx<Deployment>> res = new ArrayList<>();
        Azure azure = AuthMethodManager.getInstance().getAzureClient(sid);
        res.addAll(azure.deployments().listByResourceGroup(rgName).stream().
            map(deployment -> new ResourceEx<>(deployment, sid)).collect(Collectors.toList()));
        Collections.sort(res,
                getComparator((ResourceEx<Deployment> deploymentResourceEx) -> deploymentResourceEx.getResource().name()));
        return res;
    }

    /**
     * List Location by Subscription ID.
     *
     * @param sid subscription Id
     * @return List of Location instances
     */
    @AzureOperation(
        name = "common.list_regions.subscription",
        params = {"sid"},
        type = AzureOperation.Type.SERVICE
    )
    public List<Region> listLocationsBySubscriptionId(String sid) {
        List<Region> locations = new ArrayList<>();
        Subscription subscription = getSubscriptionById(sid);
        try {
            locations.addAll(az(AzureAccount.class).listRegions(subscription.getId()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Collections.sort(locations, getComparator(Region::getName));
        return locations;
    }

    /**
     * List all Pricing Tier supported by SDK.
     *
     * @return List of PricingTier instances.
     */
    @AzureOperation(
        name = "common.list_tiers",
        type = AzureOperation.Type.SERVICE
    )
    public List<PricingTier> listPricingTier() {
        final List<PricingTier> ret = new ArrayList<>(WEB_APP_PRICING);
        ret.sort(getComparator(PricingTier::toString));
        return correctPricingTiers(ret);
    }

    private static <T> Comparator<T> getComparator(Function<T, String> toStringMethod) {
        return (first, second) ->
                StringUtils.compareIgnoreCase(toStringMethod.apply(first), toStringMethod.apply(second));
    }

    // Remove Premium pricing tier which has performance issues with java app services
    private List<PricingTier> correctPricingTiers(final List<PricingTier> pricingTiers) {
        pricingTiers.remove(PricingTier.PREMIUM_P1);
        pricingTiers.remove(PricingTier.PREMIUM_P2);
        pricingTiers.remove(PricingTier.PREMIUM_P3);
        return pricingTiers;
    }

    private static final class SingletonHolder {
        private static final AzureMvpModel INSTANCE = new AzureMvpModel();
    }
}
