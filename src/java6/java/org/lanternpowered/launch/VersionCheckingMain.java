/*
 * This file is part of LanternServer, licensed under the MIT License (MIT).
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the Software), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.lanternpowered.launch;

import java.awt.GraphicsEnvironment;

import javax.swing.JOptionPane;

public class VersionCheckingMain {

    private static final String REQUIRED_VERSION = "1.8.0_40";
    private static final String ERROR_MESSAGE = "We have detected that you are JRE version %s, which is out of date!\n"
            + "In order to run LanternServer, you **must** be running JRE version %s (or above).\n"
            + "Previous builds of JRE version 8 will not work with the LanternServer.\n"
            + "This can be downloaded from Oracle: http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html";

    public static void main(String[] args) throws Exception {
        final String current = System.getProperty("java.version");
        if (getVersionValue(current) < getVersionValue(REQUIRED_VERSION)) {
            final String error = String.format(ERROR_MESSAGE, current, REQUIRED_VERSION);
            if (!GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(null, error, "PEBKACException!", JOptionPane.ERROR_MESSAGE);
            }
            throw new RuntimeException(error);
        }
        LanternLaunch.main(args);
    }

    /**
     * Calculates a double value based on a Java version string such that a
     * higher version will produce a higher value
     *
     * @param version The Java version
     * @return The double value of the Java version
     */
    private static double getVersionValue(String version) {
        // Get rid of any dashes, such as those in early access versions which have "-ea" on the end of the version
        if (version.contains("-")) {
            version = version.substring(0, version.indexOf('-'));
        }
        // Replace underscores with periods for easier String splitting
        version = version.replace('_', '.');
        // Ignore the '1.' in the version string since it no longer has any
        // meaning because it got removed in Java 9+
        if (version.startsWith("1.")) {
            version = version.substring(2);
        }
        // Split the version up into parts
        final String[] versionParts = version.split("\\.");
        double versionValue = 0;
        for (int i = 0; i < versionParts.length; i++) {
            try {
                final int part = Integer.valueOf(versionParts[i]);
                // first value is before zero, remaining values are 3 digits each, shifted behind zero
                versionValue += i == 0 ? part : (double) part / Math.pow(1000.0, i);
            } catch (NumberFormatException ignored) {
            }
        }
        return versionValue;
    }
}
