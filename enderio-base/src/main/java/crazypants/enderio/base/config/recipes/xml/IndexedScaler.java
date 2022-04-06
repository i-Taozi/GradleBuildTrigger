package crazypants.enderio.base.config.recipes.xml;

import javax.annotation.Nonnull;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import com.enderio.core.common.util.NNList;

import crazypants.enderio.base.config.recipes.InvalidRecipeConfigException;
import crazypants.enderio.base.config.recipes.StaxFactory;

public class IndexedScaler extends Scaler {

  private float step = 1f;
  private NNList<Data> data = new NNList<>();

  @Override
  public Scaler readResolve() throws InvalidRecipeConfigException, XMLStreamException {
    // No super on purpose
    try {
      if (step <= 0f) {
        throw new InvalidRecipeConfigException("'step' is invalid");
      }

      boolean valid = !data.isEmpty();

      float[] dataArray = new float[data.size()];
      for (int i = 0; i < data.size(); i++) {
        valid &= data.get(i).isValid();
        if (valid) {
          dataArray[i] = data.get(i).getValue();
        }
      }

      if (!valid) {
        throw new InvalidRecipeConfigException("no <data>");
      }

      scaler = of(new crazypants.enderio.base.capacitor.IndexedScaler(step, dataArray));
    } catch (InvalidRecipeConfigException e) {
      throw new InvalidRecipeConfigException(e, "in <indexed>");
    }
    return this;
  }

  @Override
  public void enforceValidity() throws InvalidRecipeConfigException {
  }

  @Override
  public boolean setAttribute(StaxFactory factory, String name, String value) throws InvalidRecipeConfigException, XMLStreamException {
    if ("step".equals(name)) {
      this.step = Float.parseFloat(value);
      return true;
    }

    return false;
  }

  @Override
  public boolean setElement(StaxFactory factory, String name, StartElement startElement) throws InvalidRecipeConfigException, XMLStreamException {
    if ("data".equals(name)) {
      data.add(factory.read(new Data(), startElement));
      return true;
    }

    return super.setElement(factory, name, startElement);
  }

  @Override
  public @Nonnull String getScalerString() {
    return ((crazypants.enderio.base.capacitor.IndexedScaler) scaler.get()).store();
  }
}
