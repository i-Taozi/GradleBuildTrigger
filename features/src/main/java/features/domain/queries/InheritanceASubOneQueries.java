package features.domain.queries;

import java.util.List;

import joist.domain.orm.queries.Select;
import features.domain.InheritanceASubOne;
import features.domain.InheritanceASubOneAlias;

public class InheritanceASubOneQueries extends InheritanceASubOneQueriesCodegen {

  public List<InheritanceASubOne> findAll() {
    InheritanceASubOneAlias sa = new InheritanceASubOneAlias("sa");
    return Select.from(sa).list();
  }

  public InheritanceASubOne findByName(String name) {
    InheritanceASubOneAlias sa = new InheritanceASubOneAlias("sa");
    Select<InheritanceASubOne> q = Select.from(sa);
    q.where(sa.name.eq(name));
    return q.unique();
  }

}
