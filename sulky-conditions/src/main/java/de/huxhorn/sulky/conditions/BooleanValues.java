/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2017 Joern Huxhorn
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
 * Copyright 2007-2017 Joern Huxhorn
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

package de.huxhorn.sulky.conditions;

public final class BooleanValues
		implements Condition, Cloneable
{
	private static final long serialVersionUID = 1780367398890411212L;

	/**
	 * true singleton
	 */
	public static final BooleanValues TRUE = new BooleanValues(true);

	/**
	 * false singleton
	 */
	public static final BooleanValues FALSE = new BooleanValues(false);

	private final boolean value;
	private final transient String string;

	private BooleanValues(boolean b)
	{
		this.value = b;
		this.string = String.valueOf(b);
	}

	/**
	 * Returns either true or false, depending on the instance.
	 *
	 * @param element the element to be evaluated.
	 * @return either true or false, depending on the instance.
	 */
	@Override
	public boolean isTrue(Object element)
	{
		return value;
	}

	//@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="CN_IDIOM_NO_SUPER_CALL",justification="")
	@SuppressWarnings({"CloneDoesntCallSuperClone"})
	@Override
	public BooleanValues clone()
			throws CloneNotSupportedException
	{
		// this is not a bug! - Bad practice - clone method does not call super.clone()
		return this; //NOSONAR
	}

	private Object readResolve()
	{
		return getInstance(value);
	}

	@Override
	public String toString()
	{
		return string;
	}

	public static BooleanValues getInstance(boolean value)
	{
		return value ? TRUE : FALSE;
	}
}
