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

package org.getlantern.firetweet.util;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import org.getlantern.firetweet.Constants;
import org.getlantern.firetweet.R;
import org.getlantern.firetweet.fragment.ProgressDialogFragment;

import java.io.File;
import java.io.IOException;

import static android.text.TextUtils.isEmpty;
import static org.getlantern.firetweet.util.Utils.getImageMimeType;

public class SaveImageTask extends AsyncTask<Object, Object, File> implements Constants {

    private static final String PROGRESS_FRAGMENT_TAG = "progress";

    private final File src;
    private final Activity activity;

    public SaveImageTask(final Activity activity, final File src) {
        this.activity = activity;
        this.src = src;
    }

    @Override
    protected File doInBackground(final Object... args) {
        if (src == null) return null;
        return saveImage(activity, src);
    }

    @Override
    protected void onCancelled() {
        final FragmentManager fm = activity.getFragmentManager();
        final DialogFragment fragment = (DialogFragment) fm.findFragmentByTag(PROGRESS_FRAGMENT_TAG);
        if (fragment != null && fragment.isVisible()) {
            fragment.dismiss();
        }
        super.onCancelled();
    }

    @Override
    protected void onPostExecute(final File result) {
        final FragmentManager fm = activity.getFragmentManager();
        final DialogFragment fragment = (DialogFragment) fm.findFragmentByTag(PROGRESS_FRAGMENT_TAG);
        if (fragment != null) {
            fragment.dismiss();
        }
        super.onPostExecute(result);
        if (result != null && result.exists()) {
            Toast.makeText(activity, R.string.saved_to_gallery, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, R.string.error_occurred, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPreExecute() {
        final DialogFragment fragment = new ProgressDialogFragment();
        fragment.setCancelable(false);
        fragment.show(activity.getFragmentManager(), PROGRESS_FRAGMENT_TAG);
        super.onPreExecute();
    }

    public static File saveImage(final Context context, final File image_file) {
        if (context == null && image_file == null) return null;
        try {
            final String name = image_file.getName();
            if (isEmpty(name)) return null;
            final String mimeType = getImageMimeType(image_file);
            final MimeTypeMap map = MimeTypeMap.getSingleton();
            final String extension = map.getExtensionFromMimeType(mimeType);
            if (extension == null) return null;
            final String nameToSave = name.contains(".") ? name : name + "." + extension;
            final File pubDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            final File saveDir = new File(pubDir, "Firetweet");
            if (!saveDir.isDirectory() && !saveDir.mkdirs()) return null;
            final File saveFile = new File(saveDir, nameToSave);
            FileUtils.copyFile(image_file, saveFile);
            if (mimeType != null) {
                MediaScannerConnection.scanFile(context, new String[]{saveFile.getPath()},
                        new String[]{mimeType}, null);
            }
            return saveFile;
        } catch (final IOException e) {
            Log.w(LOGTAG, "Failed to save file", e);
            return null;
        }
    }

}
