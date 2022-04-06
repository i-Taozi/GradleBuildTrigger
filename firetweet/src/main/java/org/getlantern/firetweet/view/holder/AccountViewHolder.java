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

package org.getlantern.firetweet.view.holder;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import org.getlantern.firetweet.R;
import org.getlantern.firetweet.view.ColorLabelRelativeLayout;

public class AccountViewHolder {

    public final ImageView profile_image;
    public final TextView name, screen_name;
    public final CheckBox checkbox;
    private final ColorLabelRelativeLayout content;
    private final View drag_handle;

    public AccountViewHolder(final View view) {
        content = (ColorLabelRelativeLayout) view;
        name = (TextView) view.findViewById(android.R.id.text1);
        screen_name = (TextView) view.findViewById(android.R.id.text2);
        profile_image = (ImageView) view.findViewById(android.R.id.icon);
        checkbox = (CheckBox) view.findViewById(android.R.id.checkbox);
        drag_handle = view.findViewById(R.id.drag_handle);
    }

    public void setAccountColor(final int color) {
        content.drawEnd(color);
    }

    public void setSortEnabled(boolean enabled) {
        drag_handle.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }
}
