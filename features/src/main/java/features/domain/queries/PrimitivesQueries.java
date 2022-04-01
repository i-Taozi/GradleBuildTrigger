package features.domain.queries;

import java.util.List;

import joist.domain.orm.queries.Select;
import joist.domain.orm.queries.Update;
import features.domain.Primitives;
import features.domain.PrimitivesAlias;

public class PrimitivesQueries extends PrimitivesQueriesCodegen {

  public List<Primitives> findByIdBetween(Long lower, Long upper) {
    PrimitivesAlias p = new PrimitivesAlias("p");
    return Select.from(p).where(p.id.between(lower, upper)).list();
  }

  public Primitives findByFlagValue(boolean value) {
    PrimitivesAlias p = new PrimitivesAlias("p");
    Select<Primitives> q = Select.from(p);
    q.where(p.flag.eq(value));
    return q.unique();
  }

  public List<Long> findIdsWithNameLike(String name) {
    PrimitivesAlias p = new PrimitivesAlias("p");
    Select<Primitives> q = Select.from(p);
    q.where(p.name.like(name));
    return q.listIds();
  }

  public long countWhereFlagIs(boolean flag) {
    PrimitivesAlias p = new PrimitivesAlias("p");
    Select<Primitives> q = Select.from(p);
    q.where(p.flag.eq(flag));
    return q.count();
  }

  public void setFlag(List<Long> ids, boolean value) {
    PrimitivesAlias p = new PrimitivesAlias("p");
    Update<Primitives> u = Update.into(p);
    u.set(p.flag.to(true));
    u.where(p.id.in(ids));
    u.execute();
  }

  public List<String> findNamesOnly() {
    PrimitivesAlias p = new PrimitivesAlias("p");
    Select<Primitives> q = Select.from(p);
    q.select(p.name.as("name"));
    q.orderBy(p.name.asc());
    return q.listValues(String.class);
  }

  public String findNameOnly(int id) {
    PrimitivesAlias p = new PrimitivesAlias("p");
    Select<Primitives> q = Select.from(p);
    q.select(p.name.as("name"));
    q.where(p.id.eq(id));
    return q.uniqueValueOrNull(String.class);
  }

  public List<NameAndFlag> findNameAndFlagOnly() {
    PrimitivesAlias p = new PrimitivesAlias("p");
    Select<Primitives> q = Select.from(p);
    q.select(p.name.as("name"), p.flag.as("flag"));
    q.orderBy(p.id.asc());
    return q.list(NameAndFlag.class);
  }

  public static class NameAndFlag {
    public String name;
    public Boolean flag;
  }

}
