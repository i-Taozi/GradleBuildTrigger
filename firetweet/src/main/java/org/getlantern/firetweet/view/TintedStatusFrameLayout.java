/*
 * Firetweet - Twitter client for Android
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

package org.getlantern.firetweet.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import org.getlantern.firetweet.util.MathUtils;
import org.getlantern.firetweet.util.Utils;

/**
 * Created by mariotaku on 14/11/26.
 */
public class TintedStatusFrameLayout extends ExtendedFrameLayout {

    private final Paint mBlackPaint, mShadowPaint, mColorPaint;

    private int mStatusBarHeight;
    private float mFactor;
    private int mColorAlpha, mShadowAlpha;
    private boolean mDrawShadow, mDrawColor;

    public TintedStatusFrameLayout(Context context) {
        this(context, null);
    }

    public TintedStatusFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintedStatusFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBlackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBlackPaint.setColor(Color.BLACK);
        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setWillNotDraw(false);
    }


    public void setColor(int color) {
        setColor(color, Color.alpha(color));
    }

    public void setColor(int color, int alpha) {
        mColorPaint.setColor(color);
        mColorAlpha = alpha;
        updateAlpha();
    }

    public void setDrawColor(boolean color) {
        mDrawColor = color;
        invalidate();
    }

    public void setDrawShadow(boolean shadow) {
        mDrawShadow = shadow;
        invalidate();
    }

    public void setFactor(float f) {
        mFactor = f;
        updateAlpha();
    }

    public void setShadowColor(int color) {
        mShadowPaint.setColor(color);
        mShadowAlpha = Color.alpha(color);
        updateAlpha();
    }

    public void setStatusBarHeight(int height) {
        mStatusBarHeight = height;
        invalidate();
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mDrawShadow) {
            canvas.drawRect(0, 0, canvas.getWidth(), mStatusBarHeight, mShadowPaint);
        } else if (mDrawColor) {
            canvas.drawRect(0, 0, canvas.getWidth(), mStatusBarHeight, mBlackPaint);
        }
        canvas.drawRect(0, 0, canvas.getWidth(), mStatusBarHeight, mDrawColor ? mColorPaint : mBlackPaint);
    }

    @Override
    protected boolean fitSystemWindows(@NonNull Rect insets) {
        setStatusBarHeight(Utils.getInsetsTopWithoutActionBarHeight(getContext(), insets.top));
        return super.fitSystemWindows(insets);
    }

    private void updateAlpha() {
        final float f = mFactor;
        mShadowPaint.setAlpha(Math.round(mShadowAlpha * MathUtils.clamp(1 - f, 0, 1)));
        mColorPaint.setAlpha(Math.round(0xFF * MathUtils.clamp(f, 0, 1)));
        invalidate();
    }
}
