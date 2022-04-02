/*
 * Copyright (C) 2011-2018, Peter Abeles. All Rights Reserved.
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

package georegression.fitting.curves;

import georegression.fitting.FitShapeToPoints_F64;
import georegression.struct.curve.ConicGeneral_F64;
import georegression.struct.point.Point2D_F64;

/**
 * @author Peter Abeles
 */
public class TestFitConicA_F64 extends GeneralFitConic_F64 {
	@Override
	public FitShapeToPoints_F64<Point2D_F64, ConicGeneral_F64> createAlg() {
		return new FitConicA_F64();
	}
}