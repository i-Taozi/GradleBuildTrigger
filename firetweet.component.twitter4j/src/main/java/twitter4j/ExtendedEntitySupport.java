/*
 * Firetweet - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package twitter4j;

/**
 * @author Mariotaku Lee
 * @since Twitter4J 2.2.5
 */
public interface ExtendedEntitySupport extends EntitySupport {

	/**
	 * Returns an array of MediaEntities if media are available in the tweet,
	 * or null if no media is included in the tweet.
	 * 
	 * @return an array of MediaEntities.
	 * @since Twitter4J 2.2.3
	 */
	MediaEntity[] getExtendedMediaEntities();

}
