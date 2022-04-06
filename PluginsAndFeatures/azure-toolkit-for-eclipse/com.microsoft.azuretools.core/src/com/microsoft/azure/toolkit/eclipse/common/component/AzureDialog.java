/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.common.component;

import com.microsoft.azure.toolkit.ide.common.components.AzDialogWrapper;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AzureDialog<T> extends AzDialogWrapper<T> {
    protected OkActionListener<T> okActionListener;

    public AzureDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        this.setTitle(this.getDialogTitle());
        return super.createDialogArea(parent);
    }

    public void doOkAction() {
        try {
            if (Objects.nonNull(this.okActionListener)) {
                final T data = this.getForm().getValue();
                this.okActionListener.onOk(data);
            } else {
                super.doOkAction();
            }
        } catch (final Exception e) {
            AzureMessager.getMessager().error(e);
        }
    }

    protected List<AzureValidationInfo> doValidateAll() {
        final List<AzureValidationInfo> infos = this.getForm().getAllValidationInfos(true);
        return infos.stream().filter(i -> !i.isValid()).collect(Collectors.toList());
    }

    protected abstract String getDialogTitle();

    public void setOkActionListener(OkActionListener<T> listener) {
        this.okActionListener = listener;
    }

    @FunctionalInterface
    public interface OkActionListener<T> {
        void onOk(T data);
    }
}
