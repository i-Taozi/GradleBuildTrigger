/*******************************************************************************
 * Copyright (c) 2015
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.common.resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This is the resource manager. It gives you resources.
 *
 * @author michael
 */
public class ResourceManager {
    private static IResourceProvider provider = null;

    public static void setProvider(IResourceProvider provider) {
        ResourceManager.provider = provider;
    }

    /**
     * Gets the file. Throws a io exception if the file does not exist.
     *
     * @param filename
     * @return
     * @throws IOException
     */
    public static InputStream getResourcesFileStream(String filename) throws IOException {
        ensureResourceProviderSet();
        return provider.getResourcesFileStream(filename);
    }

    public static OutputStream writeConfigurationFile(String filename) throws IOException {
        ensureResourceProviderSet();
        return provider.writeConfigurationFile(filename);
    }

    public static OutputStream writeUserFile(String filename) throws IOException {
        ensureResourceProviderSet();
        return provider.writeUserFile(filename);
    }

    private static void ensureResourceProviderSet() throws IOException {
        if (provider == null) {
            throw new IOException("No resource provider set.");
        }
    }

    /**
     * Gets a directory where all the content that is being created should be saved.
     *
     * @return The directory.
     */
    public static File getResourcesDirectory() {
        if (provider != null) {
            return provider.getResourcesDirectory();
        } else {
            return new File("");
        }
    }


}
