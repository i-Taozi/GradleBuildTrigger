package joist.sourcegen;

import java.net.MalformedURLException;

import joist.util.Join;

import org.junit.Assert;
import org.junit.Test;

public class GMethodTest {

  @Test
  public void testOverloadedMethods() {
    GClass gc = new GClass("Foo");
    gc.getMethod("method(String one)");
    gc.getMethod("method(Integer two)");
    Assert.assertEquals(Join.lines(//
      "public class Foo {",
      "",
      "    public void method(String one) {",
      "    }",
      "",
      "    public void method(Integer two) {",
      "    }",
      "",
      "}",
      ""), gc.toCode());
  }

  @Test
  public void testOverloadedMethodsWithArguments() {
    GClass gc = new GClass("Foo");
    gc.getMethod("method", Argument.arg("String", "one"));
    gc.getMethod("method", Argument.arg("Integer", "two"));
    Assert.assertEquals(Join.lines(//
      "public class Foo {",
      "",
      "    public void method(String one) {",
      "    }",
      "",
      "    public void method(Integer two) {",
      "    }",
      "",
      "}",
      ""), gc.toCode());
  }

  @Test
  public void testAutoImport() {
    GClass gc = new GClass("Foo");
    gc.getMethod("method").argument("foo.zaz.Bar", "bar").returnType("foo.zaz.Foo");
    Assert.assertEquals(Join.lines(//
      "import foo.zaz.Bar;",
      "",
      "public class Foo {",
      "",
      "    public foo.zaz.Foo method(Bar bar) {",
      "    }",
      "",
      "}",
      ""), gc.toCode());
  }

  @Test
  public void testAutoImportWithTypesAndNames() {
    GClass gc = new GClass("Foo");
    gc.getMethod("method").arguments("foo.zaz.Bar bar", "foo.zaz.Blah blah").returnType("foo.zaz.Foo");
    Assert.assertEquals(Join.lines(//
      "import foo.zaz.Bar;",
      "import foo.zaz.Blah;",
      "",
      "public class Foo {",
      "",
      "    public foo.zaz.Foo method(Bar bar, Blah blah) {",
      "    }",
      "",
      "}",
      ""), gc.toCode());
  }

  @Test
  public void testAutoImportWithPackagesWithNumbers() {
    GClass gc = new GClass("Foo");
    gc.getMethod("method").arguments("foo2.zaz.Bar bar");
    Assert.assertEquals(Join.lines(//
      "import foo2.zaz.Bar;",
      "",
      "public class Foo {",
      "",
      "    public void method(Bar bar) {",
      "    }",
      "",
      "}",
      ""), gc.toCode());
  }

  @Test
  public void testAutoImportWithCrazyGenerics() {
    GClass gc = new GClass("Foo");
    gc.getMethod("method1").argument("Bar<java.lang.Integer>", "bar");
    gc.getMethod("method2").argument("foo.Zaz<java.lang.Integer>", "bar");
    gc.getMethod("method3").argument("foo.Zaz<java.util.Set<java.lang.Integer>>", "bar");
    Assert.assertEquals(Join.lines(//
      "import foo.Zaz;",
      "import java.util.Set;",
      "",
      "public class Foo {",
      "",
      "    public void method1(Bar<Integer> bar) {",
      "    }",
      "",
      "    public void method2(Zaz<Integer> bar) {",
      "    }",
      "",
      "    public void method3(Zaz<Set<Integer>> bar) {",
      "    }",
      "",
      "}",
      ""), gc.toCode());
  }

  @Test
  public void testAutoImportDirectly() {
    GClass gc = new GClass("foo.Foo");
    gc.addImports("foo.Zaz<java.lang.Integer>");
    gc.addImports("foo.Set<java.util.Set<java.lang.Integer>>");
    Assert.assertEquals(Join.lines(//
      "package foo;",
      "",
      "public class Foo {",
      "",
      "}",
      ""), gc.toCode());
  }

  @Test
  public void testAutoImportWithCollidingNames() {
    GClass gc = new GClass("Foo");
    gc.getMethod("method1").argument("foo.Bar<java.lang.Integer>", "bar");
    gc.getMethod("method2").argument("foo.Bar<java.lang.Integer>", "bar");
    gc.getMethod("method3").argument("zaz.Bar<java.lang.Integer>", "bar");
    Assert.assertEquals(Join.lines(//
      "import foo.Bar;",
      "",
      "public class Foo {",
      "",
      "    public void method1(Bar<Integer> bar) {",
      "    }",
      "",
      "    public void method2(Bar<Integer> bar) {",
      "    }",
      "",
      "    public void method3(zaz.Bar<Integer> bar) {",
      "    }",
      "",
      "}",
      ""), gc.toCode());
  }

  @Test
  public void testOneMethodAnnotated() {
    GClass gc = new GClass("foo.bar.Foo");
    GMethod hello = gc.getMethod("hello");
    hello.arguments("String foo");
    hello.setBody("return 'Hi' + foo;");
    hello.addAnnotation("@SuppressWarnings").addOverride();
    Assert.assertEquals(
      Join.lines(new Object[] {
        "package foo.bar;",
        "",
        "public class Foo {",
        "",
        "    @SuppressWarnings",
        "    @Override",
        "    public void hello(String foo) {",
        "        return \"Hi\" + foo;",
        "    }",
        "",
        "}",
        "" }),
      gc.toCode());
  }

  @Test
  public void testOneMethodThrows() {
    GClass gc = new GClass("foo.bar.Foo");
    GMethod hello = gc.getMethod("hello");
    hello.arguments("String foo");
    hello.setBody("return 'Hi' + foo;");
    hello.addThrows(Exception.class.getName());
    hello.addThrows(MalformedURLException.class.getName());
    Assert.assertEquals(
      Join.lines(new Object[] {
        "package foo.bar;",
        "",
        "public class Foo {",
        "",
        "    public void hello(String foo) throws Exception, MalformedURLException {",
        "        return \"Hi\" + foo;",
        "    }",
        "",
        "}",
        "" }),
      gc.toCode());
  }

  @Test
  public void testIndentDoesNotAddExtraWhiteSpace() {
    GClass gc = new GClass("foo.bar.Foo");
    GMethod hello = gc.getMethod("hello");
    hello.body.line("int i = 1;");
    hello.body.line("");
    hello.body.line("return i;");
    Assert.assertEquals(
      Join.lines(new Object[] {
        "package foo.bar;",
        "",
        "public class Foo {",
        "",
        "    public void hello() {",
        "        int i = 1;",
        "",
        "        return i;",
        "    }",
        "",
        "}",
        "" }),
      gc.toCode());
  }

  @Test
  public void testTypeParameters() {
    GClass gc = new GClass("foo.bar.Foo");
    gc.getMethod("hello").arguments("T foo").typeParameters("T");
    gc.getMethod("goodbye").arguments("T foo").typeParameters("T").setStatic();
    Assert.assertEquals(
      Join.lines(new Object[] {
        "package foo.bar;",
        "",
        "public class Foo {",
        "",
        "    public <T> void hello(T foo) {",
        "    }",
        "",
        "    public static <T> void goodbye(T foo) {",
        "    }",
        "",
        "}",
        "" }),
      gc.toCode());
  }

  @Test
  public void testOverloadedWithNoArguments() {
    GClass gc = new GClass("foo.bar.Foo");
    gc.getMethod("hello()");
    Assert.assertEquals(
      Join.lines(new Object[] { "package foo.bar;", "", "public class Foo {", "", "    public void hello() {", "    }", "", "}", "" }),
      gc.toCode());
  }

  @Test
  public void testOverloadedWithTwoTypeArguments() {
    GClass gc = new GClass("foo.bar.Foo");
    gc.getMethod("hello(Map<K, V> map)").typeParameters("K, V");
    Assert.assertEquals(
      Join.lines(new Object[] {
        "package foo.bar;",
        "",
        "public class Foo {",
        "",
        "    public <K, V> void hello(Map<K, V> map) {",
        "    }",
        "",
        "}",
        "" }),
      gc.toCode());
  }

  @Test
  public void testAbstract() {
    GClass gc = new GClass("Foo").setAbstract();
    gc.getMethod("method(String one)").setAbstract();
    Assert.assertEquals(Join.lines(//
      "public abstract class Foo {",
      "",
      "    public abstract void method(String one);",
      "",
      "}",
      ""), gc.toCode());
  }

  @Test
  public void testAssignFieldsOfParameters() {
    GClass gc = new GClass("Foo");
    gc.getField("one").type("String");
    gc.getField("two").type("Integer");
    gc.getMethod("method", Argument.arg("String", "one"), Argument.arg("Integer", "two")).assignFields();
    Assert.assertEquals(Join.lines(//
      "public class Foo {",
      "",
      "    private String one;",
      "    private Integer two;",
      "",
      "    public void method(String one, Integer two) {",
      "        this.one = one;",
      "        this.two = two;",
      "    }",
      "",
      "}",
      ""), gc.toCode());
  }

}
