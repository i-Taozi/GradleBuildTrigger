package crazypants.enderio.base.config.recipes.xml;

import javax.xml.stream.XMLStreamException;

import crazypants.enderio.base.config.recipes.InvalidRecipeConfigException;
import crazypants.enderio.base.config.recipes.StaxFactory;

public class ItemFloatAmount extends Item {

  protected float amount = 1f;

  @Override
  public boolean setAttribute(StaxFactory factory, String name, String value) throws InvalidRecipeConfigException, XMLStreamException {
    if ("amount".equals(name)) {
      try {
        this.amount = Float.parseFloat(value);
      } catch (NumberFormatException e) {
        throw new InvalidRecipeConfigException("Invalid value in 'amount': Not a number");
      }
      return true;
    }

    return super.setAttribute(factory, name, value);
  }

}
