/*
 * Copyright 2015-2018 Canoo Engineering AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.canoo.dp.impl.platform.projector.server.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceBundleTranslator implements Translator {

    private final String baseName;

    private final String encoding;

    public ResourceBundleTranslator(String baseName) {
        this(baseName, "utf-8");
    }

    public ResourceBundleTranslator(String baseName, String encoding) {
        this.baseName = baseName;
        this.encoding = encoding;
    }


    @Override
    public String translate(Locale locale, String key, Object... args) {
        return ResourceBundle.getBundle(baseName, locale, new EncodingResourceBundleControl(encoding)).getString(key);
    }
}

