/*
 * Copyright 2017 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.iceberg.expressions;

public class And implements Expression {
  private final Expression left;
  private final Expression right;

  And(Expression left, Expression right) {
    this.left = left;
    this.right = right;
  }

  public Expression left() {
    return left;
  }

  public Expression right() {
    return right;
  }

  @Override
  public Operation op() {
    return Expression.Operation.AND;
  }

  @Override
  public Expression negate() {
    // not(and(a, b)) => or(not(a), not(b))
    return Expressions.or(left.negate(), right.negate());
  }

  @Override
  public String toString() {
    return String.format("(%s and %s)", left, right);
  }
}
