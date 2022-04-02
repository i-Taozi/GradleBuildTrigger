/*
 * Copyright (C) 2011-2020, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Geometric Regression Library (GeoRegression).
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

package georegression.struct.shapes;

import georegression.misc.GrlConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestRectangle2D_F64 {
	@Test
	void enforceExtents() {
		Rectangle2D_F64 a = new Rectangle2D_F64(-1,-2,50,10);
		a.enforceExtents();
		assertEquals(-1,a.p0.x, GrlConstants.TEST_F64);
		assertEquals(-2,a.p0.y, GrlConstants.TEST_F64);
		assertEquals(50,a.p1.x, GrlConstants.TEST_F64);
		assertEquals(10,a.p1.y, GrlConstants.TEST_F64);

		a = new Rectangle2D_F64(50,10,-1,-2);
		a.enforceExtents();
		assertEquals(-1,a.p0.x, GrlConstants.TEST_F64);
		assertEquals(-2,a.p0.y, GrlConstants.TEST_F64);
		assertEquals(50,a.p1.x, GrlConstants.TEST_F64);
		assertEquals(10,a.p1.y, GrlConstants.TEST_F64);
	}
}