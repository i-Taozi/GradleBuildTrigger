package joist.sourcegen;

import joist.util.Join;

import org.junit.Assert;
import org.junit.Test;

public class GFieldTest {

  @Test
  public void testOneField() {
    GClass gc = new GClass("foo.bar.Foo");
    gc.getField("id").type(int.class);
    Assert.assertEquals(Join.lines("package foo.bar;",//
      "",
      "public class Foo {",
      "",
      "    private int id;",
      "",
      "}",
      ""), gc.toCode());
  }

  @Test
  public void testOnePublicField() {
    GClass gc = new GClass("foo.bar.Foo");
    gc.getField("id").type(int.class).setPublic();
    Assert.assertEquals(Join.lines("package foo.bar;",//
      "",
      "public class Foo {",
      "",
      "    public int id;",
      "",
      "}",
      ""), gc.toCode());
  }

  @Test
  public void testOneFieldWithDefaultValue() {
    GClass gc = new GClass("foo.bar.Foo");
    gc.getField("id").type(Integer.class).initialValue("null");
    Assert.assertEquals(Join.lines(new Object[] { "package foo.bar;",//
      "",
      "public class Foo {",
      "",
      "    private Integer id = null;",
      "",
      "}",
      "" }), gc.toCode());
  }

  @Test
  public void testOnePublicFieldWithTypeImported() {
    GClass gc = new GClass("foo.bar.Foo");
    gc.getField("id").type("foo.zaz.Bar<Integer>").setPublic();
    Assert.assertEquals(Join.lines("package foo.bar;",//
      "",
      "import foo.zaz.Bar;",
      "",
      "public class Foo {",
      "",
      "    public Bar<Integer> id;",
      "",
      "}",
      ""), gc.toCode());
  }

  @Test
  public void testOneFieldOneGetter() {
    GClass gc = new GClass("foo.bar.Foo");
    gc.getField("id").type(int.class);
    gc.getMethod("getId").returnType(int.class).body.append("return this.id;");
    Assert.assertEquals(Join.lines(
      "package foo.bar;",
      "",
      "public class Foo {",
      "",
      "    private int id;",
      "",
      "    public int getId() {",
      "        return this.id;",
      "    }",
      "",
      "}",
      ""), gc.toCode());
  }

  @Test
  public void testOneFieldAssignedToAnonymousInnerClass() {
    GClass gc = new GClass("foo.bar.Foo");
    GField foo = gc.getField("foo").type("Shim<Foo>").setStatic().setFinal();

    GClass fooc = foo.initialAnonymousClass();
    fooc.getMethod("getFoo").returnType("Foo").body.append("return null;");

    Assert.assertEquals(Join.lines(
      "package foo.bar;",
      "",
      "public class Foo {",
      "",
      "    private static final Shim<Foo> foo = new Shim<Foo>() {",
      "        public Foo getFoo() {",
      "            return null;",
      "        }",
      "    };",
      "",
      "}",
      ""), gc.toCode());
  }

  @Test
  public void testTwoFieldsAssignedToAnonymousInnerClass() {
    GClass gc = new GClass("foo.bar.Foo");
    GField foo = gc.getField("foo").type("Shim<Foo>").setStatic().setFinal();
    GClass fooc = foo.initialAnonymousClass();
    fooc.getMethod("getFoo").returnType("Foo").body.append("return null;");

    GField bar = gc.getField("bar").type("Shim<Bar>").setStatic().setFinal();
    GClass barc = bar.initialAnonymousClass();
    barc.getMethod("getBar").returnType("Bar").body.append("return null;");

    Assert.assertEquals(Join.lines(
      "package foo.bar;",
      "",
      "public class Foo {",
      "",
      "    private static final Shim<Foo> foo = new Shim<Foo>() {",
      "        public Foo getFoo() {",
      "            return null;",
      "        }",
      "    };",
      "    private static final Shim<Bar> bar = new Shim<Bar>() {",
      "        public Bar getBar() {",
      "            return null;",
      "        }",
      "    };",
      "",
      "}",
      ""), gc.toCode());
  }

  @Test
  public void testOneFieldWithGetter() {
    GClass gc = new GClass("foo.bar.Foo");
    gc.getField("id").type(Integer.class).initialValue("null").makeGetter();
    Assert.assertEquals(
      Join.lines(new Object[] {
        "package foo.bar;",
        "",
        "public class Foo {",
        "",
        "    private Integer id = null;",
        "",
        "    public Integer getId() {",
        "        return this.id;",
        "    }",
        "",
        "}",
        "" }),
      gc.toCode());
  }

  @Test
  public void testOneFieldAnnotated() {
    GClass gc = new GClass("foo.bar.Foo");
    gc.getField("id").type(Integer.class).initialValue("null").addAnnotation("@SuppressWarnings");
    Assert.assertEquals(
      Join.lines(new Object[] {
        "package foo.bar;",
        "",
        "public class Foo {",
        "",
        "    @SuppressWarnings",
        "    private Integer id = null;",
        "",
        "}",
        "" }),
      gc.toCode());
  }

  @Test
  public void testAutoImportInitialValue() {
    GClass gc = new GClass("foo.Foo");
    gc.getField("bar").type("bar.IBar<zaz.Zaz>").initialValue("new bar.BarImpl<zaz.Zaz>()").autoImportInitialValue();
    Assert.assertEquals(Join.lines("package foo;",//
      "",
      "import bar.BarImpl;",
      "import bar.IBar;",
      "import zaz.Zaz;",
      "",
      "public class Foo {",
      "",
      "    private IBar<Zaz> bar = new BarImpl<Zaz>();",
      "",
      "}",
      ""), gc.toCode());
  }
}
