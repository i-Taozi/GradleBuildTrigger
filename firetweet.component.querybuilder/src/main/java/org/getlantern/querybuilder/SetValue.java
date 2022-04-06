package org.getlantern.querybuilder;

import java.util.Locale;

/**
 * Created by mariotaku on 14-8-7.
 */
public class SetValue implements SQLLang {

    private final Columns.Column column;
    private final SQLLang expression;

    public SetValue(Columns.Column column, SQLLang expression) {
        this.column = column;
        this.expression = expression;
    }


    @Override
    public String getSQL() {
        return String.format(Locale.ROOT, "%s = %s", column.getSQL(), expression.getSQL());
    }
}
