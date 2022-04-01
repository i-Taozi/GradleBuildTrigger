package features.migrations;

import static joist.migrations.MigrationKeywords.createTable;
import static joist.migrations.MigrationKeywords.foreignKey;
import static joist.migrations.MigrationKeywords.integer;
import static joist.migrations.MigrationKeywords.primaryKey;
import static joist.migrations.MigrationKeywords.varchar;
import joist.migrations.AbstractMigration;

public class m0005 extends AbstractMigration {

  public m0005() {
    super("Parent with multiple children.");
  }

  public void apply() {
    createTable("parent_b_parent", primaryKey("id"), varchar("name").unique(), integer("version"));
    createTable("parent_b_child_foo", primaryKey("id"), foreignKey("parent_b_parent"), varchar("name"), integer("version"));
    createTable("parent_b_child_bar", primaryKey("id"), foreignKey("parent_b_parent"), varchar("name"), integer("version"));
    // add another child that has both the parent and a sibling required; this will potentially confuse
    // the builder because it will see the parent required twice (for us and our sibling) and we really
    // want them to be the same parent
    createTable(
      "parent_b_child_zaz",
      primaryKey("id"),
      foreignKey("parent_b_parent"),
      foreignKey("parent_b_child_bar"),
      varchar("name"),
      integer("version"));
  }

}
