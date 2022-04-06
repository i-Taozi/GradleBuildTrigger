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

package org.getlantern.firetweet.util.content;

import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;

import org.getlantern.firetweet.FiretweetConstants;

public final class SupportFragmentReloadCursorObserver extends ContentObserver implements FiretweetConstants {

	private final Fragment mFragment;
	private final int mLoaderId;
	private final LoaderCallbacks<Cursor> mCallback;

	public SupportFragmentReloadCursorObserver(final Fragment fragment, final int loaderId,
			final LoaderCallbacks<Cursor> callback) {
		super(createHandler());
		mFragment = fragment;
		mLoaderId = loaderId;
		mCallback = callback;
	}

	@Override
	public void onChange(final boolean selfChange) {
		onChange(selfChange, null);
	}

	@Override
	public void onChange(final boolean selfChange, final Uri uri) {
		if (mFragment == null || mFragment.getActivity() == null || mFragment.isDetached()) return;
		// Handle change.
		mFragment.getLoaderManager().restartLoader(mLoaderId, null, mCallback);
	}

	private static Handler createHandler() {
		if (Thread.currentThread().getId() != 1) return null;
		return new Handler();
	}
}
