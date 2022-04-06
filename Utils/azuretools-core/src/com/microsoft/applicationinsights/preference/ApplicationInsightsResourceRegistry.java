/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.applicationinsights.preference;

import com.microsoft.azure.management.applicationinsights.v2015_05_01.ApplicationInsightsComponent;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;

import java.util.*;

public class ApplicationInsightsResourceRegistry {
    /**
     * List of application insights resources,
     * to be in sync with preference file data.
     */
    private static List<ApplicationInsightsResource> appInsightsResrcList =
            new ArrayList<ApplicationInsightsResource>();

    public static List<ApplicationInsightsResource> getAppInsightsResrcList() {
        return appInsightsResrcList;
    }

    public static void setAppInsightsResrcList(List<ApplicationInsightsResource> appInsightsResrcList) {
        ApplicationInsightsResourceRegistry.appInsightsResrcList = appInsightsResrcList;
    }

    /**
     * Method returns array of application insights resources names.
     * If resource with same name but different subscription name
     * exists then it will add concatenated string of resource name and subscription name to array.
     * @return
     */
    public static String[] getResourcesNamesToDisplay() {
        ArrayList<String> nameList = getResourcesNames();
        List<ApplicationInsightsResource> resourceList = getAppInsightsResrcList();
        String [] nameArr = null;
        // check whether registry entries with same resource names exist
        if (chkDuplicateUsingSet(nameList)) {
            List<ApplicationInsightsResourceWithSameName> sameResourceNameList =
                    identifyDuplicates(nameList);
            // iterate over same resource name list.
            for (int i = 0; i < sameResourceNameList.size(); i++) {
                ApplicationInsightsResourceWithSameName sameResource = sameResourceNameList.get(i);
                // get list of indices on which entries with same resource name exists.
                List<Integer> indices = sameResource.getIndices();
                for (int j = 0; j < indices.size(); j++) {
                    int index = indices.get(j);
                    String subName = resourceList.get(index).getSubscriptionName();
                    // append subscription name to resource name
                    String resourceAndSub = concatenateRsrcAndSubName(nameList.get(index), subName);
                    nameList.set(index, resourceAndSub);
                }
            }
        }
        nameArr = nameList.toArray(new String[nameList.size()]);
        return nameArr;
    }

    /**
     * Method returns array of application insights resources names.
     * @return
     */
    public static ArrayList<String> getResourcesNames() {
        ArrayList<String> nameList = new ArrayList<String>();
        // Get list of application insights resources and prepare list of resources names.
        List<ApplicationInsightsResource> resourceList = getAppInsightsResrcList();
        for (Iterator<ApplicationInsightsResource> iterator = resourceList.iterator(); iterator.hasNext();) {
            ApplicationInsightsResource resource = (ApplicationInsightsResource) iterator.next();
            nameList.add(resource.getResourceName());
        }
        return nameList;
    }

    /**
     * Method checks list of application insights resources one by one,
     * and if there are more than one entry with
     * same resource name then create object
     * of ApplicationInsightsResourceWithSameName with name and indices
     * where entries are found in list.
     * @param nameList
     * @return
     */
    public static List<ApplicationInsightsResourceWithSameName> identifyDuplicates(List<String> nameList) {
        List<ApplicationInsightsResourceWithSameName> sameResourceNameList =
                new ArrayList<ApplicationInsightsResourceWithSameName>();
        for (int i = 0; i < nameList.size(); i++) {
            for (int j = i + 1; j < nameList.size(); j++) {
                // check if duplicate entry present in list.
                if (i != j && nameList.get(i).equals(nameList.get(j))) {
                    ApplicationInsightsResourceWithSameName resource =
                            new ApplicationInsightsResourceWithSameName(nameList.get(i));
                    /*
                     * Check same resource name list contains
                     * entry with this resource name,
                     * if yes then just add index of entry to list
                     * else create new object.
                     */
                    if (sameResourceNameList.contains(resource)) {
                        int resourceIndex = sameResourceNameList.indexOf(resource);
                        ApplicationInsightsResourceWithSameName presentResource =
                                sameResourceNameList.get(resourceIndex);
                        if (!presentResource.getIndices().contains(j)) {
                            presentResource.getIndices().add(j);
                        }
                    } else {
                        sameResourceNameList.add(resource);
                        resource.getIndices().add(i);
                        resource.getIndices().add(j);
                    }
                }
            }
        }
        return sameResourceNameList;
    }

    /**
     * Method to check whether storage account registry
     * contains entries with same account names
     * but different URL text.
     * @param nameList
     * @return
     */
    public static boolean chkDuplicateUsingSet(List<String> nameList) {
        Set<String> nameSet = new HashSet<String>(nameList);
        return nameSet.size() < nameList.size();
    }

    /**
     * Method returns string which has concatenation of
     * application insights resource's name and subscription name
     * for display purpose.
     * @param resourceName
     * @param subName
     * @return String
     */
    public static String concatenateRsrcAndSubName(String resourceName, String subName) {
        String resourceAndSub = resourceName + " (" + subName + ")";
        return resourceAndSub;
    }

    /**
     * Method returns index of application insight resource from registry,
     * which has same instrumentation key.
     * @param key
     * @return
     */
    public static int getResourceIndexAsPerKey(String key) {
        List<ApplicationInsightsResource> resourceList = getAppInsightsResrcList();
        for (int i = 0; i < resourceList.size(); i++) {
            if (key.equals(resourceList.get(i).getInstrumentationKey())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Method returns instrumentation key of application insight resource of specific index.
     * @param index
     * @return
     */
    public static String getKeyAsPerIndex(int index) {
        List<ApplicationInsightsResource> resourceList = getAppInsightsResrcList();
        // to do check index exists or not
        return resourceList.get(index).getInstrumentationKey();
    }

    /**
     * Get list of application insight resources associated with particular subscription.
     * @param subId
     * @return
     */
    public static List<ApplicationInsightsResource> getResourceListAsPerSub(String subId) {
        List<ApplicationInsightsResource> subWiseList = new ArrayList<ApplicationInsightsResource>();
        if (subId != null && !subId.isEmpty()) {
            List<ApplicationInsightsResource> resourceList = getAppInsightsResrcList();
            for (ApplicationInsightsResource resource : resourceList) {
                if (resource.getSubscriptionId().equalsIgnoreCase(subId)) {
                    subWiseList.add(resource);
                }
            }
        }
        return subWiseList;
    }

    /**
     * Prepare list of ApplicationInsightsResource using list of Resource.
     * @param resourceList
     * @param sub
     * @return
     */
    public static List<ApplicationInsightsResource> prepareAppResListFromRes(
            List<ApplicationInsightsComponent> resourceList, Subscription sub) {
        List<ApplicationInsightsResource> list = new ArrayList<ApplicationInsightsResource>();
        for (ApplicationInsightsComponent resource : resourceList) {
            ApplicationInsightsResource resourceToAdd = new ApplicationInsightsResource(resource, sub, true);
            list.add(resourceToAdd);
        }
        return list;
    }

    public static List<ApplicationInsightsResource> getAddedResources() {
        // return manually added resources
        List<ApplicationInsightsResource> list = new ArrayList<ApplicationInsightsResource>();
        List<ApplicationInsightsResource> resourceList = getAppInsightsResrcList();
        for (ApplicationInsightsResource resource : resourceList) {
            if (resource.getSubscriptionId().equalsIgnoreCase("(Unknown)")) {
                list.add(resource);
            }
        }
        return list;
    }
}
