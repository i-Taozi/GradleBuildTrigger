package joist.migrations.columns;

import java.util.ArrayList;
import java.util.List;

import joist.migrations.MigrationKeywords;
import joist.util.Interpolate;
import joist.util.Wrap;

public abstract class AbstractColumn<T extends AbstractColumn<T>> implements Column {

  private final String name;
  private final String dataType;
  private String tableName;
  private boolean nullable = false;
  private boolean unique = false;
  private String defaultValue;

  protected AbstractColumn(String name, String dataType) {
    this.name = name;
    this.dataType = dataType;
  }

  @SuppressWarnings("unchecked")
  public T nullable() {
    this.nullable = true;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T unique() {
    this.unique = true;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T defaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
    return (T) this;
  }

  public String getName() {
    return this.name;
  }

  public String getQuotedName() {
    return Wrap.quotes(this.name);
  }

  public String getTableName() {
    return this.tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String toSql() {
    return this.getQuotedName() + " " + this.getDataType() + (this.hasDefault() ? this.getDefaultExpression() : "");
  }

  /** @return any {@code null}/{@code not null}/{@code unique} constraints to apply to the column */
  public List<String> postInjectCommands() {
    List<String> sqls = new ArrayList<String>();
    if (!this.isNullable()) {
      this.addNotNull(sqls);
    }
    if (this.isUnique()) {
      String constraintName = this.getTableName() + "_" + this.getName() + "_key";
      sqls.add(Interpolate.string(
        "ALTER TABLE {} ADD CONSTRAINT {} UNIQUE ({});",
        Wrap.quotes(this.getTableName()),
        Wrap.quotes(constraintName),
        Wrap.quotes(this.getName())));
    }
    return sqls;
  }

  private void addNotNull(List<String> sqls) {
    if (MigrationKeywords.isMySQL()) {
      // mysql replaces all column metadata so this needs to include the default data as well
      String optionalDefault = this.hasDefault() ? " " + this.getDefaultExpression() : "";
      sqls.add(Interpolate.string(
        "ALTER TABLE {} MODIFY {} {} NOT NULL{};",
        Wrap.quotes(this.tableName),
        Wrap.quotes(this.name),
        this.getDataType(),
        optionalDefault));
    } else if (MigrationKeywords.isPg()) {
      sqls.add(Interpolate.string(//
        "ALTER TABLE {} ALTER COLUMN {} SET NOT NULL;",
        Wrap.quotes(this.tableName),
        Wrap.quotes(this.name)));
    } else {
      throw new IllegalStateException("Unhandled db " + MigrationKeywords.config.db);
    }
  }

  @Override
  public boolean isNullable() {
    return this.nullable;
  }

  protected boolean isUnique() {
    return this.unique;
  }

  @Override
  public boolean hasDefault() {
    return this.defaultValue != null;
  }

  protected String getDefaultExpression() {
    return " DEFAULT " + this.getDefaultValue();
  }

  protected String getDefaultValue() {
    return this.defaultValue;
  }

  public String getDataType() {
    return this.dataType;
  }

}
