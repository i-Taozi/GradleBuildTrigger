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

import java.util.ArrayList;
import java.util.List;

/**
 * Returns true if any of it's sub-conditions return true.
 */
@SuppressWarnings("PMD.ShortClassName")
public class Or
	implements ConditionGroup, Cloneable
{
	private static final long serialVersionUID = 4475481127736826653L;

	private List<Condition> conditions;

	public Or()
	{
		this(null);
	}

	public Or(List<Condition> conditions)
	{
		this.conditions = conditions;
	}

	@Override
	public List<Condition> getConditions()
	{
		return conditions;
	}

	@Override
	public void setConditions(List<Condition> conditions)
	{
		this.conditions = conditions;
	}

	/**
	 * Returns true if any of the contained conditions is true.
	 *
	 * This implies that it returns false if the contained conditions are either null or empty.
	 *
	 * @param element the object to be evaluated.
	 * @return true if any of the contained conditions is true.
	 */
	@Override
	public boolean isTrue(Object element)
	{
		if(conditions != null)
		{
			for(Condition condition : conditions)
			{
				if(condition.isTrue(element))
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Or clone()
		throws CloneNotSupportedException
	{
		Or result = (Or) super.clone();
		if(result.conditions != null)
		{
			List<Condition> clonedConditions = new ArrayList<>(conditions.size());
			for(Condition condition : result.conditions)
			{
				clonedConditions.add(condition.clone());
			}
			result.conditions = clonedConditions;
		}
		return result;
	}


	@Override
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		if(conditions == null || conditions.isEmpty())
		{
			result.append("false");
		}
		else
		{
			result.append('(');
			boolean first = true;
			for(Condition condition : conditions)
			{
				if(first)
				{
					first = false;
				}
				else
				{
					result.append(" || ");
				}
				result.append(condition);
			}
			result.append(')');
		}
		return result.toString();
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;

		final Or or = (Or) o;

		return !(conditions != null ? !conditions.equals(or.conditions) : or.conditions != null);
	}

	@Override
	public int hashCode()
	{
		return (conditions != null ? conditions.hashCode() : 0);
	}
}
