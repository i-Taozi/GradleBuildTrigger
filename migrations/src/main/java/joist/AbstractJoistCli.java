package joist;

import joist.codegen.Codegen;
import joist.codegen.Config;
import joist.codegen.Schema;
import joist.codegen.passes.GenerateFlushFunction;
import joist.codegen.passes.MySqlHistoryTriggersPass;
import joist.domain.orm.Db;
import joist.migrations.DatabaseBootstrapper;
import joist.migrations.Migrater;
import joist.migrations.PermissionFixer;

public abstract class AbstractJoistCli {

  public Config config;
  private Schema schema;

  public AbstractJoistCli(String projectName, Db db) {
    this(new Config(projectName, db));
  }

  public AbstractJoistCli(String projectName, String defaultDatabaseName, Db db) {
    this(new Config(projectName, defaultDatabaseName, db));
  }

  public AbstractJoistCli(Config config) {
    this.config = config;
    if (".".equals(this.config.dbAppSaSettings.password)) {
      throw new RuntimeException("You need to set db.sa.password either on the command line or in build.properties.");
    }
  }

  public void cycle() {
    this.createDatabase();
    this.migrateDatabase();
    this.fixPermissions();
    this.codegen();
    if (this.config.useHistoryTriggers) {
      this.historyTriggers();
    }
  }

  public void createBackup() {
    // first make a clean database
    this.createDatabase();
    this.migrateDatabase();
    this.fixPermissions();
    // now run the backup
    new DatabaseBootstrapper(this.config).backup();
  }

  public void createDatabase() {
    new DatabaseBootstrapper(this.config).dropAndCreate();
  }

  public void migrateDatabase() {
    new Migrater(this.config).migrate();
    if (this.config.useHistoryTriggers) {
      this.historyTriggers();
    }
  }

  public void fixPermissions() {
    PermissionFixer pf = new PermissionFixer(this.config);
    pf.grantAllOnAllTablesTo(this.config.dbAppUserSettings.user);
    if (this.config.db.isPg()) {
      // mysql doesn't have the concept of ownership (...afaik)
      pf.setOwnerOfAllTablesTo(this.config.dbAppSaSettings.user);
      pf.setOwnerOfAllSequencesTo(this.config.dbAppSaSettings.user);
      pf.grantAllOnAllSequencesTo(this.config.dbAppUserSettings.user);
    }
    pf.flushPermissionIfNeeded();
  }

  public void codegen() {
    new Codegen(this.config, this.getSchema()).generate();
  }

  public void generateFlushFunction() {
    Codegen codegen = new Codegen(this.config, this.getSchema());
    new GenerateFlushFunction().pass(codegen);
  }

  public void historyTriggers() {
    if (this.config.db.isMySQL()) {
      new MySqlHistoryTriggersPass().pass(this.getSchema());
    } else {
      throw new IllegalStateException("Currently history triggers are only supported in MySQL");
    }
  }

  protected Schema getSchema() {
    if (this.schema == null) {
      this.schema = new Schema(this.config);
      this.schema.populate();
    }
    return this.schema;
  }

}
