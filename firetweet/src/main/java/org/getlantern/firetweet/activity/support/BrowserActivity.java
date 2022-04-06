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

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import org.getlantern.firetweet.R;
import org.getlantern.firetweet.fragment.BaseWebViewFragment;

public class BrowserActivity extends BaseActionBarActivity {

    private Uri mUri = Uri.parse("about:blank");

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case MENU_HOME:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mUri = getIntent().getData();
        if (mUri == null) {
            Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        final Fragment fragment = Fragment.instantiate(this, BaseWebViewFragment.class.getName());
        final Bundle bundle = new Bundle();
        bundle.putString(EXTRA_URI, mUri.toString());
        fragment.setArguments(bundle);
        ft.replace(android.R.id.content, fragment);
        ft.commit();
    }
}
