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

package org.getlantern.firetweet.fragment.support;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import org.getlantern.firetweet.R;
import org.getlantern.firetweet.fragment.iface.IDialogFragmentCallback;

import me.uucky.colorpicker.ColorPickerDialog;

public final class ColorPickerDialogFragment extends BaseSupportDialogFragment implements
        DialogInterface.OnClickListener {

    @Override
    public void onCancel(final DialogInterface dialog) {
        super.onCancel(dialog);
        final FragmentActivity a = getActivity();
        if (a instanceof Callback) {
            ((Callback) a).onCancelled();
        }
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        final FragmentActivity a = getActivity();
        final Dialog d = getDialog();
        if (!(a instanceof Callback) || !(d instanceof ColorPickerDialog)) return;
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE: {
                final int color = ((ColorPickerDialog) d).getColor();
                ((Callback) a).onColorSelected(color);
                break;
            }
            case DialogInterface.BUTTON_NEUTRAL: {
                ((Callback) a).onColorCleared();
                break;
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final int color;
        final Bundle args = getArguments();
        if (savedInstanceState != null) {
            color = savedInstanceState.getInt(EXTRA_COLOR, Color.WHITE);
        } else {
            color = args.getInt(EXTRA_COLOR, Color.WHITE);
        }
        final boolean showAlphaSlider = args.getBoolean(EXTRA_ALPHA_SLIDER, true);
        final ColorPickerDialog d = new ColorPickerDialog(getActivity());
        final Resources res = getResources();
        for (int presetColor : PRESET_COLORS) {
            d.addColor(res.getColor(presetColor));
        }
        d.setInitialColor(color);
        d.setAlphaEnabled(showAlphaSlider);
        d.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.ok), this);
        if (args.getBoolean(EXTRA_CLEAR_BUTTON, false)) {
            d.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.clear), this);
        }
        d.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel), this);
        return d;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        final FragmentActivity a = getActivity();
        if (a instanceof Callback) {
            ((Callback) a).onDismissed();
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        final Dialog d = getDialog();
        if (d instanceof ColorPickerDialog) {
            outState.putInt(EXTRA_COLOR, ((ColorPickerDialog) d).getColor());
        }
        super.onSaveInstanceState(outState);
    }

    public static interface Callback extends IDialogFragmentCallback {

        public void onColorCleared();

        public void onColorSelected(int color);

    }

}
