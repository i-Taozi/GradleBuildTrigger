/*
 * Copyright (c) 1998-2015 Caucho Technology -- all rights reserved
 *
 * This file is part of Baratine(TM)
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Baratine is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Baratine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Baratine; if not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Alex Rojkov
 */

package com.caucho.v5.ramp.vault;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.logging.Logger;

import io.baratine.db.Cursor;
import io.baratine.service.Service;
import io.baratine.service.ServiceException;
import io.baratine.vault.Asset;
import io.baratine.vault.IdAsset;

import com.caucho.v5.amp.vault.StateAsset;
import com.caucho.v5.convert.bean.FieldBase;
import com.caucho.v5.convert.bean.FieldNull;
import com.caucho.v5.convert.bean.FieldObject;
import com.caucho.v5.inject.AnnotationLiteral;
import com.caucho.v5.inject.type.TypeRef;
import com.caucho.v5.kraken.info.TableInfo;
import com.caucho.v5.util.IdentityGenerator;
import com.caucho.v5.util.L10N;
import com.caucho.v5.util.RandomUtil;

class AssetInfo<ID,T>
{
  private static final Logger log
    = Logger.getLogger(AssetInfo.class.getName());

  private static final L10N L = new L10N(AssetInfo.class);
  
  private static final 
  HashMap<Class<?>,Function<AssetInfo<?,?>,IdGenerator<?>>> _idGenMap;

  private static final Map<Class,Class> _primitiveWrappers = new HashMap<>();

  private final String _tableName;
  
  private final FieldInfo<T,?>[] _fields;
  private final FieldBase<T> _fieldState;
  
  private Class<T> _type;
  
  private boolean _isDocument;

  private Class<ID> _idType;
  private IdGenerator<ID> _idGen;
  
  private AtomicLong _idSequence = new AtomicLong();

  private String _addressPrefix;

  private int _saveColumns;

  private boolean _isSolo;

  public AssetInfo(Class<T> type,
                   Class<ID> idType,
                   Asset table,
                   Service serviceTable)
  {
    Objects.requireNonNull(type);
    Objects.requireNonNull(idType);

    _type = type;
    _idType = idType;
    
    _addressPrefix = "/" + type.getSimpleName();

    _tableName = tableName(type, table, serviceTable);
    
    _fields = introspect();
    _fieldState = introspectLoadState();
  }

  private String tableName(Class<T> type, Asset table, Service serviceTable)
  {
    String tableName;

    if (serviceTable != null && ! serviceTable.value().isEmpty()) {
      tableName = serviceTable.value();
      tableName = tableName.substring(1);
      tableName = tableName.replace('/', '_');
    }
    else if (table != null && !table.value().isEmpty()) {
      tableName = table.value();
    }
    else {
      tableName = type.getSimpleName();
    }

    return tableName;
  }

  private FieldInfo<T,?> []introspect()
  {
    List<FieldInfo<T,?>> fields = new ArrayList<>();
    
    if (Void.class.equals(_idType)) {
      FieldIdSolo<T> fieldId = new FieldIdSolo<>();
      
      _isSolo = true;
      
      fields.add(fieldId);
    }

    Class<?> type = _type;
    do {
      for (Field field : type.getDeclaredFields()) {
        if (! isPersistent(field)) {
          continue;
        }

        ColumnVault column = field.getAnnotation(ColumnVault.class);

        if (column == null) {
          column = makeDefaultColumn(field);
        }

        FieldInfo<T,?> fieldInfo;

        if (IdAsset.class.equals(field.getType())) {
          fieldInfo = new FieldInfoIdAsset<>(field, column);
        }
        else {
          fieldInfo = new FieldAsset<>(field, column);
        }

        if (! fieldInfo.isColumn()) {
          _isDocument = true;
        }

        fields.add(fieldInfo);
      }
    } while (! Object.class.equals(type = type.getSuperclass()));

    List<FieldInfo<T,?>> ids = new ArrayList<>();

    for (FieldInfo<T,?> field : fields) {
      if (field.isId()) {
        ids.add(field);
      }
    }
    
    if (ids.size() == 0) {
      throw new IllegalStateException(L.l("{0} must define a primary key",
                                          _type));
    }
    else if (ids.size() > 1) {
      throw new IllegalStateException(L.l("too many fields declare primary key {0}",
                                          ids.toString()));
    }

    if (! isIdTypeMatch(_idType, ids.get(0).getJavaType())) {
      throw new IllegalStateException(L.l(
        "repository defined <ID> '{0}' does not match actual @Id `{1}` on entity '{2}'",
        _idType.getName(),
        ids.get(0).getJavaType(),
        _type.getName()));
    }

    ColumnVault objColumn = _type.getAnnotation(ColumnVault.class);

    if (objColumn != null) {
      fields.add(new FieldInfoObject(_type, objColumn));
    }

    FieldInfo<T,?>[] fieldsArray = fields.toArray(new FieldInfo[fields.size()]);
    
    Function<AssetInfo<?,?>,IdGenerator<ID>> idGenFun
      = (Function) _idGenMap.get(_idType);
    
    _idSequence.set(RandomUtil.getRandomLong());
    IdGenerator<ID> idGen = null;
    
    if (idGenFun != null) {
      idGen = idGenFun.apply(this);
    }
    else {
      idGen = new IdGenerator<>(this);
    }
    
    Objects.requireNonNull(idGen);
    
    _idGen = idGen;
    
    return fieldsArray;
  }
  
  private FieldBase<T> introspectLoadState()
  {
    for (Field field : _type.getDeclaredFields()) {
      if (field.getType().equals(StateAsset.class)) {
        return new FieldObject<>(field);
      }
    }
    
    return new FieldNull<>();
  }

  private boolean isIdTypeMatch(Class<?> repIdType, Class<?> idType)
  {
    if (repIdType.equals(idType)) {
      return true;
    }

    if (idType.isPrimitive()
        && _primitiveWrappers.get(idType).equals(repIdType)) {
      return true;
    }

    return false;
  }
  
  public Class<T> type()
  {
    return _type;
  }

  public Class<ID> idType()
  {
    return _idType;
  }

  public boolean isSolo()
  {
    return _isSolo;
  }
  
  public boolean isDocument()
  {
    return _isDocument;
  }

  private boolean isPersistent(Field field)
  {
    int mod = field.getModifiers();

    if (Modifier.isStatic(mod))
      return false;
    else if (Modifier.isTransient(mod))
      return false;

    return true;
  }

  public FieldInfo<T,ID> id()
  {
    for (FieldInfo<T,?> field : _fields) {
      if (field.isId()) {
        return (FieldInfo<T,ID>) field;
      }
    }

    return null;
  }

  public ID id(T bean)
  {
    return (ID) id().getValue(bean);
  }
  
  private String prefix()
  {
    return _addressPrefix;
  }

  private int node()
  {
    return 0;
  }

  private int nodeCount()
  {
    return 1;
  }
  
  private long nextSequence()
  {
    return _idSequence.incrementAndGet();
  }

  public void nextId(T bean)
  {
    ID id = id(bean);
    
    ID idNew = _idGen.generate(id);
    
    if (id != idNew) {
      id().setValue(bean, idNew);
    }
  }
  
  public ID nextId()
  {
    return _idGen.generate(null);
  }

  public String generateAddress(ID id)
  {
    return _idGen.generateAddress(id);
  }

  public FieldInfo<T,?>[] getFields()
  {
    return _fields;
  }

  public FieldInfo<?,?> field(String fieldName)
  {
    for (FieldInfo<?,?> field : _fields) {
      if (field.columnName().equalsIgnoreCase(fieldName)) {
        return field;
      }
    }
    
    return null;
  }

  public String tableName()
  {
    return _tableName;
  }

  public int getSize()
  {
    return _fields.length;
  }

  String saveSql()
  {
    StringBuilder head = new StringBuilder("insert into ")
      .append(tableName())
      .append(" (");

    StringBuilder tail = new StringBuilder(") values (");

    FieldInfo<T,?>[] fields = getFields();
    
    boolean isDocument = false;
    int saveColumns = 0;

    for (int i = 0; i < fields.length; i++) {
      FieldInfo<T,?> field = getFields()[i];
      
      if (! field.isColumn()) {
        isDocument = true;
        continue;
      }

      saveColumns++;
      head.append(field.columnName());

      tail.append('?');
      if ((i + 1) < fields.length) {
        head.append(", ");
        tail.append(", ");
      }
    }
    
    if (isDocument) {
      saveColumns++;
      head.append("__doc");
      tail.append("?");
    }

    tail.append(')');
    head.append(tail);
    
    _saveColumns = saveColumns;

    return head.toString();
  }

  public String loadSql()
  {
    StringBuilder head = new StringBuilder("select ");
    StringBuilder where = new StringBuilder(" where ");

    FieldInfo<T,?>[] fields = getFields();
    boolean isFirst = true;

    for (int i = 0; i < fields.length; i++) {
      FieldInfo<T,?> field = fields[i];

      if (field.isId()) {
        where.append(field.columnName()).append(" = ?");
        continue;
      }
      
      if (! field.isColumn()) {
        continue;
      }

      if (isFirst) {
        isFirst = false;
      }
      else {
        head.append(", ");
      }
      
      head.append(field.columnName());
    }
    
    if (isDocument()) {
      if (! isFirst) {
        head.append(", ");
      }
      
      head.append("__doc");
    }

    head.append(" from ").append(tableName());

    head.append(where);

    return head.toString();
  }

  public String deleteSql()
  {
    StringBuilder sql = new StringBuilder();
    
    sql.append("delete from ").append(tableName());
    
    sql.append(" where ");

    FieldInfo<T,?>[] fields = getFields();
    boolean isFirst = true;

    for (int i = 0; i < fields.length; i++) {
      FieldInfo<T,?> field = fields[i];

      if (field.isId()) {
        sql.append(field.columnName()).append(" = ?");
      }
    }
    
    return sql.toString();
  }

  public Object[] saveValues(T entity)
  {
    Object []values = new Object[_saveColumns];
    Map<String,Object> doc = null;
    
    if (_isDocument) {
      doc = new HashMap<>();
      values[values.length - 1] = doc;
    }
    
    int i = 0;
    for (FieldInfo<T,?> field : _fields) {
      if (field.isColumn()) {
        values[i++] = field.toParam(field.getValue(entity));
      }
      else {
        doc.put(field.columnName(), field.getValue(entity));
      }
    }
    
    return values;
  }

  public Object getValue(int index, T bean)
  {
    Object value = _fields[index].getValue(bean);
    
    if (value == null && _fields[index].isId()) {
      value = new Long(0);
    }

    return value;
  }

  public T readObject(Cursor cursor, boolean isInitPk)
    throws ReflectiveOperationException
  {
    Objects.requireNonNull(cursor);

    FieldInfoObject fieldObject = null;

    int index = 0;
    for (int i = 0; i < _fields.length; i++) {
      FieldInfo<T,?> field = _fields[i];

      if (! field.isId() || isInitPk)
        index++;

      if (field instanceof FieldInfoObject) {
        fieldObject = (FieldInfoObject) field;
        break;
      }
    }

    T t;

    if (fieldObject == null) {
      t = createFromClass();
      load(cursor, t, isInitPk);
    }
    else {
      t = createFromCursor(cursor, index);
    }

    return t;
  }

  public void load(Cursor cursor, T entity)
  {
    try {
      Objects.requireNonNull(cursor);
      Objects.requireNonNull(entity);
    
      load(cursor, entity, false);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw ServiceException.createAndRethrow(e);
    }
  }

  private void load(Cursor cursor, T bean, boolean isInitId)
    throws IllegalAccessException
  {
    _fieldState.setObject(bean, StateAsset.LOADED);
    
    int index = 0;
    for (int i = 0; i < _fields.length; i++) {
      FieldInfo<T,?> field = _fields[i];

      if (! isInitId && field.isId()) {
        continue;
      }
      else if (field.isColumn()) {
        index++;

        field.setValue(bean, cursor, index);
      }
    }
    
    if (isDocument()) {
      Object doc = cursor.getObject(index + 1);

      if (doc instanceof Map) {
        Map<String,Object> docMap = (Map) doc;
      
        for (FieldInfo<T,?> field : _fields) {
          field.setValueFromDocument(bean, docMap);
        }
      }
    }
  }

  public void loadFail(T entity)
  {
    Objects.requireNonNull(entity);

    _fieldState.setObject(entity, StateAsset.UNLOADED);
  }

  public boolean isDeleting(T entity)
  {
    StateAsset state = (StateAsset) _fieldState.getObject(entity);
    
    return state != null && state == StateAsset.DELETING;
  }

  public void delete(T entity)
  {
    StateAsset state = (StateAsset) _fieldState.getObject(entity);
    
    if (state == null) {
    }
    else if (state == StateAsset.DELETING) {
      _fieldState.setObject(entity, StateAsset.DELETED);
    }
    else {
      throw new IllegalStateException(state.toString());
    }
  }

  private T createFromClass()
    throws ReflectiveOperationException
  {
    Constructor ctor = _type.getConstructor();

    if (ctor == null)
      throw new IllegalStateException(
        L.l("{0} must provide default constructor", _type));

    ctor.setAccessible(true);

    return (T) ctor.newInstance();
  }

  private T createFromCursor(Cursor cursor, int index)
  {
    T t = null;

    if (cursor != null)
      t = (T) cursor.getObject(index);

    return t;
  }

  private Asset makeDefaultTable(Class type)
  {
    String name = type.getSimpleName();

    if (name.endsWith("Impl"))
      name = name.substring(0, name.lastIndexOf("Impl"));

    return new TableLiteral(name);
  }

  private ColumnVault makeDefaultColumn(Field field)
  {
    String name = field.getName();

    if (name.startsWith("_"))
      name = name.substring(1);

    return new ColumnLiteral(name);
  }

  public void fillColumns(TableInfo tableInfo)
  {
    for (FieldInfo<T,?> field : _fields) {
      field.fillColumn(tableInfo);
    }
    
    _isDocument = tableInfo.column("__doc") != null;
  }

  public void setPk(T t, ID id)
    throws IllegalAccessException
  {
    id().setValue(t, id);
  }

  public FieldInfo findFieldByType(TypeRef resultType)
  {
    Class<?> rawResult = resultType.rawClass();

    for (FieldInfo<T,?> field : _fields) {
      if (field.isId())
        continue;

      Class<?> fieldType = field.getJavaType();

      if (rawResult.equals(fieldType))
        return field;

      if (fieldType.isPrimitive()
          && rawResult.equals(_primitiveWrappers.get(fieldType))) {
        return field;
      }
    }

    return null;
  }

  @Override
  public String toString()
  {
    return AssetInfo.class.getSimpleName()
           + "["
           + _type
           + ':'
           + tableName()
           + "]";
  }

  public StringBuilder selectField(FieldInfo field)
  {
    StringBuilder sql = new StringBuilder("select ")
      .append(field.columnName())
      .append(" from ").append(_tableName);

    return sql;
  }

  private static class IdGenerator<ID>
  {
    private AssetInfo<?,?> _entity;
    
    IdGenerator(AssetInfo<?,?> entity)
    {
      _entity = entity;
    }
    
    protected AssetInfo<?,?> entity()
    {
      return _entity;
    }
    
    public ID generate(ID id)
    {
      return id;
    }
    
    public String generateAddress(ID id)
    {
      id = generate(id);
      
      return _entity.prefix() + "/" + id;
    }
  }
  
  private static class IdGeneratorLong extends IdGenerator<Long>
  {
    private IdentityGenerator _idGen;
    
    private IdGeneratorLong(AssetInfo<?,?> entity)
    {
      super(entity);
      
      _idGen = IdentityGenerator.newGenerator().node(entity.node()).get();
    }
    
    @Override
    public Long generate(Long id)
    {
      if (id != null && id.longValue() != 0) {
        return id;
      }

      return new Long(_idGen.get());
    }
  }
  
  private static class IdGeneratorIdAsset extends IdGenerator<IdAsset>
  {
    private IdentityGenerator _idGen;
    
    private IdGeneratorIdAsset(AssetInfo<?,?> assetInfo)
    {
      super(assetInfo);
      
      _idGen = IdentityGenerator.newGenerator().node(assetInfo.node()).get();
    }
    
    @Override
    public IdAsset generate(IdAsset id)
    {
      if (id != null) {
        return id;
      }

      return new IdAsset(_idGen.get());
    }
  }

  private static class TableLiteral extends AnnotationLiteral<Asset> implements Asset
  {
    private final String _name;

    public TableLiteral(String table)
    {
      _name = table;
    }

    @Override
    public String value()
    {
      return _name;
    }
  }

  private static class ColumnLiteral extends AnnotationLiteral<ColumnVault>
    implements ColumnVault
  {
    private final String _name;

    public ColumnLiteral(String name)
    {
      _name = name;
    }

    @Override
    public String name()
    {
      return _name;
    }
  }

  static {
    _idGenMap = new HashMap<>();

    _idGenMap.put(Long.class, IdGeneratorLong::new);
    _idGenMap.put(long.class, IdGeneratorLong::new);
    
    _idGenMap.put(IdAsset.class, IdGeneratorIdAsset::new);

    _primitiveWrappers.put(boolean.class, Boolean.class);
    _primitiveWrappers.put(char.class, Character.class);
    _primitiveWrappers.put(byte.class, Byte.class);
    _primitiveWrappers.put(short.class, Short.class);
    _primitiveWrappers.put(int.class, Integer.class);
    _primitiveWrappers.put(long.class, Long.class);
    _primitiveWrappers.put(float.class, Float.class);
    _primitiveWrappers.put(double.class, Double.class);
  }
}
