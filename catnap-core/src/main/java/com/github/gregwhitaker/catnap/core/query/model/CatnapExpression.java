/*
 * Copyright 2016 Greg Whitaker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.gregwhitaker.catnap.core.query.model;

/**
 * Generic expression in the Catnap query format.
 */
public abstract class CatnapExpression implements Expression {
    protected String field;
    protected Operator operator;
    protected String valueToCheck;

    /**
     * Creates a new instance of a Catnap Expression.
     *
     * @param field name of the field to evaluate
     * @param operator operator to evaluate
     * @param valueToCheck value to evaluate instances of the field's value against
     */
    public CatnapExpression(String field, Operator operator, String valueToCheck) {
        this.field = field;
        this.operator = operator;
        this.valueToCheck = valueToCheck;
    }

    @Override
    public String getField() {
        return field;
    }

    @Override
    public Operator getOperator() {
        return operator;
    }

    @Override
    public String getValue() {
        return valueToCheck;
    }
}
