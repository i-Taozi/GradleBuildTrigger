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

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.getlantern.firetweet.Constants;
import org.getlantern.firetweet.R;
import org.getlantern.firetweet.fragment.iface.IMapFragment;
import org.getlantern.firetweet.fragment.support.GoogleMapFragment;
import org.getlantern.firetweet.fragment.support.WebMapFragment;
import org.getlantern.firetweet.util.ParseUtils;
import org.getlantern.firetweet.util.ThemeUtils;

public class GoogleMapViewerActivity extends BaseActionBarActivity implements Constants {

    @Override
    public int getThemeResourceId() {
        return ThemeUtils.getViewerThemeResource(this);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_google_maps_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case MENU_HOME: {
                onBackPressed();
                break;
            }
            case MENU_CENTER: {
                final Fragment fragment = getSupportFragmentManager().findFragmentById(android.R.id.content);
                if (!(fragment instanceof IMapFragment)) {
                    break;
                }
                ((IMapFragment) fragment).center();
                break;
            }
        }
        return true;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Uri uri = getIntent().getData();
        if (uri == null || !AUTHORITY_MAP.equals(uri.getAuthority())) {
            finish();
            return;
        }
        final Bundle bundle = new Bundle();
        final double latitude = ParseUtils.parseDouble(uri.getQueryParameter(QUERY_PARAM_LAT), Double.NaN);
        final double longitude = ParseUtils.parseDouble(uri.getQueryParameter(QUERY_PARAM_LNG), Double.NaN);
        if (Double.isNaN(latitude) || Double.isNaN(longitude)) {
            finish();
            return;
        }
        try {
            bundle.putDouble(EXTRA_LATITUDE, latitude);
            bundle.putDouble(EXTRA_LONGITUDE, longitude);
        } catch (final NumberFormatException e) {
            finish();
            return;
        }
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        final Fragment fragment = isNativeMapSupported() ? new GoogleMapFragment() : new WebMapFragment();
        fragment.setArguments(bundle);
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, fragment).commit();
    }

    private boolean isNativeMapSupported() {
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS;
    }
}
