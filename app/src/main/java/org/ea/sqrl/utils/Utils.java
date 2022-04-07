package org.ea.sqrl.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.PopupMenu;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;

import com.google.zxing.FormatException;

import org.ea.sqrl.database.IdentityDBHelper;
import org.ea.sqrl.processors.SQRLStorage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

import static android.content.pm.PackageManager.GET_META_DATA;

/**
 *
 * @author Daniel Persson
 */
public class Utils {
    private static final String TAG = "EncryptionUtils";

    /**
     * Wrapper function if we ever need to read more than one byte segment in the future.
     *
     * @param data    String from the QR code read.
     * @return  The string without any extra information.
     */
    public static byte[] readSQRLQRCode(Intent data) throws FormatException {
        byte[] qrCode = new byte[0];
        for(int i=0;; i++) {
            byte[] newSegment = data.getByteArrayExtra("SCAN_RESULT_BYTE_SEGMENTS_" + i);
            if(newSegment == null) break;
            qrCode = EncryptionUtils.combine(qrCode, newSegment);
        }

        return qrCode;
    }

    public static String readSQRLQRCodeAsString(Intent data) {
        byte[] qrCode = null;
        try {
            qrCode = readSQRLQRCode(data);
        } catch (FormatException fe) { return null; }

        if (qrCode == null) return null;
        return new String(qrCode);
    }

    public static int getInteger(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    public static void setLanguage(Context context) {
        String lang = getLanguage(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Resources res = context.getResources();
            android.content.res.Configuration conf = res.getConfiguration();
            Locale locale = new Locale(lang);

            conf.setLocale(locale);
            res.updateConfiguration(conf, res.getDisplayMetrics());
        }
    }

    public static String getLanguage(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String lang = sharedPreferences.getString("language", "");
        return lang.isEmpty() ? Locale.getDefault().getLanguage() : lang;
    }

    public static void reloadActivityTitle(Activity activity) {
        try {
            int labelId = activity.getPackageManager().getActivityInfo(
                    activity.getComponentName(), GET_META_DATA).labelRes;
            if (labelId != 0) {
                activity.setTitle(labelId);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void refreshStorageFromDb(Activity activity) throws Exception {
        SharedPreferences sharedPref = activity.getApplication().getSharedPreferences(
                "org.ea.sqrl.preferences",
                Context.MODE_PRIVATE
        );
        long currentId = sharedPref.getLong("current_id", 0);
        IdentityDBHelper aDbHelper = IdentityDBHelper.getInstance(activity);
        byte[] identityData = aDbHelper.getIdentityData(currentId);
        SQRLStorage sqrlStorage = SQRLStorage.getInstance(activity);
        sqrlStorage.read(identityData);
    }

    public static byte[] getFileIntentContent(Context context, Uri contentUri) {
        if (contentUri == null) return null;

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(contentUri);

            while ((len = inputStream.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }

            inputStream.close();
            return os.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void enablePopupMenuIcons(PopupMenu popup) {
        try {
            Field[] fields = popup.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popup);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static byte[] readFullInputStreamBytes(InputStream inputStream) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;

        try {
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            return result.toByteArray();

        } catch (IOException ex) {
            return null;
        }
    }

    public static byte[] getAssetContent(Context context, String assetName) {
        AssetManager am = context.getAssets();

        try {
            InputStream is = am.open(assetName);
            return readFullInputStreamBytes(is);
        } catch (Exception e) {
            return null;
        }
    }

    public static DisplayMetrics getDisplayMetrics(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm;
    }

    public static void hideViewIfDisplayHeightSmallerThan(Activity activity, View view, int minHeight) {
        if (getDisplayMetrics(activity).heightPixels < minHeight) {
            view.setVisibility(View.GONE);
        }
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            int i = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static boolean isValidSqrlUri(Uri uri) {
        if (uri == null) return false;
        String scheme = uri.getScheme();
        if (scheme == null || !scheme.toLowerCase().equals("sqrl")) return false;

        return true;
    }

    public static Spannable getSpanWithHighlight(String s) {
        Spannable textSpan = new SpannableString(s.replaceAll("_", ""));
        int point = 0;
        int highlighted = 0;
        while(point >= 0) {
            int start = s.indexOf("_", point + 1);
            if(start == -1) break;
            int end = s.indexOf("_", start + 1);
            if(end == -1) break;
            textSpan.setSpan(new ForegroundColorSpan(Color.YELLOW), start - highlighted, end  - (highlighted + 1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            point = end;
            highlighted += 2;
        }
        return textSpan;
    }

    /**
     * Re-masks the password field within a TextInputLayout if the password is currently visible in plain text.
     * 
     * @param textInputLayout The TextInputLayout holding the password field to be re-masked.
     */
    public static void reMaskPassword(TextInputLayout textInputLayout) {
        EditText editText = (textInputLayout != null) ? textInputLayout.getEditText() : null;
        if (editText != null && !(editText.getTransformationMethod() instanceof PasswordTransformationMethod)) {
            textInputLayout.passwordVisibilityToggleRequested(true);
        }
    }
}
