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
package org.lanternpowered.server.script.transformer;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Joiner;
import org.lanternpowered.api.script.Import;
import org.lanternpowered.server.script.ScriptFunctionMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class ScriptTransformerContext {

    @Nullable private final String asset;
    private String className;
    private final Map<String, Import> imports = new HashMap<>();
    @Nullable private Class<?> superClass;
    private final Set<Class<?>> interfaces = new HashSet<>();
    private final Set<String> dependencies = new HashSet<>();
    @Nullable private final ScriptFunctionMethod functionMethod;
    private String scriptBody;

    public ScriptTransformerContext(String className, String scriptBody, @Nullable ScriptFunctionMethod functionMethod,
            @Nullable String asset) {
        this.functionMethod = functionMethod;
        this.scriptBody = scriptBody;
        this.className = className;
        this.asset = asset;
    }

    /**
     * Adds a {@link Import} to the script.
     *
     * @param theImport The import
     * @return Whether the import was successfully added, and that there
     *         is none present with the same name
     */
    public boolean addImport(Import theImport) {
        checkNotNull(theImport, "theImport");
        final String value = theImport.getValue();
        final int index = value.lastIndexOf('.');
        final String name = index == -1 ? value : value.substring(index + 1);
        if (this.imports.containsKey(name)) {
            return false;
        }
        this.imports.put(name, theImport);
        return true;
    }

    public void addInterface(Class<?> iface) {
        this.interfaces.add(iface);
    }

    /**
     * Gets the asset path that the resource is being compiled
     * from. If present.
     *
     * @return The asset
     */
    public Optional<String> getAsset() {
        return Optional.ofNullable(this.asset);
    }

    public Set<Class<?>> getInterfaces() {
        return Collections.unmodifiableSet(this.interfaces);
    }

    public void addDependency(String asset) {
        this.dependencies.add(checkNotNull(asset, "asset"));
    }

    public void setSuperClass(@Nullable Class<?> superClass) {
        this.superClass = superClass;
    }

    public String compile() {
        final StringBuilder builder = new StringBuilder();

        final String simpleClassName;
        int index = this.className.lastIndexOf('.');
        if (index != -1) {
            builder.append("package ").append(this.className.substring(0, index)).append("\n\n");
            simpleClassName = this.className.substring(index + 1);
        } else {
            simpleClassName = this.className;
        }

        final List<Import> imports0 = this.imports.values().stream()
                .filter(Import::isStatic)
                .collect(Collectors.toList());
        if (!imports0.isEmpty()) {
            for (Import import0 : imports0) {
                builder.append("import static " ).append(import0.getValue()).append("\n");
            }
            builder.append("\n");
        }

        final List<Import> imports1 = new ArrayList<>(this.imports.values());
        imports1.removeAll(imports0);
        if (!imports1.isEmpty()) {
            for (Import import1 : imports1) {
                builder.append("import " ).append(import1.getValue()).append("\n");
            }
            builder.append("\n");
        }

        builder.append("public class ").append(simpleClassName);
        if (this.superClass != null) {
            builder.append(" extends ").append(this.superClass.getName());
        }
        if (!this.interfaces.isEmpty()) {
            builder.append(" implements ").append(Joiner.on(", ").join(this.interfaces.stream()
                    .map(Class::getName).collect(Collectors.toList())));
        }
        builder.append(" {\n");

        for (String line : this.scriptBody.split("\n")) {
            builder.append("    ").append(line).append("\n");
        }

        builder.append("}\n");
        return builder.toString();
    }

    public String getScriptBody() {
        return this.scriptBody;
    }

    public void setScriptBody(String scriptBody) {
        this.scriptBody = scriptBody;
    }

    public Optional<ScriptFunctionMethod> getFunctionMethod() {
        return Optional.ofNullable(this.functionMethod);
    }

    public Set<String> getDependencies() {
        return this.dependencies;
    }
}
