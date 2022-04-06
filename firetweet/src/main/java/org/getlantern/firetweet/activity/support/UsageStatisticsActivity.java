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

package org.getlantern.firetweet.activity.support;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import org.getlantern.firetweet.Constants;
import org.getlantern.firetweet.R;
import org.getlantern.firetweet.activity.SettingsActivity;
import org.getlantern.firetweet.fragment.SettingsDetailsFragment;

public class UsageStatisticsActivity extends BaseActionBarActivity implements Constants {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle fragmentArgs = new Bundle();
        fragmentArgs.putInt(EXTRA_RESID, R.xml.preferences_usage_statistics);
        final Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsDetailsFragment.class.getName());
        intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS, fragmentArgs);
        intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_TITLE, R.string.usage_statistics);
        startActivity(intent);
        finish();
    }

}
