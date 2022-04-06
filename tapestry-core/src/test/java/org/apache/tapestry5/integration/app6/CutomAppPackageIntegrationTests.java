// Copyright 2009, 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.integration.app6;

import org.apache.tapestry5.integration.TapestryCoreTestCase;
import org.testng.annotations.Test;

/**
 * Additional integration tests that do not fit with the main group due to the need for special configuration.
 */
public class CutomAppPackageIntegrationTests extends TapestryCoreTestCase
{

    /** TAP5-815 */
    @Test
    public void asset_protection()
    {
        openLinks("Asset Protection Demo");
        clickAndWait("link=Show CSS");
        assertTextPresent("//Some CSS");
    }

}
