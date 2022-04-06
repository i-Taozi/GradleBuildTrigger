/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.core.mvp.model;

@Deprecated
public class ResourceEx<T> {
    private T resource;
    private String subscriptionId;
    public ResourceEx(T resource, String subscriptionId) {
        this.resource = resource;
        this.subscriptionId = subscriptionId;
    }
    public T getResource() {
        return resource;
    }
    public void setResource(T resource) {
        this.resource = resource;
    }
    public String getSubscriptionId() {
        return subscriptionId;
    }
    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }
}
