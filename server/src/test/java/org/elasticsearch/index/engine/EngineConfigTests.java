/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.index.engine;

import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.seqno.RetentionLeases;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.IndexSettingsModule;

public class EngineConfigTests extends ESTestCase {

    public void testOptimizeAutoGeneratedIdsSettingDeprecation() {
        boolean optimizeAutoGeneratedIds = randomBoolean();
        Settings.Builder builder = Settings.builder()
            .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
            .put(EngineConfig.INDEX_OPTIMIZE_AUTO_GENERATED_IDS.getKey(), optimizeAutoGeneratedIds);
        IndexSettings indexSettings = IndexSettingsModule.newIndexSettings("index1", builder.build());

        EngineConfig config = new EngineConfig(
                null,
                null,
                null,
                indexSettings,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        assertEquals(optimizeAutoGeneratedIds, config.isAutoGeneratedIDsOptimizationEnabled());
        assertSettingDeprecationsAndWarnings(new Setting<?>[]{EngineConfig.INDEX_OPTIMIZE_AUTO_GENERATED_IDS});
    }

}
