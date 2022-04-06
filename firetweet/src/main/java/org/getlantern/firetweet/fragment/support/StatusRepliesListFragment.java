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

package org.getlantern.firetweet.fragment.support;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.Loader;

import org.getlantern.firetweet.loader.support.StatusRepliesLoader;
import org.getlantern.firetweet.model.ParcelableStatus;

import java.util.List;

public class StatusRepliesListFragment extends StatusesSearchFragment {

    @Override
    public Loader<List<ParcelableStatus>> onCreateStatusesLoader(final Context context,
                                                                 final Bundle args,
                                                                 final boolean fromUser) {
        final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
        final String screenName = args.getString(EXTRA_SCREEN_NAME);
        final long statusId = args.getLong(EXTRA_STATUS_ID, -1);
        final long maxId = args.getLong(EXTRA_MAX_ID, -1);
        final long sinceId = args.getLong(EXTRA_SINCE_ID, -1);
        final int tabPosition = args.getInt(EXTRA_TAB_POSITION, -1);
        return new StatusRepliesLoader(getActivity(), accountId, screenName, statusId, maxId,
                sinceId, getAdapterData(), getSavedStatusesFileArgs(), tabPosition, fromUser);
    }

    @Override
    protected String[] getSavedStatusesFileArgs() {
        final Bundle args = getArguments();
        if (args == null) return null;
        final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
        final String screenName = args.getString(EXTRA_SCREEN_NAME);
        final long statusId = args.getLong(EXTRA_STATUS_ID);
        return new String[]{AUTHORITY_STATUS_REPLIES, "account" + accountId, "screen_name" + screenName,
                "status_id" + statusId};
    }

}
