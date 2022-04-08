/*
 * Radon - An open-source Java obfuscator
 * Copyright (C) 2019 ItzSomebody
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package me.itzsomebody.radon.dictionaries;

/**
 * String generation interface.
 *
 * @author ItzSomebody
 */
public interface Dictionary {
    /**
     * @param length the length the generated string should be.
     * @return generates string randomly.
     */
    String randomString(int length);

    /**
     * @param length the length the generated string should be.
     * @return generates unique string randomly.
     */
    String uniqueRandomString(int length);

    /**
     * @return next unique string.
     */
    String nextUniqueString();

    /**
     * @return last generated unique string. If non, null.
     */
    String lastUniqueString();

    /**
     * @return name of dictionary.
     */
    String getDictionaryName();

    void reset();

    Dictionary copy();
}
