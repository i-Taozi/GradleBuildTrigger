/*
 * 				Firetweet - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.getlantern.firetweet.loader.support;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import org.getlantern.firetweet.model.ParcelableStatus;

import java.util.List;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import static org.getlantern.firetweet.util.Utils.isFiltered;

public class RetweetsOfMeLoader extends TwitterAPIStatusesLoader {

    private int mTotalItemsCount;

    public RetweetsOfMeLoader(final Context context, final long accountId, final long sinceId, final long maxId,
                              final List<ParcelableStatus> data, final String[] savedStatusesArgs,
                              final int tabPosition, boolean fromUser) {
        super(context, accountId, sinceId, maxId, data, savedStatusesArgs, tabPosition, fromUser);
    }

    public int getTotalItemsCount() {
        return mTotalItemsCount;
    }

    @NonNull
    @Override
    protected ResponseList<Status> getStatuses(@NonNull final Twitter twitter, final Paging paging) throws TwitterException {
        if (twitter == null) return null;
        final ResponseList<Status> statuses = twitter.getRetweetsOfMe(paging);
        if (mTotalItemsCount == -1 && !statuses.isEmpty()) {
            final User user = statuses.get(0).getUser();
            if (user != null) {
                mTotalItemsCount = user.getStatusesCount();
            }
        }
        return statuses;
    }

    @Override
    protected boolean shouldFilterStatus(final SQLiteDatabase database, final ParcelableStatus status) {
        return isFiltered(database, -1, status.text_plain, status.text_html, status.source, -1);
    }
}
