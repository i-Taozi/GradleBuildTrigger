/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.core.mvp.ui.base;

public class MvpUIHelperFactory {
    private MvpUIHelper mvpUIHelper;

    private MvpUIHelperFactory() {
    }

    private static final class MvpUIHelperFactoryHolder {
        private static final MvpUIHelperFactory INSTANCE = new MvpUIHelperFactory();
    }

    public static MvpUIHelperFactory getInstance() {
        return MvpUIHelperFactoryHolder.INSTANCE;
    }

    public void init(MvpUIHelper mvpUIHelper) {
        this.mvpUIHelper = mvpUIHelper;
    }

    public MvpUIHelper getMvpUIHelper() {
        return this.mvpUIHelper;
    }
}
