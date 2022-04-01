package joist.perf;

import joist.domain.orm.Db;
import joist.domain.orm.Repository;
import joist.domain.uow.UoW;

import com.sun.japex.TestCase;

import features.domain.Parent;

public class JoistDriver extends com.sun.japex.JapexDriverBase {

  @Override
  public void initializeDriver() {
    if (Repository.datasource == null) {
      Repository.configure(Db.PG, "features");
    }
  }

  @Override
  public void run(TestCase testCase) {
    int number = new Integer(testCase.getParam("number"));
    boolean commitOnEach = testCase.getBooleanParam("commitOnEach");
    boolean insert = testCase.getParam("type").equals("insert");
    if (!commitOnEach) {
      UoW.open(null);
    }

    for (int i = 0; i < number; i++) {
      if (commitOnEach) {
        UoW.open(null);
      }

      if (insert) {
        new Parent(String.valueOf(i));
      } else {
        Parent.queries.find(i + 1).setName(System.currentTimeMillis() + "");
      }

      if (commitOnEach) {
        UoW.commit();
        UoW.close();
      }
    }

    if (!commitOnEach) {
      UoW.commit();
      UoW.close();
    }
  }

}
