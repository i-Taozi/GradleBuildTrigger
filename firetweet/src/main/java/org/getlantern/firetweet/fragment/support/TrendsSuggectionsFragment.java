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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.getlantern.firetweet.app.FiretweetApplication;
import org.getlantern.firetweet.provider.FiretweetDataStore.CachedTrends;
import org.getlantern.firetweet.util.AsyncTwitterWrapper;
import org.getlantern.firetweet.util.MultiSelectManager;
import org.getlantern.firetweet.util.message.TaskStateChangedEvent;

import static org.getlantern.firetweet.util.Utils.getDefaultAccountId;
import static org.getlantern.firetweet.util.Utils.getTableNameByUri;
import static org.getlantern.firetweet.util.Utils.openTweetSearch;

public class TrendsSuggectionsFragment extends BasePullToRefreshListFragment implements LoaderCallbacks<Cursor> {

    private MultiSelectManager mMultiSelectManager;
    private SharedPreferences mPreferences;

    private TrendsAdapter mTrendsAdapter;

    private long mAccountId;

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        super.onActivityCreated(savedInstanceState);
        mMultiSelectManager = getMultiSelectManager();
        mAccountId = getDefaultAccountId(getActivity());
        mTrendsAdapter = new TrendsAdapter(getActivity());
        setListAdapter(mTrendsAdapter);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        final Uri uri = CachedTrends.Local.CONTENT_URI;
        final String table = getTableNameByUri(uri);
        final String where = table != null ? CachedTrends.TIMESTAMP + " = " + "(SELECT " + CachedTrends.TIMESTAMP
                + " FROM " + table + " ORDER BY " + CachedTrends.TIMESTAMP + " DESC LIMIT 1)" : null;
        return new CursorLoader(getActivity(), uri, CachedTrends.COLUMNS, where, null, null);
    }

    @Override
    public void onListItemClick(final ListView l, final View v, final int position, final long id) {
        if (mMultiSelectManager.isActive()) return;
        final String trend = mTrendsAdapter.getItem(position - l.getHeaderViewsCount());
        if (trend == null) return;
        openTweetSearch(getActivity(), mAccountId, trend);
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        mTrendsAdapter.swapCursor(null);
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
        mTrendsAdapter.swapCursor(cursor);
    }

    @Override
    public void onRefresh() {
        if (isRefreshing()) return;
        final AsyncTwitterWrapper twitter = getTwitterWrapper();
        if (twitter == null) return;
        twitter.getLocalTrendsAsync(mAccountId, mPreferences.getInt(KEY_LOCAL_TRENDS_WOEID, 1));
    }

    @Override
    public void onStart() {
        super.onStart();
        getLoaderManager().restartLoader(0, null, this);
        final Bus bus = FiretweetApplication.getInstance(getActivity()).getMessageBus();
        bus.register(this);
    }

    @Override
    public void onStop() {
        final Bus bus = FiretweetApplication.getInstance(getActivity()).getMessageBus();
        bus.unregister(this);
        super.onStop();
    }

    @Subscribe
    public void notifyTaskStateChanged(TaskStateChangedEvent event) {
        updateRefreshState();
    }

    protected void updateRefreshState() {
        final AsyncTwitterWrapper twitter = getTwitterWrapper();
        if (twitter == null || !getUserVisibleHint()) return;
        setRefreshing(twitter.isLocalTrendsRefreshing());
    }

    static class TrendsAdapter extends SimpleCursorAdapter {
        private int mNameIdx;

        @Override
        public String getItem(int position) {
            final Cursor c = getCursor();
            if (c != null && !c.isClosed() && c.moveToPosition(position))
                return c.getString(mNameIdx);
            return null;
        }

        @Override
        public Cursor swapCursor(Cursor c) {
            if (c != null) {
                mNameIdx = c.getColumnIndex(CachedTrends.NAME);
            }
            return super.swapCursor(c);
        }

        public TrendsAdapter(final Context context) {
            super(context, android.R.layout.simple_list_item_1, null, new String[]{CachedTrends.NAME},
                    new int[]{android.R.id.text1}, 0);
        }

    }

}
