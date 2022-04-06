/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.applicationinsights.preference;

import com.microsoft.azure.management.applicationinsights.v2015_05_01.ApplicationInsightsComponent;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;

import java.io.Serializable;
/**
 * Model class to store application insights resource data.
 */
public class ApplicationInsightsResource implements Serializable, Comparable<ApplicationInsightsResource> {
    private static final long serialVersionUID = -5687551495552719808L;
    String resourceName;
    String instrumentationKey;
    String subscriptionName;
    String subscriptionId;
    String location;
    String resourceGroup;
    // imported = false --> manually added without authentication
    boolean imported;

    public ApplicationInsightsResource() {
        super();
    }

    public ApplicationInsightsResource(ApplicationInsightsComponent component, Subscription subscription, boolean imported) {
        super();
        this.resourceName = component.name();
        this.instrumentationKey = component.instrumentationKey();
        this.subscriptionName = subscription.getName();
        this.subscriptionId = subscription.getId();
        this.location = component.regionName();
        this.resourceGroup = component.resourceGroupName();
        this.imported = imported;
    }

    public ApplicationInsightsResource(String resourceName,
            String instrumentationKey, String subscriptionName,
            String subscriptionId, String location, String resourceGroup,
            boolean imported) {
        super();
        this.resourceName = resourceName;
        this.instrumentationKey = instrumentationKey;
        this.subscriptionName = subscriptionName;
        this.subscriptionId = subscriptionId;
        this.location = location;
        this.resourceGroup = resourceGroup;
        this.imported = imported;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getInstrumentationKey() {
        return instrumentationKey;
    }

    public void setInstrumentationKey(String instrumentationKey) {
        this.instrumentationKey = instrumentationKey;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(String resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    public boolean isImported() {
        return imported;
    }

    public void setImported(boolean imported) {
        this.imported = imported;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public void setSubscriptionName(String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        ApplicationInsightsResource resource = (ApplicationInsightsResource) obj;
        String key = resource.getInstrumentationKey();
        boolean value = instrumentationKey != null && instrumentationKey.equals(key);
        return value;
    }

    @Override
    public int compareTo(ApplicationInsightsResource object) {
        int value = resourceName.compareToIgnoreCase(object.getResourceName());
        return value;
    }
}
