package joist.converter;

import joist.util.TestCounters;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ConverterInheritanceTest {

  private ConverterRegistry r = new ConverterRegistry();

  @Before
  public void setUp() {
    TestCounters.resetAll();
    this.r.addConverter(new AbstractConverter<FakeDomainObject, String>() {
      public String convertOneToTwo(FakeDomainObject value, Class<? extends String> toType) {
        return value.getId().toString();
      }

      public FakeDomainObject convertTwoToOne(String value, Class<? extends FakeDomainObject> toType) {
        try {
          // We get the toType, so even though we return the generic DomainObject, we have
          // the concrete type to do database lookups, etc.
          FakeDomainObject d = toType.newInstance();
          d.setId(new Integer(value));
          return d;
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });
  }

  @Test
  public void testInterfaceConverterWorksForClass() {
    // Employee --> String picks up the DomainObjectToString converter
    Assert.assertEquals("1", this.r.convert(new Employee(1), String.class));
    // String --> Employee picks up the DomainObjectToString converter
    Assert.assertEquals(1, this.r.convert("1", Employee.class).getId().intValue());
  }

  @Test
  public void testProbingIsCached() {
    // Start at 0
    Assert.assertEquals(0, ConverterRegistry.probes.get());

    // We have to probe the first time
    Assert.assertEquals("1", this.r.convert(new Employee(1), String.class));
    Assert.assertEquals(1, ConverterRegistry.probes.get());

    // The second time we won't have to probe
    Assert.assertEquals("1", this.r.convert(new Employee(1), String.class));
    Assert.assertEquals(1, ConverterRegistry.probes.get());
  }

  public interface FakeDomainObject {
    Integer getId();

    void setId(Integer id);
  }

  public static class Employee implements FakeDomainObject {
    public Integer id;

    public Employee() {
    }

    public Employee(Integer id) {
      this.id = id;
    }

    public Integer getId() {
      return this.id;
    }

    public void setId(Integer id) {
      this.id = id;
    }
  }
}
