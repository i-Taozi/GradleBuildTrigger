package joist.migrations.columns;

public class MoneyColumn extends AbstractColumn<MoneyColumn> {

  public MoneyColumn(String name) {
    super(name, "int");
  }

}
