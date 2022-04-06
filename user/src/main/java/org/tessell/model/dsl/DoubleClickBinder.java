package org.tessell.model.dsl;

import com.google.gwt.event.dom.client.HasDoubleClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;

public class DoubleClickBinder extends EventBinder {

  private final HasDoubleClickHandlers clickable;

  DoubleClickBinder(final Binder b, final HasDoubleClickHandlers clickable) {
    super(b);
    this.clickable = clickable;
  }

  @Override
  protected HandlerRegistration hookUpRunnable(final Runnable runnable) {
    return clickable.addDoubleClickHandler(e -> runnable.run());
  }

  @Override
  protected HandlerRegistration hookUpEventRunnable(final DomEventRunnable runnable) {
    return clickable.addDoubleClickHandler(e -> runnable.run(e));
  }

}
