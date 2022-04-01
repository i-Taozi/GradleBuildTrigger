package joist.codegen.passes;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import joist.codegen.Codegen;
import joist.codegen.dtos.Entity;
import joist.jdbc.Jdbc;
import joist.util.Interpolate;
import joist.util.StringBuilderr;
import joist.util.Wrap;

public class GenerateFlushFunction implements Pass<Codegen> {

  private static final Logger log = LoggerFactory.getLogger(GenerateFlushFunction.class);
  private DataSource ds;

  public void pass(Codegen codegen) {
    log.info("Creating flush_test_database stored procedure");
    this.ds = codegen.getConfig().dbAppSaSettings.getDataSource();
    if (codegen.getConfig().db.isPg()) {
      this.generatePg(codegen);
    } else if (codegen.getConfig().db.isMySQL()) {
      this.generateMySQL(codegen);
    } else {
      throw new IllegalStateException("Unhandled db " + codegen.getConfig().db);
    }
  }

  private void generatePg(Codegen codegen) {
    StringBuilderr sql = new StringBuilderr();
    sql.line("CREATE OR REPLACE FUNCTION flush_test_database() RETURNS integer AS");
    sql.line("$BODY$");
    sql.line("BEGIN");
    sql.line("SET CONSTRAINTS ALL DEFERRED;");
    for (Entity entity : codegen.getSchema().getEntities().values()) {
      if (entity.isRoot() && !entity.isStableTable()) {
        sql.line("ALTER SEQUENCE {} RESTART WITH 1 INCREMENT BY 1;", Wrap.quotes(entity.getTableName() + "_id_seq"));
      }
    }
    for (Entity entity : codegen.getSchema().getEntities().values()) {
      if (entity.isRoot() && !entity.isStableTable()) {
        sql.line("DELETE FROM {};", Wrap.quotes(entity.getTableName()));
      }
    }
    sql.line("RETURN 0;");
    sql.line("END;");
    sql.line("$BODY$");
    sql.line("  LANGUAGE 'plpgsql' VOLATILE");
    sql.line("  COST 100;");
    sql.line("ALTER FUNCTION flush_test_database() SECURITY DEFINER;");
    Jdbc.update(this.ds, sql.toString());
  }

  private void generateMySQL(Codegen codegen) {
    StringBuilderr sql = new StringBuilderr();
    sql.line("CREATE PROCEDURE flush_test_database()");
    sql.line("BEGIN");
    sql.line("SET FOREIGN_KEY_CHECKS=0;");
    for (Entity entity : codegen.getSchema().getEntities().values()) {
      // mysql 5.6's TRUNCATE causes significant slow downs, so only conditionally
      // issue the truncate. This changed a single method in feature test from
      // take 3+ seconds to 0.1 seconds. See: https://bugs.mysql.com/bug.php?id=68184
      if (!entity.isCodeEntity() && !entity.isStableTable() && entity.isRoot()) {
        sql.line(
          "SET @c = (SELECT `AUTO_INCREMENT` FROM information_schema.tables WHERE table_schema = '{}' AND table_name = '{}');",
          codegen.getConfig().dbAppUserSettings.schemaName,
          entity.getTableName());
        sql.line("IF @c > 1 THEN ");
        for (Entity e : entity.getAllBaseAndSubEntities()) {
          sql.line("TRUNCATE TABLE {};", e.getTableName());
        }
        sql.line("END IF;");
      }
    }
    sql.line("SET FOREIGN_KEY_CHECKS=1;");
    sql.line("SELECT 1;");
    sql.line("END");
    Jdbc.update(this.ds, "DROP PROCEDURE IF EXISTS flush_test_database;");
    Jdbc.update(this.ds, sql.toString());
    Jdbc.update(this.ds, Interpolate.string("GRANT ALL ON PROCEDURE flush_test_database TO {}@'%'", codegen.getConfig().dbAppUserSettings.user));
  }

}
