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

package de.huxhorn.sulky.swing;

import de.huxhorn.sulky.generics.GenericWrapper;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * A wrapper for PropertyChangeListener that ensures that the wrapped listeners propertyChange method is invoked
 * on the EventDispatchThread.
 */
public class PropertyChangeWrapper
	extends GenericWrapper<PropertyChangeListener>
	implements PropertyChangeListener
{
	public PropertyChangeWrapper(PropertyChangeListener wrapped)
	{
		super(wrapped);
	}

	@Override
	public void propertyChange(final PropertyChangeEvent evt)
	{
		final PropertyChangeListener wrapped = getWrapped();
		if(EventQueue.isDispatchThread())
		{
			wrapped.propertyChange(evt);
		}
		else
		{
			EventQueue.invokeLater(new PropertyChangeRunnable(evt));
		}
	}

	private class PropertyChangeRunnable
		implements Runnable
	{
		private final PropertyChangeEvent event;

		PropertyChangeRunnable(PropertyChangeEvent event)
		{
			this.event=event;
		}

		@Override
		public void run()
		{
			getWrapped().propertyChange(event);
		}
	}
}
