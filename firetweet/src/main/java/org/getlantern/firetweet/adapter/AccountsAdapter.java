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

package org.getlantern.firetweet.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.mobeta.android.dslv.SimpleDragSortCursorAdapter;

import org.getlantern.firetweet.Constants;
import org.getlantern.firetweet.R;
import org.getlantern.firetweet.adapter.iface.IBaseAdapter;
import org.getlantern.firetweet.app.FiretweetApplication;
import org.getlantern.firetweet.model.ParcelableAccount;
import org.getlantern.firetweet.model.ParcelableAccount.Indices;
import org.getlantern.firetweet.provider.FiretweetDataStore.Accounts;
import org.getlantern.firetweet.util.MediaLoaderWrapper;
import org.getlantern.firetweet.view.holder.AccountViewHolder;

public class AccountsAdapter extends SimpleDragSortCursorAdapter implements Constants, IBaseAdapter {

    private final MediaLoaderWrapper mImageLoader;
    private final SharedPreferences mPreferences;

    private boolean mDisplayProfileImage;
    private int mChoiceMode;
    private boolean mSortEnabled;
    private Indices mIndices;

    public AccountsAdapter(final Context context) {
        super(context, R.layout.list_item_account, null, new String[]{Accounts.NAME},
                new int[]{android.R.id.text1}, 0);
        final FiretweetApplication application = FiretweetApplication.getInstance(context);
        mImageLoader = application.getMediaLoaderWrapper();
        mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public ParcelableAccount getAccount(int position) {
        final Cursor c = getCursor();
        if (c == null || c.isClosed() || !c.moveToPosition(position)) return null;
        return new ParcelableAccount(c, mIndices);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        final int color = cursor.getInt(mIndices.color);
        final AccountViewHolder holder = (AccountViewHolder) view.getTag();
        holder.screen_name.setText("@" + cursor.getString(mIndices.screen_name));
        holder.setAccountColor(color);
        if (mDisplayProfileImage) {
            mImageLoader.displayProfileImage(holder.profile_image, cursor.getString(mIndices.profile_image_url));
        } else {
            mImageLoader.cancelDisplayTask(holder.profile_image);
//            holder.profile_image.setImageResource(R.drawable.ic_profile_image_default);
        }
        final boolean isMultipleChoice = mChoiceMode == ListView.CHOICE_MODE_MULTIPLE
                || mChoiceMode == ListView.CHOICE_MODE_MULTIPLE_MODAL;
        holder.checkbox.setVisibility(isMultipleChoice ? View.VISIBLE : View.GONE);
        holder.setSortEnabled(mSortEnabled);
        super.bindView(view, context, cursor);
    }

    @Override
    public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
        final View view = super.newView(context, cursor, parent);
        final AccountViewHolder holder = new AccountViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public MediaLoaderWrapper getImageLoader() {
        return mImageLoader;
    }

    @Override
    public int getLinkHighlightOption() {
        return 0;
    }

    @Override
    public float getTextSize() {
        return 0;
    }

    @Override
    public boolean isDisplayNameFirst() {
        return false;
    }

    @Override
    public boolean isProfileImageDisplayed() {
        return mDisplayProfileImage;
    }


    @Override
    public boolean isShowAccountColor() {
        return false;
    }

    @Override
    public void setDisplayNameFirst(boolean nameFirst) {

    }

    public void setChoiceMode(final int mode) {
        if (mChoiceMode == mode) return;
        mChoiceMode = mode;
        notifyDataSetChanged();
    }

    @Override
    public void setDisplayProfileImage(final boolean display) {
        mDisplayProfileImage = display;
        notifyDataSetChanged();
    }

    @Override
    public void setLinkHighlightOption(String option) {

    }

    @Override
    public void setShowAccountColor(boolean show) {

    }

    @Override
    public void setTextSize(float textSize) {

    }

    @Override
    public Cursor swapCursor(final Cursor cursor) {
        if (cursor != null) {
            mIndices = new Indices(cursor);
        }
        return super.swapCursor(cursor);
    }

    public void setSortEnabled(boolean sortEnabled) {
        if (mSortEnabled == sortEnabled) return;
        mSortEnabled = sortEnabled;
        notifyDataSetChanged();
    }
}
