package joist.sourcegen;

import joist.util.Join;

import org.junit.Assert;
import org.junit.Test;

public class GEnumClassTest {

  @Test
  public void testEmptyEnum() {
    GClass gc = new GClass("foo.bar.Foo").setEnum();
    Assert.assertEquals(Join.lines(//
      "package foo.bar;",
      "",
      "public enum Foo {",
      "",
      "}",
      ""), gc.toCode());
  }

  @Test
  public void testOneValue() {
    GClass gc = new GClass("foo.bar.Foo").setEnum();
    gc.addEnumValue("FOO");
    Assert.assertEquals(Join.lines(//
      "package foo.bar;",
      "",
      "public enum Foo {",
      "",
      "    FOO",
      "",
      "}",
      ""), gc.toCode());
  }

  @Test
  public void testTwoValues() {
    GClass gc = new GClass("foo.bar.Foo").setEnum();
    gc.addEnumValue("FOO");
    gc.addEnumValue("BAR");
    Assert.assertEquals(Join.lines(//
      "package foo.bar;",
      "",
      "public enum Foo {",
      "",
      "    FOO,",
      "    BAR",
      "",
      "}",
      ""), gc.toCode());
  }

  @Test
  public void testPrivateFields() {
    GClass gc = new GClass("foo.bar.Foo").setEnum();
    gc.getField("i").type("Integer").makeGetter();
    gc.getField("j").type("String").makeGetter();
    gc.addEnumValue("FOO(0, \"foo\")");
    gc.addEnumValue("BAR(1, \"bar\")");

    GMethod c = gc.getConstructor("Integer i", "String j").setPrivate();
    c.body.line("this.i = i;");
    c.body.line("this.j = j;");
    Assert.assertEquals(Join.linesWithTickToQuote(
      "package foo.bar;",
      "",
      "public enum Foo {",
      "",
      "    FOO(0, 'foo'),",
      "    BAR(1, 'bar');",
      "",
      "    private Integer i;",
      "    private String j;",
      "",
      "    private Foo(Integer i, String j) {",
      "        this.i = i;",
      "        this.j = j;",
      "    }",
      "",
      "    public Integer getI() {",
      "        return this.i;",
      "    }",
      "",
      "    public String getJ() {",
      "        return this.j;",
      "    }",
      "",
      "}",
      ""), gc.toCode());
  }

  public enum Foo {
    FOO, BAR
  }

}
