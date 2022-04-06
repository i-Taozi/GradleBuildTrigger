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

package org.getlantern.firetweet.activity.support;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MenuItem;

import org.getlantern.firetweet.Constants;
import org.getlantern.firetweet.activity.iface.IControlBarActivity;
import org.getlantern.firetweet.app.FiretweetApplication;
import org.getlantern.firetweet.fragment.iface.IBaseFragment.SystemWindowsInsetsCallback;
import org.getlantern.firetweet.fragment.iface.IBasePullToRefreshFragment;
import org.getlantern.firetweet.util.AsyncTwitterWrapper;
import org.getlantern.firetweet.util.MessagesManager;
import org.getlantern.firetweet.util.ThemeUtils;
import org.getlantern.firetweet.view.iface.IExtendedView.OnFitSystemWindowsListener;

import java.util.ArrayList;

@SuppressLint("Registered")
public class BaseActionBarActivity extends ThemedActionBarActivity implements Constants,
        OnFitSystemWindowsListener, SystemWindowsInsetsCallback, IControlBarActivity {

    private boolean mInstanceStateSaved, mIsVisible, mIsOnTop;

    private Rect mSystemWindowsInsets;
    private ArrayList<ControlBarOffsetListener> mControlBarOffsetListeners = new ArrayList<>();

    public MessagesManager getMessagesManager() {
        return getFiretweetApplication() != null ? getFiretweetApplication().getMessagesManager() : null;
    }

    @Override
    public int getThemeColor() {
        return ThemeUtils.getUserAccentColor(this);
    }

    @Override
    public int getThemeResourceId() {
        return ThemeUtils.getThemeResource(this);
    }

    public FiretweetApplication getFiretweetApplication() {
        return (FiretweetApplication) getApplication();
    }

    public AsyncTwitterWrapper getTwitterWrapper() {
        return getFiretweetApplication() != null ? getFiretweetApplication().getTwitterWrapper() : null;
    }

    public boolean isOnTop() {
        return mIsOnTop;
    }

    public boolean isVisible() {
        return mIsVisible;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case MENU_BACK: {
                onBackPressed();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void startActivity(final Intent intent) {
        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(final Intent intent, final int requestCode) {
        super.startActivityForResult(intent, requestCode);
    }

    protected IBasePullToRefreshFragment getCurrentPullToRefreshFragment() {
        return null;
    }

    protected boolean isStateSaved() {
        return mInstanceStateSaved;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPause() {
        mIsOnTop = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mInstanceStateSaved = false;
        mIsOnTop = true;
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        mInstanceStateSaved = true;
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mIsVisible = true;
        final MessagesManager manager = getMessagesManager();
        if (manager != null) {
            manager.addMessageCallback(this);
        }
    }

    @Override
    protected void onStop() {
        mIsVisible = false;
        final MessagesManager manager = getMessagesManager();
        if (manager != null) {
            manager.removeMessageCallback(this);
        }
        super.onStop();
    }


    @Override
    public boolean getSystemWindowsInsets(Rect insets) {
        if (mSystemWindowsInsets == null) return false;
        insets.set(mSystemWindowsInsets);
        return true;
    }

    @Override
    public void onFitSystemWindows(Rect insets) {
        mSystemWindowsInsets = new Rect(insets);
        notifyControlBarOffsetChanged();
    }

    @Override
    public void setControlBarOffset(float offset) {

    }

    @Override
    public void setControlBarVisibleAnimate(boolean visible) {

    }

    @Override
    public float getControlBarOffset() {
        return 0;
    }

    @Override
    public int getControlBarHeight() {
        return 0;
    }

    @Override
    public void notifyControlBarOffsetChanged() {
        final float offset = getControlBarOffset();
        for (final ControlBarOffsetListener l : mControlBarOffsetListeners) {
            l.onControlBarOffsetChanged(this, offset);
        }
    }

    @Override
    public void registerControlBarOffsetListener(ControlBarOffsetListener listener) {
        mControlBarOffsetListeners.add(listener);
    }

    @Override
    public void unregisterControlBarOffsetListener(ControlBarOffsetListener listener) {
        mControlBarOffsetListeners.remove(listener);
    }
}
