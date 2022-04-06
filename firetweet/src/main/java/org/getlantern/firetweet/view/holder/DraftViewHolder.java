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
import android.widget.TextView;

import org.getlantern.firetweet.R;
import org.getlantern.firetweet.view.CardMediaContainer;
import org.getlantern.firetweet.view.iface.IColorLabelView;

public class DraftViewHolder extends ViewListHolder {

    public final IColorLabelView content;
    public final TextView text;
    public final TextView time;
    public CardMediaContainer media_preview_container;

    public DraftViewHolder(final View view) {
        super(view);
        content = (IColorLabelView) findViewById(R.id.content);
        text = (TextView) findViewById(R.id.text);
        time = (TextView) findViewById(R.id.time);
        media_preview_container = (CardMediaContainer) findViewById(R.id.media_preview_container);
    }

    public void setTextSize(final float textSize) {
        text.setTextSize(textSize);
        time.setTextSize(textSize * 0.75f);
    }

}
