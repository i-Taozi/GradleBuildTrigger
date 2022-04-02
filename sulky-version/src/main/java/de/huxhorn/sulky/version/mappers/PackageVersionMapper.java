/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2018 Joern Huxhorn
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright 2007-2018 Joern Huxhorn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.huxhorn.sulky.version.mappers;

import de.huxhorn.sulky.version.ClassStatisticMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@SuppressWarnings("PMD.AvoidThrowingNullPointerException") // target is Java 1.6
public class PackageVersionMapper
	implements ClassStatisticMapper
{
	@SuppressWarnings("PMD.UseDiamondOperator") // target is Java 1.6
	private final Map<String, Set<Character>> packageVersions=new HashMap<String, Set<Character>>();

	public Map<String, Set<Character>> getPackageVersions()
	{
		return packageVersions;
	}

	@Override
	@SuppressWarnings("PMD.UseDiamondOperator") // target is Java 1.6
	public void evaluate(String source, String packageName, String className, char majorVersion)
	{
		if(source == null)
		{
			throw new NullPointerException("'source' must not be null!");
		}
		if(packageName == null)
		{
			throw new NullPointerException("'packageName' must not be null!");
		}
		if(className == null)
		{
			throw new NullPointerException("'className' must not be null!");
		}

		Set<Character> versions = packageVersions.get(packageName);
		if(versions == null)
		{
			versions = new TreeSet<Character>();
			packageVersions.put(packageName, versions);
		}
		versions.add(majorVersion);
	}

	@Override
	public void reset()
	{
		packageVersions.clear();
	}
}
