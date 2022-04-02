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

package de.huxhorn.sulky.sounds;

import java.util.Map;

public interface Sounds
{
	/**
	 * Plays the sound with the given name.
	 *
	 * If ignoreDuplicates is true, another instance
	 * of the same sound will not be
	 * added to the playlist while it is still played or waiting to be played.
	 *
	 * @param soundName the name of the sound to be played.
	 * @param ignoreDuplicates if true, another instance of the same sound will not be added to the playlist.
	 */
	void play(String soundName, boolean ignoreDuplicates);

	/**
	 * Shortcut for play(soundName, true).
	 *
	 * @param soundName the name of the sound to be played.
	 */
	void play(String soundName);

	void setMute(boolean mute);

	boolean isMute();

	Map<String, String> getSoundLocations();

	void setSoundLocations(Map<String, String> soundLocations);
}
