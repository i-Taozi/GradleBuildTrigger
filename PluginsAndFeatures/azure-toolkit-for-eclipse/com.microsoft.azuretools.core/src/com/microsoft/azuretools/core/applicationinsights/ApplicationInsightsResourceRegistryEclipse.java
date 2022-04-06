/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.core.applicationinsights;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azuretools.authmanage.models.SubscriptionDetail;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import com.microsoft.applicationinsights.preference.ApplicationInsightsResource;
import com.microsoft.applicationinsights.preference.ApplicationInsightsResourceRegistry;
import com.microsoft.azure.management.applicationinsights.v2015_05_01.ApplicationInsightsComponent;
import com.microsoft.azuretools.azurecommons.helpers.AzureCmdException;
import com.microsoft.azuretools.core.Activator;
import com.microsoft.tooling.msservices.helpers.azure.sdk.AzureSDKManager;


public class ApplicationInsightsResourceRegistryEclipse {
    /**
     * Method updates application insights registry by adding, removing or updating resources.
     * @param client
     * @throws java.io.IOException
     * @throws RestOperationException
     * @throws AzureCmdException
     */
    public static void updateApplicationInsightsResourceRegistry(List<Subscription> subList) throws Exception {
        for (Subscription sub : subList) {
            if (sub.isSelected()) {
                try {
                    // fetch resources available for particular subscription
                    List<ApplicationInsightsComponent> resourceList = AzureSDKManager.getInsightsResources(sub.getId());

                    // Removal logic
                    List<ApplicationInsightsResource> registryList = ApplicationInsightsResourceRegistry
                            .getResourceListAsPerSub(sub.getId());
                    List<ApplicationInsightsResource> importedList = ApplicationInsightsResourceRegistry
                            .prepareAppResListFromRes(resourceList, sub);
                    List<String> inUsekeyList = getInUseInstrumentationKeys();
                    for (ApplicationInsightsResource registryRes : registryList) {
                        if (!importedList.contains(registryRes)) {
                            String key = registryRes.getInstrumentationKey();
                            int index = ApplicationInsightsResourceRegistry.getResourceIndexAsPerKey(key);
                            if (inUsekeyList.contains(key)) {
                                // key is used by project but not present in
                                // cloud,
                                // so make it as manually added resource and not
                                // imported.
                                ApplicationInsightsResource resourceToAdd = new ApplicationInsightsResource(key, key,
                                        Messages.unknown, Messages.unknown, Messages.unknown, Messages.unknown, false);
                                ApplicationInsightsResourceRegistry.getAppInsightsResrcList().set(index, resourceToAdd);
                            } else {
                                // key is not used by any project then delete
                                // it.
                                ApplicationInsightsResourceRegistry.getAppInsightsResrcList().remove(index);
                            }
                        }
                    }

                    // Addition logic
                    List<ApplicationInsightsResource> list = ApplicationInsightsResourceRegistry
                            .getAppInsightsResrcList();
                    for (ApplicationInsightsComponent resource : resourceList) {
                        ApplicationInsightsResource resourceToAdd = new ApplicationInsightsResource(resource, sub, true);
                        if (list.contains(resourceToAdd)) {
                            int index = ApplicationInsightsResourceRegistry
                                    .getResourceIndexAsPerKey(resource.instrumentationKey());
                            ApplicationInsightsResource objectFromRegistry = list.get(index);
                            if (!objectFromRegistry.isImported()) {
                                ApplicationInsightsResourceRegistry.getAppInsightsResrcList().set(index, resourceToAdd);
                            }
                        } else {
                            ApplicationInsightsResourceRegistry.getAppInsightsResrcList().add(resourceToAdd);
                        }
                    }
                } catch (Exception e) {
                    Activator.getDefault().log(String.format(Messages.aiListErr, sub.getName()), e);
                }
            }
        }
        ApplicationInsightsPreferences.save();
        ApplicationInsightsPreferences.setLoaded(true);
    }

    public static void keeepManuallyAddedList() {
        List<ApplicationInsightsResource> addedList = ApplicationInsightsResourceRegistry.getAddedResources();
        List<String> addedKeyList = new ArrayList<String>();
        for (ApplicationInsightsResource res : addedList) {
            addedKeyList.add(res.getInstrumentationKey());
        }
        List<String> inUsekeyList = getInUseInstrumentationKeys();
        for (String inUsekey : inUsekeyList) {
            if (!addedKeyList.contains(inUsekey)) {
                ApplicationInsightsResource resourceToAdd = new ApplicationInsightsResource(
                        inUsekey, inUsekey, Messages.unknown, Messages.unknown,
                        Messages.unknown, Messages.unknown, false);
                addedList.add(resourceToAdd);
            }
        }
        ApplicationInsightsResourceRegistry.setAppInsightsResrcList(addedList);
        ApplicationInsightsPreferences.save();
        ApplicationInsightsPreferences.setLoaded(true);
    }

    /**
     * Method scans all open Maven or Dynamic web projects form workspace
     * and prepare a list of instrumentation keys which are in use.
     * @return
     */
    public static List<String> getInUseInstrumentationKeys() {
        List<String> keyList = new ArrayList<String>();
        try {
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IWorkspaceRoot root = workspace.getRoot();
            for (IProject project : root.getProjects()) {
                if (project.isOpen() && WebPropertyTester.isWebProj(project)) {
                    String aiXMLPath;
                    if (project.hasNature(Messages.natMaven)) {
                        aiXMLPath = Messages.aiXMLPathMaven;
                    } else {
                        aiXMLPath = Messages.aiXMLPath;
                    }
                    AILibraryHandler handler = new AILibraryHandler();
                    IFile file = project.getFile(aiXMLPath);
                    if (file.exists()) {
                        handler.parseAIConfXmlPath(file.getLocation().toOSString());
                        String key = handler.getAIInstrumentationKey();
                        if (key != null && !key.isEmpty()) {
                            keyList.add(key);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Activator.getDefault().log(ex.getMessage(), ex);
        }
        return keyList;
    }
}
