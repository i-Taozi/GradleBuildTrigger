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

package org.getlantern.firetweet.util;

import android.support.v4.app.Fragment;

import org.getlantern.firetweet.fragment.support.CardBrowserFragment;
import org.getlantern.firetweet.model.ParcelableStatus.ParcelableCardEntity;
import org.getlantern.firetweet.model.ParcelableStatus.ParcelableCardEntity.ParcelableValueItem;

/**
 * Created by mariotaku on 15/1/1.
 */
public abstract class TwitterCardFragmentFactory {

    public abstract Fragment createAnimatedGifFragment(ParcelableCardEntity card);

    public abstract Fragment createAudioFragment(ParcelableCardEntity card);

    public abstract Fragment createPlayerFragment(ParcelableCardEntity card);

    public static TwitterCardFragmentFactory getInstance() {
        return new TwitterCardFragmentFactoryImpl();
    }

    public static Fragment createGenericPlayerFragment(ParcelableCardEntity card) {
        final ParcelableValueItem player_url = ParcelableCardEntity.getValue(card, "player_url");
        return CardBrowserFragment.show((String) player_url.value);
    }
}
