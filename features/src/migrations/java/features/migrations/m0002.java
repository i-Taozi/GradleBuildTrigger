package features.migrations;

import static joist.migrations.MigrationKeywords.addColumn;
import static joist.migrations.MigrationKeywords.createTable;
import static joist.migrations.MigrationKeywords.foreignKey;
import static joist.migrations.MigrationKeywords.integer;
import static joist.migrations.MigrationKeywords.primaryKey;
import static joist.migrations.MigrationKeywords.varchar;
import joist.migrations.AbstractMigration;

public class m0002 extends AbstractMigration {

  public m0002() {
    super("Parent/child.");
  }

  public void apply() {
    createTable("parent", primaryKey("id"), varchar("name"), integer("version"));
    createTable("child", primaryKey("id"), foreignKey("parent").ownerIsThem(), varchar("name"), integer("version"));
    createTable("grand_child", primaryKey("id"), foreignKey("child"), varchar("name"), integer("version"));

    // for property skipped test, add fk primitives -> parent
    addColumn("primitives", foreignKey("parent").ownerIsNeither().nullable());
  }

}
