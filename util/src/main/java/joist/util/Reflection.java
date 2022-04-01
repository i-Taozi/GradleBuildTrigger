package joist.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Reflection {

  private Reflection() {
  }

  public static Object invoke(String methodName, Object target, Object... params) {
    Class<?>[] types = new Class<?>[params.length];
    for (int i = 0; i < params.length; i++) {
      types[i] = params[i].getClass();
    }
    Method m = null;
    try {
      m = target.getClass().getMethod(methodName);
    } catch (NoSuchMethodException nsme) {
      throw new RuntimeException(nsme);
    }
    return Reflection.invoke(m, target, params);
  }

  public static Object invoke(Method method, Object target, Object... params) {
    try {
      return method.invoke(target, params);
    } catch (InvocationTargetException ite) {
      if (ite.getTargetException() instanceof RuntimeException) {
        throw (RuntimeException) ite.getTargetException();
      } else {
        throw new RuntimeException(ite.getTargetException());
      }
    } catch (IllegalAccessException iea) {
      throw new RuntimeException(iea);
    }
  }

  public static Object get(Field field, Object target) {
    try {
      if (!field.isAccessible()) {
        field.setAccessible(true);
      }
      return field.get(target);
    } catch (IllegalAccessException iae) {
      throw new RuntimeException(iae);
    }
  }

  public static void set(Field field, Object target, Object value) {
    try {
      field.set(target, value);
    } catch (IllegalAccessException iae) {
      throw new RuntimeException(iae);
    }
  }

  public static Object newInstance(String className) {
    return Reflection.newInstance(Reflection.forName(className));
  }

  public static <T> T newInstance(Class<T> type) {
    try {
      return type.newInstance();
    } catch (IllegalAccessException iea) {
      throw new RuntimeException(iea);
    } catch (InstantiationException ie) {
      throw new RuntimeException(ie);
    }
  }

  public static Class<?> forName(String className) {
    Class<?> c = Reflection.forNameOrNull(className);
    if (c == null) {
      throw new RuntimeException("Class " + className + " was not found.");
    }
    return c;
  }

  public static Class<?> forNameOrNull(String className) {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  public static Field getField(Class<?> c, String fieldName) {
    try {
      return c.getField(fieldName);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    } catch (SecurityException e) {
      throw new RuntimeException(e);
    }
  }
}
