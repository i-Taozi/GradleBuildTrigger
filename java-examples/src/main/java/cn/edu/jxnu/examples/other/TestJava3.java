/* All Contributors (C) 2020 */
package cn.edu.jxnu.examples.other;

/**
 * 测试语法
 *
 * @author 梦境迷离
 * @time 2018-09-08
 */
public class TestJava3 {

    public static void main(String[] args) {
        A a = new A();
        System.out.println(a instanceof B); // true a与父类 lgtm [java/useless-type-test]
        System.out.println(a instanceof C); // true a与父接口 lgtm [java/useless-type-test]
        System.out.println(a instanceof D); // false a与子类 lgtm [java/useless-type-test]
    }
}

interface C {}

abstract class B implements C {}

class A extends B {}

class D extends A {}
