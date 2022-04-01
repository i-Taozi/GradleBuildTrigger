package features.domain;

import java.util.List;

import joist.util.Copy;

import org.junit.Assert;
import org.junit.Test;

public class PrimitivesUpdateTest extends AbstractFeaturesTest {

  @Test
  public void testChangeFlag() {
    new Primitives("testSave");
    this.commitAndReOpen();

    Assert.assertFalse(Primitives.queries.find(1).getFlag());
    List<Long> ids = Copy.list(1l);
    Primitives.queries.setFlag(ids, true);
    this.commitAndReOpen();

    Assert.assertEquals(true, Primitives.queries.find(1).getFlag().booleanValue());
  }

  @Test
  public void testChangeFlagWithDynamicList() {
    new Primitives("foo1");
    new Primitives("foo2");
    new Primitives("bar");
    this.commitAndReOpen();

    List<Long> ids = Primitives.queries.findIdsWithNameLike("foo%");
    Assert.assertEquals(2, ids.size());
    Assert.assertFalse(Primitives.queries.find(1).getFlag());
    Assert.assertFalse(Primitives.queries.find(2).getFlag());
    Assert.assertFalse(Primitives.queries.find(3).getFlag());

    Primitives.queries.setFlag(ids, true);
    this.commitAndReOpen();

    Assert.assertTrue(Primitives.queries.find(1).getFlag());
    Assert.assertTrue(Primitives.queries.find(2).getFlag());
    Assert.assertFalse(Primitives.queries.find(3).getFlag());
  }

}
