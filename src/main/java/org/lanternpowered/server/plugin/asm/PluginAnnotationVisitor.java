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
package org.lanternpowered.server.plugin.asm;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.objectweb.asm.Opcodes.ASM5;

import com.google.common.base.Preconditions;
import org.lanternpowered.server.plugin.InvalidPluginException;
import org.objectweb.asm.AnnotationVisitor;
import org.spongepowered.plugin.meta.PluginMetadata;

final class PluginAnnotationVisitor extends WarningAnnotationVisitor {

    private enum State {
        DEFAULT, AUTHORS, DEPENDENCIES
    }

    private final PluginMetadata metadata;

    private State state = State.DEFAULT;
    private boolean hasId;

    PluginAnnotationVisitor(String className) {
        super(ASM5, className);
        this.metadata = new PluginMetadata("unknown");
    }

    PluginMetadata getMetadata() {
        return this.metadata;
    }

    @Override
    String getAnnotation() {
        return "@Plugin";
    }

    private void checkState(State state) {
        Preconditions.checkState(this.state == state, "Expected state %s, but is %s", state, this.state);
    }

    @Override
    public void visit(String name, Object value) {
        if (this.state == State.AUTHORS) {
            this.metadata.addAuthor((String) value);
            return;
        }

        checkState(State.DEFAULT);
        checkNotNull(name, "name");
        switch (name) {
            case "id":
                this.hasId = true;
                this.metadata.setId((String) value);
                return;
            case "name":
                this.metadata.setName((String) value);
                return;
            case "version":
                this.metadata.setVersion((String) value);
                return;
            case "description":
                this.metadata.setDescription((String) value);
                return;
            case "url":
                this.metadata.setUrl((String) value);
                return;
            default:
                super.visit(name, value);
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String desc) {
        if (this.state == State.DEPENDENCIES) {
            return new DependencyAnnotationVisitor(this.className, this.metadata);
        }
        return super.visitAnnotation(name, desc);
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        checkNotNull(name, "name");
        switch (name) {
            case "authors":
                this.state = State.AUTHORS;
                return this;
            case "dependencies":
                this.state = State.DEPENDENCIES;
                return this;
            default:
                return super.visitArray(name);
        }
    }

    @Override
    public void visitEnd() {
        if (this.state != State.DEFAULT) {
            this.state = State.DEFAULT;
            return;
        }

        if (!this.hasId) {
            throw new InvalidPluginException("Plugin annotation is missing required element 'id'");
        }
    }

}
