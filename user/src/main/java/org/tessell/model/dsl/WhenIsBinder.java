package org.tessell.model.dsl;

import org.tessell.gwt.animation.client.IsAnimation;
import org.tessell.gwt.user.client.ui.HasCss;
import org.tessell.gwt.user.client.ui.IsWidget;
import org.tessell.model.commands.UiCommand;
import org.tessell.model.properties.Property;
import org.tessell.model.validation.events.RuleTriggeredEvent;
import org.tessell.model.validation.events.RuleUntriggeredEvent;
import org.tessell.util.WidgetUtils;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HasEnabled;

public class WhenIsBinder<P> {

  private final Binder b;
  private final Property<P> property;
  private final WhenCondition<P> condition;
  private boolean trigged = false;

  public WhenIsBinder(final Binder b, final Property<P> property, final WhenCondition<P> condition) {
    this.b = b;
    this.property = property;
    this.condition = condition;
  }

  public WhenIsSetStyleBinder<P> set(final String style) {
    return new WhenIsSetStyleBinder<P>(b, property, condition, style);
  }

  public WhenIsSetOrElseStyleBinder<P> setOrElse(final String ifTrueStyle, final String ifFalseStyle) {
    return new WhenIsSetOrElseStyleBinder<P>(b, property, condition, ifTrueStyle, ifFalseStyle);
  }

  /** @return a fluent {@link SetPropertyBinder} to set a value on {@code other} when this condition is true. */
  public <Q> WhenIsSetValueBinder<P, Q> set(final TakesValue<Q> value) {
    return new WhenIsSetValueBinder<P, Q>(b, property, condition, value);
  }

  public <V> WhenIsRemoveBinder<P, V> remove(final V newValue) {
    return new WhenIsRemoveBinder<P, V>(b, property, condition, newValue);
  }

  public <V> WhenIsAddBinder<P, V> add(final V newValue) {
    return new WhenIsAddBinder<P, V>(b, property, condition, newValue);
  }

  public WhenIsAttachBinder<P> attach(final IsWidget widget) {
    return new WhenIsAttachBinder<P>(b, property, condition, widget);
  }

  public void run(final Runnable... runnables) {
    b.add(property.addPropertyChangedHandler(e -> runIfCondition(runnables)));
    runIfCondition(runnables); // run initial
  }

  // UiCommands are Runnables (now), but execute is still a nice alias
  public void execute(final UiCommand command) {
    run(command);
  }

  public void show(final HasCss... csses) {
    b.add(property.addPropertyChangedHandler(e -> showIfCondition(csses)));
    showIfCondition(csses); // set initial
  }

  public void hide(final HasCss... csses) {
    b.add(property.addPropertyChangedHandler(e -> hideIfCondition(csses)));
    hideIfCondition(csses); // set initial
  }

  public void visible(final HasCss... css) {
    b.add(property.addPropertyChangedHandler(e -> visibleIfCondition(css)));
    visibleIfCondition(css); // set initial
  }

  public void error(final String message) {
    b.add(property.addPropertyChangedHandler(e -> errorIfCondition(message)));
    errorIfCondition(message);
  }

  public void enable(final HasEnabled... enabled) {
    b.add(property.addPropertyChangedHandler(e -> updateEnabled(enabled, true)));
    updateEnabled(enabled, true); // set initial value
  }

  public void disable(final HasEnabled... enabled) {
    b.add(property.addPropertyChangedHandler(e -> updateEnabled(enabled, false)));
    updateEnabled(enabled, false); // set initial value
  }

  public void fadeIn(final HasCss widget) {
    final IsAnimation[] lastAnimation = { null };
    b.add(property.addPropertyChangedHandler(e -> {
      if (lastAnimation[0] != null) {
        lastAnimation[0].cancel();
      }
      if (condition.evaluate(property)) {
        lastAnimation[0] = WidgetUtils.fadeIn(widget);
      } else {
        lastAnimation[0] = WidgetUtils.fadeOut(widget);
      }
    }));
    // set initial value
    if (condition.evaluate(property)) {
      lastAnimation[0] = WidgetUtils.fadeIn(widget);
    } else {
      // assume we want to hide right away
      WidgetUtils.hide(widget);
    }
  }

  private void updateEnabled(final HasEnabled[] enabled, final boolean valueIfTrue) {
    final boolean valueToSet = condition.evaluate(property) ? valueIfTrue : !valueIfTrue;
    for (final HasEnabled e : enabled) {
      e.setEnabled(valueToSet);
    }
  }

  private void runIfCondition(Runnable... runnables) {
    if (condition.evaluate(property)) {
      for (Runnable runnable : runnables) {
        runnable.run();
      }
    }
  }

  private void errorIfCondition(final String message) {
    if (condition.evaluate(property)) {
      property.fireEvent(new RuleTriggeredEvent(this, message, new Boolean[] { false }));
      trigged = true;
    } else if (trigged) {
      property.fireEvent(new RuleUntriggeredEvent(this, message));
      trigged = false;
    }
  }

  private void showIfCondition(final HasCss... csses) {
    for (final HasCss css : csses) {
      if (condition.evaluate(property)) {
        css.getStyle().clearDisplay();
      } else {
        css.getStyle().setDisplay(Display.NONE);
      }
    }
  }

  private void hideIfCondition(final HasCss... csses) {
    for (final HasCss css : csses) {
      if (condition.evaluate(property)) {
        css.getStyle().setDisplay(Display.NONE);
      } else {
        css.getStyle().clearDisplay();
      }
    }
  }

  private void visibleIfCondition(final HasCss... csses) {
    for (final HasCss css : csses) {
      if (condition.evaluate(property)) {
        css.getStyle().clearVisibility();
      } else {
        css.getStyle().setVisibility(Visibility.HIDDEN);
      }
    }
  }
}
