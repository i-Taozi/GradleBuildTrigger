/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2014 Joern Huxhorn
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
 * Copyright 2007-2014 Joern Huxhorn
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

import java.util.List;

public final class Conditions
{
	static
	{
		// for the sake of coverage
		new Conditions();
	}

	private Conditions()
	{}

	/**
	 * Returns true if condition contains otherCondition.
	 *
	 * Conditions "contain" themselves so this method returns true if condition equals otherCondition.
	 *
	 * @param condition the condition to be checked for containment
	 * @param otherCondition the condition that might contain condition.
	 * @return true if condition contains otherCondition.
	 */
	public static boolean contains(Condition condition, Condition otherCondition)
	{
		if(condition == null)
		{
			return false;
		}
		if(condition.equals(otherCondition))
		{
			return true;
		}
		if(condition instanceof ConditionWrapper)
		{
			ConditionWrapper conditionWrapper = (ConditionWrapper) condition;
			Condition wrappedCondition = conditionWrapper.getCondition();
			if(wrappedCondition == null)
			{
				return otherCondition == null;
			}
			return contains(wrappedCondition, otherCondition);
		}
		else if(condition instanceof ConditionGroup)
		{
			ConditionGroup conditionGroup = (ConditionGroup) condition;
			List<Condition> conditions = conditionGroup.getConditions();
			if(conditions == null)
			{
				return false;
			}
			for(Condition c : conditions)
			{
				if(contains(c, otherCondition))
				{
					return true;
				}
			}
		}
		return false;
	}
}
