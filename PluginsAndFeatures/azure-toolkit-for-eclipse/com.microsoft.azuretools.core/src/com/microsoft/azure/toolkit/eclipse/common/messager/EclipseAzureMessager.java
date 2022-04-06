/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.common.messager;

import com.microsoft.azure.toolkit.lib.common.messager.IAzureMessage;
import com.microsoft.azure.toolkit.lib.common.messager.IAzureMessager;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskContext;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azuretools.core.Activator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

public class EclipseAzureMessager implements IAzureMessager {
    @Override
    public boolean show(IAzureMessage raw) {
        if (raw.getPayload() instanceof Throwable) {
            Activator.getDefault().log("caught an error by messager", ((Throwable) raw.getPayload()));
        }
        switch (raw.getType()) {
            case ALERT:
            case CONFIRM:
                final String title = Optional.ofNullable(raw.getTitle()).orElse(DEFAULT_TITLE);
                MessageDialog.openConfirm(null, title, raw.getContent());
                return true;
            default:
        }
        final AzureTask<?> task = AzureTaskContext.current().getTask();
        final Boolean backgrounded = Optional.ofNullable(task).map(AzureTask::getBackgrounded).orElse(null);
        final EclipseAzureMessage message = new EclipseAzureMessage(raw);
        if (Objects.equals(backgrounded, Boolean.FALSE) && message.getType() == IAzureMessage.Type.ERROR) {
            this.showErrorDialog(message);
        } else {
            this.showNotification(message);
        }
        return true;
    }

    private void showErrorDialog(@Nonnull EclipseAzureMessage message) {
        AzureTaskManager.getInstance().runLater(() -> new EclipseMessageDialog(message, Display.getCurrent().getActiveShell()).open());
    }

    private void showNotification(@Nonnull EclipseAzureMessage message) {
        AzureTaskManager.getInstance().runLater(() -> new EclipseMessageNotification(message, Display.getCurrent()).open());
    }
}
