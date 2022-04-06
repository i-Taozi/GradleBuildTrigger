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

package org.getlantern.firetweet.util.content;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import org.apache.commons.lang3.ArrayUtils;
import org.getlantern.querybuilder.Columns;
import org.getlantern.querybuilder.Columns.Column;
import org.getlantern.querybuilder.Expression;
import org.getlantern.querybuilder.NewColumn;
import org.getlantern.querybuilder.OnConflict;
import org.getlantern.querybuilder.Tables;
import org.getlantern.querybuilder.query.SQLInsertQuery;
import org.getlantern.querybuilder.query.SQLSelectQuery;
import org.getlantern.firetweet.util.FiretweetArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.getlantern.querybuilder.SQLQueryBuilder.alterTable;
import static org.getlantern.querybuilder.SQLQueryBuilder.createTable;
import static org.getlantern.querybuilder.SQLQueryBuilder.dropTable;
import static org.getlantern.querybuilder.SQLQueryBuilder.insertInto;
import static org.getlantern.querybuilder.SQLQueryBuilder.select;

public final class DatabaseUpgradeHelper {
    public static void safeUpgrade(final SQLiteDatabase db, final String table, final String[] newColNames,
                                   final String[] newColTypes, final boolean dropDirectly, final boolean strictMode,
                                   final Map<String, String> colAliases) {
        safeUpgrade(db, table, newColNames, newColTypes, dropDirectly, strictMode, colAliases, OnConflict.REPLACE);
    }

    public static void safeUpgrade(final SQLiteDatabase db, final String table, final String[] newColNames,
                                   final String[] newColTypes, final boolean dropDirectly, final boolean strictMode,
                                   final Map<String, String> colAliases, final OnConflict onConflict) {

        if (newColNames == null || newColTypes == null || newColNames.length != newColTypes.length)
            throw new IllegalArgumentException("Invalid parameters for upgrading table " + table
                    + ", length of columns and types not match.");

        // First, create the table if not exists.
        final NewColumn[] newCols = NewColumn.createNewColumns(newColNames, newColTypes);
        final String createQuery = createTable(true, table).columns(newCols).buildSQL();
        db.execSQL(createQuery);

        // We need to get all data from old table.
        final String[] oldCols = getColumnNames(db, table);
        if (strictMode) {
            final String oldCreate = getCreateSQL(db, table);
            final Map<String, String> map = getTypeMapByCreateQuery(oldCreate);
            boolean differenct = false;
            for (final NewColumn newCol : newCols) {
                if (!newCol.getType().equalsIgnoreCase(map.get(newCol.getName()))) {
                    differenct = true;
                }
            }
            if (!differenct) return;
        } else if (oldCols == null || FiretweetArrayUtils.contentMatch(newColNames, oldCols)) return;
        if (dropDirectly) {
            db.beginTransaction();
            db.execSQL(dropTable(true, table).getSQL());
            db.execSQL(createQuery);
            db.setTransactionSuccessful();
            db.endTransaction();
            return;
        }
        final String tempTable = String.format(Locale.US, "temp_%s_%d", table, System.currentTimeMillis());
        db.beginTransaction();
        db.execSQL(alterTable(table).renameTo(tempTable).buildSQL());
        db.execSQL(createQuery);
        final String[] notNullCols = getNotNullColumns(newCols);
        final String insertQuery = createInsertDataQuery(table, tempTable, newColNames, oldCols, colAliases,
                notNullCols, onConflict);
        if (insertQuery != null) {
            db.execSQL(insertQuery);
        }
        db.execSQL(dropTable(true, tempTable).getSQL());
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public static void safeUpgrade(final SQLiteDatabase db, final String table, final String[] newColNames,
                                   final String[] newColTypes, final boolean dropDirectly, final Map<String, String> colAliases) {
        safeUpgrade(db, table, newColNames, newColTypes, dropDirectly, true, colAliases, OnConflict.REPLACE);
    }

    private static String createInsertDataQuery(final String table, final String tempTable, final String[] newCols,
                                                final String[] oldCols, final Map<String, String> colAliases, final String[] notNullCols,
                                                final OnConflict onConflict) {
        final SQLInsertQuery.Builder qb = insertInto(onConflict, table);
        final List<String> newInsertColsList = new ArrayList<>();
        for (final String newCol : newCols) {
            final String oldAliasedCol = colAliases != null ? colAliases.get(newCol) : null;
            if (ArrayUtils.contains(oldCols, newCol) || oldAliasedCol != null
                    && ArrayUtils.contains(oldCols, oldAliasedCol)) {
                newInsertColsList.add(newCol);
            }
        }
        final String[] newInsertCols = newInsertColsList.toArray(new String[newInsertColsList.size()]);
        if (!FiretweetArrayUtils.contains(newInsertCols, notNullCols)) return null;
        qb.columns(newInsertCols);
        final Columns.Column[] oldDataCols = new Columns.Column[newInsertCols.length];
        for (int i = 0, j = oldDataCols.length; i < j; i++) {
            final String newCol = newInsertCols[i];
            final String oldAliasedCol = colAliases != null ? colAliases.get(newCol) : null;
            if (oldAliasedCol != null && ArrayUtils.contains(oldCols, oldAliasedCol)) {
                oldDataCols[i] = new Columns.Column(oldAliasedCol, newCol);
            } else {
                oldDataCols[i] = new Columns.Column(newCol);
            }
        }
        final SQLSelectQuery.Builder selectOldBuilder = select(new Columns(oldDataCols));
        selectOldBuilder.from(new Tables(tempTable));
        qb.select(selectOldBuilder.build());
        return qb.buildSQL();
    }

    private static String[] getColumnNames(final SQLiteDatabase db, final String table) {
        final Cursor cur = db.query(table, null, null, null, null, null, null, "1");
        if (cur == null) return null;
        try {
            return cur.getColumnNames();
        } finally {
            cur.close();
        }
    }

    private static String getCreateSQL(final SQLiteDatabase db, final String table) {
        final SQLSelectQuery.Builder qb = select(new Column("sql"));
        qb.from(new Tables("sqlite_master"));
        qb.where(new Expression("type = ? AND name = ?"));
        final Cursor c = db.rawQuery(qb.buildSQL(), new String[]{"table", table});
        if (c == null) return null;
        try {
            if (c.moveToFirst()) return c.getString(0);
            return null;
        } finally {
            c.close();
        }
    }

    private static String[] getNotNullColumns(final NewColumn[] newCols) {
        if (newCols == null) return null;
        final String[] notNullCols = new String[newCols.length];
        int count = 0;
        for (final NewColumn column : newCols) {
            if (column.getType().endsWith(" NOT NULL")) {
                notNullCols[count++] = column.getName();
            }
        }
        return FiretweetArrayUtils.subArray(notNullCols, 0, count);
    }

    private static Map<String, String> getTypeMapByCreateQuery(final String query) {
        if (TextUtils.isEmpty(query)) return Collections.emptyMap();
        final int start = query.indexOf("("), end = query.lastIndexOf(")");
        if (start < 0 || end < 0) return Collections.emptyMap();
        final HashMap<String, String> map = new HashMap<>();
        for (final String segment : query.substring(start + 1, end).split(",")) {
            final String trimmed = segment.trim().replaceAll(" +", " ");
            final int idx = trimmed.indexOf(" ");
            map.put(trimmed.substring(0, idx), trimmed.substring(idx + 1, trimmed.length()));
        }
        return map;
    }

}
