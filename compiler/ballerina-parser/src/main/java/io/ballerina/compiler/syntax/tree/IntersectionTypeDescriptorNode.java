/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.syntax.tree;

import io.ballerina.compiler.internal.parser.tree.STNode;

import java.util.Objects;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class IntersectionTypeDescriptorNode extends TypeDescriptorNode {

    public IntersectionTypeDescriptorNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Node leftTypeDesc() {
        return childInBucket(0);
    }

    public Token bitwiseAndToken() {
        return childInBucket(1);
    }

    public Node rightTypeDesc() {
        return childInBucket(2);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(NodeTransformer<T> visitor) {
        return visitor.transform(this);
    }

    @Override
    protected String[] childNames() {
        return new String[]{
                "leftTypeDesc",
                "bitwiseAndToken",
                "rightTypeDesc"};
    }

    public IntersectionTypeDescriptorNode modify(
            Node leftTypeDesc,
            Token bitwiseAndToken,
            Node rightTypeDesc) {
        if (checkForReferenceEquality(
                leftTypeDesc,
                bitwiseAndToken,
                rightTypeDesc)) {
            return this;
        }

        return NodeFactory.createIntersectionTypeDescriptorNode(
                leftTypeDesc,
                bitwiseAndToken,
                rightTypeDesc);
    }

    public IntersectionTypeDescriptorNodeModifier modify() {
        return new IntersectionTypeDescriptorNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class IntersectionTypeDescriptorNodeModifier {
        private final IntersectionTypeDescriptorNode oldNode;
        private Node leftTypeDesc;
        private Token bitwiseAndToken;
        private Node rightTypeDesc;

        public IntersectionTypeDescriptorNodeModifier(IntersectionTypeDescriptorNode oldNode) {
            this.oldNode = oldNode;
            this.leftTypeDesc = oldNode.leftTypeDesc();
            this.bitwiseAndToken = oldNode.bitwiseAndToken();
            this.rightTypeDesc = oldNode.rightTypeDesc();
        }

        public IntersectionTypeDescriptorNodeModifier withLeftTypeDesc(
                Node leftTypeDesc) {
            Objects.requireNonNull(leftTypeDesc, "leftTypeDesc must not be null");
            this.leftTypeDesc = leftTypeDesc;
            return this;
        }

        public IntersectionTypeDescriptorNodeModifier withBitwiseAndToken(
                Token bitwiseAndToken) {
            Objects.requireNonNull(bitwiseAndToken, "bitwiseAndToken must not be null");
            this.bitwiseAndToken = bitwiseAndToken;
            return this;
        }

        public IntersectionTypeDescriptorNodeModifier withRightTypeDesc(
                Node rightTypeDesc) {
            Objects.requireNonNull(rightTypeDesc, "rightTypeDesc must not be null");
            this.rightTypeDesc = rightTypeDesc;
            return this;
        }

        public IntersectionTypeDescriptorNode apply() {
            return oldNode.modify(
                    leftTypeDesc,
                    bitwiseAndToken,
                    rightTypeDesc);
        }
    }
}
