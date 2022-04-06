package org.getlantern.firetweet.extension.streaming.util;

import android.content.Context;
import android.database.Cursor;

import org.getlantern.firetweet.FiretweetConstants;
import org.getlantern.firetweet.FiretweetSharedPreferences;
import org.getlantern.firetweet.provider.FiretweetDataStore.Accounts;

import java.io.Closeable;
import java.io.IOException;

import static android.text.TextUtils.isEmpty;

public class Utils implements FiretweetConstants {


    public static void closeSilently(Closeable closeable) {
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (IOException ignore) {

        }
    }

    public static long[] getActivatedAccountIds(final Context context) {
        long[] accounts = new long[0];
        if (context == null) return accounts;
        final String[] cols = new String[]{Accounts.ACCOUNT_ID};
        final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, cols, Accounts.IS_ACTIVATED + "=1",
                null, Accounts.ACCOUNT_ID);
        if (cur != null) {
            final int idx = cur.getColumnIndexOrThrow(Accounts.ACCOUNT_ID);
            cur.moveToFirst();
            accounts = new long[cur.getCount()];
            int i = 0;
            while (!cur.isAfterLast()) {
                accounts[i] = cur.getLong(idx);
                i++;
                cur.moveToNext();
            }
            cur.close();
        }
        return accounts;
    }

    public static String getNonEmptyString(final FiretweetSharedPreferences pref, final String key, final String def) {
        if (pref == null) return def;
        final String val = pref.getString(key, def);
        return isEmpty(val) ? def : val;
    }

    public static String replaceLast(final String text, final String regex, final String replacement) {
        if (text == null || regex == null || replacement == null) return text;
        return text.replaceFirst("(?s)" + regex + "(?!.*?" + regex + ")", replacement);
    }

}
