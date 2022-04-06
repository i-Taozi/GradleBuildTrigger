package org.tessell.model.properties;

import java.util.Map;

import org.tessell.model.events.PropertyChangedEvent;
import org.tessell.model.events.PropertyChangedHandler;
import org.tessell.model.validation.events.RuleTriggeredHandler;
import org.tessell.model.validation.events.RuleUntriggeredHandler;
import org.tessell.model.validation.rules.Rule;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Converts a source property from another type, using a {@link PropertyConverter}.
 *
 * Although technically a separate property instance, all validation rules/errors/etc.
 * are deferred to our source property.
 *
 * @param <SP> the source property type
 * @param <DP> the destination property type
 */
public class ConvertedProperty<DP, SP> extends AbstractAbstractProperty<DP> {

  private final Property<SP> source;
  private final PropertyConverter<SP, DP> converter;

  public ConvertedProperty(final Property<SP> source, final PropertyConverter<SP, DP> converter) {
    this.source = source;
    this.converter = converter;
  }

  @Override
  public String toString() {
    return source.getValueName() + " " + get();
  }

  @Override
  public DP get() {
    SP value = source.get();
    return (value == null) ? converter.nullValue() : converter.to(value);
  }

  @Override
  public void set(DP value) {
    throw new IllegalStateException(this + " is a derived value");
  }

  @Override
  public void set(DP value, boolean shouldTouch) {
    throw new IllegalStateException(this + " is a derived value");
  }

  @Override
  public void setInitialValue(DP value) {
    throw new IllegalStateException(this + " is a derived value");
  }

  @Override
  public void setDefaultValue(DP value) {
    throw new IllegalStateException(this + " is a derived value");
  }

  @Override
  public DP getValue() {
    return get();
  }

  @Override
  public void setValue(DP value) {
    throw new IllegalStateException(this + " is a derived value");
  }

  @Override
  public void setValue(DP value, boolean fire) {
    throw new IllegalStateException(this + " is a derived value");
  }

  @Override
  public void setIfNull(DP value) {
    throw new IllegalStateException(this + " is a derived value");
  }

  @Override
  public HandlerRegistration addRuleTriggeredHandler(RuleTriggeredHandler handler) {
    return source.addRuleTriggeredHandler(handler);
  }

  @Override
  public HandlerRegistration addRuleUntriggeredHandler(RuleUntriggeredHandler handler) {
    return source.addRuleUntriggeredHandler(handler);
  }

  @Override
  public void reassess() {
    source.reassess();
  }

  @Override
  @SuppressWarnings("unchecked")
  public void addRule(Rule<? super DP> rule) {
    source.addRule((Rule<? super SP>) rule); // hm, doesn't look right
  }

  @Override
  public boolean isTouched() {
    return source.isTouched();
  }

  @Override
  public void setTouched(boolean touched) {
    source.setTouched(touched);
  }

  @Override
  public boolean touch() {
    return source.touch();
  }

  @Override
  public boolean isValid() {
    return source.isValid();
  }

  @Override
  public Property<Boolean> valid() {
    return source.valid();
  }

  @Override
  public Property<Boolean> touched() {
    return source.touched();
  }

  @Override
  public Property<Boolean> changed() {
    return source.changed();
  }

  @Override
  public void fireEvent(GwtEvent<?> event) {
    source.fireEvent(event);
  }

  @Override
  public <T extends Property<?>> T addDerived(T downstream, Object token, boolean touch) {
    return source.addDerived(downstream, token, touch);
  }

  @Override
  public <T extends Property<?>> T removeDerived(T downstream, Object token) {
    return source.removeDerived(downstream, token);
  }

  @Override
  public Property<DP> depends(Property<?>... upstream) {
    source.depends(upstream);
    return this;
  }

  @Override
  public HandlerRegistration addPropertyChangedHandler(final PropertyChangedHandler<DP> handler) {
    return source.addPropertyChangedHandler(new PropertyChangedHandler<SP>() {
      public void onPropertyChanged(PropertyChangedEvent<SP> event) {
        DP oldValue = event.getOldValue() == null ? null : converter.to(event.getOldValue());
        DP newValue = event.getNewValue() == null ? null : converter.to(event.getNewValue());
        handler.onPropertyChanged(new PropertyChangedEvent<DP>(ConvertedProperty.this, oldValue, newValue));
      }
    });
  }

  @Override
  public HandlerRegistration nowAndOnChange(final PropertyValueHandler<DP> handler) {
    return source.nowAndOnChange(new PropertyValueHandler<SP>() {
      public void onValue(SP value) {
        DP newValue = value == null ? null : converter.to(value);
        handler.onValue(newValue);
      }
    });
  }

  @Override
  public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<DP> handler) {
    return source.addValueChangeHandler(new ValueChangeHandler<SP>() {
      public void onValueChange(ValueChangeEvent<SP> event) {
        DP newValue = event.getValue() == null ? null : converter.to(event.getValue());
        // need an inner class because ValueChangedEvent's cstr is protected
        handler.onValueChange(new ValueChangeEvent<DP>(newValue) {
        });
      }
    });
  }

  @Override
  public String getName() {
    return source.getName();
  }

  @Override
  public String getValueName() {
    return source.getValueName();
  }

  @Override
  public Map<Object, String> getErrors() {
    return source.getErrors();
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }

  @Override
  public boolean isRequired() {
    return source.isRequired();
  }

  @Override
  public void setRequired(boolean required) {
    source.setRequired(required);
  }

}
