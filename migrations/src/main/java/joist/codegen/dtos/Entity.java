package joist.codegen.dtos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import joist.codegen.Config;
import joist.util.Copy;
import joist.util.Inflector;
import joist.util.MapToList;

import org.apache.commons.lang.StringUtils;

/** Represents a domain object we will generate code for. */
public class Entity {

  protected Config config;
  private String tableName;
  private Entity baseEntity;
  private List<Entity> subEntities = new ArrayList<Entity>();
  private List<PrimitiveProperty> primitiveProperties = new ArrayList<PrimitiveProperty>();
  private List<ManyToOneProperty> manyToOneProperties = new ArrayList<ManyToOneProperty>();
  private List<OneToManyProperty> oneToManyProperties = new ArrayList<OneToManyProperty>();
  private List<ManyToManyProperty> manyToManyProperties = new ArrayList<ManyToManyProperty>();
  private List<String> uniquePropertyTypes;
  private List<String> uniqueCodeNames;

  public Entity(Config config, String tableName) {
    this.config = config;
    this.tableName = tableName;
  }

  public String getParentClassName() {
    if (this.isSubclass()) {
      return this.baseEntity.getClassName();
    } else {
      return this.config.getDomainObjectBaseClass();
    }
  }

  public boolean hasSubclasses() {
    return this.subEntities.size() > 0;
  }

  public Config getConfig() {
    return this.config;
  }

  public boolean isRoot() {
    return this.baseEntity == null && !this.isCodeEntity();
  }

  public boolean isAbstract() {
    return this.hasSubclasses() && !this.config.isNotAbstractEvenThoughSubclassed(this.tableName);
  }

  public boolean isConcrete() {
    return !this.isAbstract();
  }

  public boolean isSubclass() {
    return this.getBaseEntity() != null;
  }

  public boolean isCodeEntity() {
    return false;
  }

  public String getClassName() {
    return Inflector.camelize(this.getTableName());
  }

  /** @return {@code st} for a table {@code some_table} */
  public String getAliasAlias() {
    String a = "";
    for (String part : this.getTableName().split("_")) {
      a += part.substring(0, 1);
    }
    // add trailing 0 to avoid using keywords (like AdStat == as)
    return a + "0";
  }

  public String getAliasName() {
    return this.getClassName() + "Alias";
  }

  public String getVariableName() {
    return StringUtils.uncapitalize(this.getClassName());
  }

  public String getCodegenClassName() {
    return this.getClassName() + "Codegen";
  }

  public String getBuilderClassName() {
    return this.getClassName() + "Builder";
  }

  public String getBuilderCodegenClassName() {
    return this.getClassName() + "BuilderCodegen";
  }

  public String getQueriesCodegenClassName() {
    return this.getClassName() + "QueriesCodegen";
  }

  public String getFullCodegenClassName() {
    return this.config.getDomainObjectPackage() + "." + this.getCodegenClassName();
  }

  public String getFullAliasClassName() {
    return this.config.getDomainObjectPackage() + "." + this.getClassName() + "Alias";
  }

  public String getFullClassName() {
    return this.config.getDomainObjectPackage() + "." + this.getClassName();
  }

  public String getFullQueriesClassName() {
    return this.config.getQueriesPackage() + "." + this.getClassName() + "Queries";
  }

  public String getFullQueriesCodegenClassName() {
    return this.config.getQueriesPackage() + "." + this.getQueriesCodegenClassName();
  }

  public String getFullBuilderCodegenClassName() {
    return this.config.getBuildersPackage() + "." + this.getBuilderCodegenClassName();
  }

  public String getFullBuilderClassName() {
    return this.config.getBuildersPackage() + "." + this.getBuilderClassName();
  }

  public List<PrimitiveProperty> getPrimitiveProperties() {
    return this.primitiveProperties;
  }

  public List<ManyToOneProperty> getManyToOneProperties() {
    return this.manyToOneProperties;
  }

  public List<OneToManyProperty> getOneToManyProperties() {
    return this.oneToManyProperties;
  }

  public List<ManyToManyProperty> getManyToManyProperties() {
    return this.manyToManyProperties;
  }

  public String getTableName() {
    return this.tableName;
  }

  public Entity getBaseEntity() {
    return this.baseEntity;
  }

  public Entity getRootEntity() {
    Entity current = this;
    while (current.getBaseEntity() != null) {
      current = current.getBaseEntity();
    }
    return current;
  }

  public void setBaseEntity(Entity baseObject) {
    this.baseEntity = baseObject;
    // In Class Table Inheritance, the id column overlaps in each table, but we only want it in the root class
    for (Iterator<PrimitiveProperty> i = this.getPrimitiveProperties().iterator(); i.hasNext();) {
      if (i.next().getColumnName().equals("id")) {
        i.remove();
      }
    }
  }

  public void addSubEntity(Entity subEntity) {
    this.subEntities.add(subEntity);
  }

  public List<Entity> getSubEntities() {
    return this.subEntities;
  }

  public List<Entity> getSubEntitiesRecursively() {
    List<Entity> ret = new ArrayList<Entity>();
    List<Entity> walk = Copy.list(this.getSubEntities());
    while (walk.size() != 0) {
      Entity sub = walk.remove(0);
      ret.add(sub);
      walk.addAll(sub.getSubEntities());
    }
    return ret;
  }

  public List<Entity> getAllBaseAndSubEntities() {
    List<Entity> ret = new ArrayList<Entity>();
    ret.addAll(this.getBaseEntities());
    ret.add(this);
    ret.addAll(this.getSubEntitiesRecursively());
    return ret;
  }

  public List<String> getUniqueCodeNames() {
    if (this.uniqueCodeNames == null) {
      MapToList<String, String> perCodeName = new MapToList<String, String>();
      for (Entity entity : this.getAllBaseAndSubEntities()) {
        for (ManyToOneProperty mtop : entity.getManyToOneProperties()) {
          if (mtop.getOneSide().isCodeEntity()) {
            for (CodeValue code : ((CodeEntity) mtop.getOneSide()).getCodes()) {
              perCodeName.get(code.getEnumName()).add(mtop.getOneSide().getClassName());
            }
          }
        }
      }
      this.uniqueCodeNames = listOfKeysWithOnlyOneValue(perCodeName);
    }
    return this.uniqueCodeNames;
  }

  public List<String> getUniquePropertyTypes() {
    if (this.uniquePropertyTypes == null) {
      MapToList<String, String> perType = new MapToList<String, String>();
      for (Entity entity : this.getAllBaseAndSubEntities()) {
        for (PrimitiveProperty p : entity.getPrimitiveProperties()) {
          perType.get(p.getJavaType()).add(p.getVariableName());
        }
        for (ManyToOneProperty mtop : entity.getManyToOneProperties()) {
          perType.get(mtop.getJavaType()).add(mtop.getVariableName());
        }
      }
      this.uniquePropertyTypes = listOfKeysWithOnlyOneValue(perType);
    }
    return this.uniquePropertyTypes;
  }

  public List<Entity> getBaseEntities() {
    List<Entity> ret = new ArrayList<Entity>();
    Entity base = this.getBaseEntity();
    while (base != null) {
      ret.add(base);
      base = base.getBaseEntity();
    }
    return ret;
  }

  public boolean isStableTable() {
    return this.config.isStableTable(this.getTableName());
  }

  public String toString() {
    return this.getClassName();
  }

  private static List<String> listOfKeysWithOnlyOneValue(MapToList<String, String> m) {
    List<String> ret = new ArrayList<String>();
    for (Map.Entry<String, List<String>> e : m.entrySet()) {
      if (e.getValue().size() == 1) {
        ret.add(e.getKey());
      }
    }
    return ret;
  }

}
