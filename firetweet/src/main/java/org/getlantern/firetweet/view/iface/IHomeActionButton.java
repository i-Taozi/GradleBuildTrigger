package org.getlantern.firetweet.view.iface;

import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

/**
 * Created by mariotaku on 14/10/23.
 */
public interface IHomeActionButton {
    void setButtonColor(int color);

    void setIcon(Bitmap bm);

    void setIcon(Drawable drawable);

    void setIcon(int resId);

    void setIconColor(int color, PorterDuff.Mode mode);

    void setShowProgress(boolean showProgress);

    void setTitle(CharSequence title);

    void setTitle(int title);
}
