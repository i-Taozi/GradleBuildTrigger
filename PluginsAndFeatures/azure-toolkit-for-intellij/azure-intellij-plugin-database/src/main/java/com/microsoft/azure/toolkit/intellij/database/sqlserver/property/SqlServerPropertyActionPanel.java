/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.database.sqlserver.property;

import lombok.Getter;

import javax.swing.*;

public class SqlServerPropertyActionPanel extends JPanel {
    @Getter
    private JButton saveButton;
    @Getter
    private JButton discardButton;
    private JPanel rootPanel;
}
