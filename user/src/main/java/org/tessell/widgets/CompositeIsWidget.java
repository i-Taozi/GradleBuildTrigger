package org.tessell.widgets;

import org.tessell.gwt.dom.client.IsElement;
import org.tessell.gwt.dom.client.IsStyle;
import org.tessell.gwt.user.client.ui.IsWidget;

import com.google.gwt.event.dom.client.DomEvent.Type;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base class for making logical {@link IsWidget}s that can be unit tested.
 *
 * E.g.:
 *
 * <code>
 *     class MyWidget extends CompositeIsWidget {
 *       private final IsTextBox textBox = newTextBox();
 *
 *       public MyWidget() {
 *         setWidget(textBox);
 *       }
 *
 *       public void customLogic() {
 *         textBox.getStyle().setBackgroundColor("blue");
 *       }
 *     }
 * </code>
 *
 * Note that only widgets that do not rely directly on the DOM
 * can be built in this fashion. E.g. {@link #onBrowserEvent(Event)}
 * is not implemented for stub/test widgets.
 *
 * If you do have a widget that needs direct DOM access, you'll
 * have to make your own {@code IsMyWidget}, {@code GwtMyWidget},
 * and {@code StubMyWidget} classes, so that {@code GwtMyWidget}
 * can do whatever it wants with DOM/browser-coupled code, and
 * {@code StubMyWidget} will pretend to do it for tests.
 */
public class CompositeIsWidget implements IsWidget {

  protected IsWidget widget;

  /** Sets our composite widgets, and returns it for fluent/inline assignments. */
  protected <W extends IsWidget> W setWidget(final W widget) {
    if (this.widget != null) {
      throw new IllegalStateException("CompositeIsWidget.setWidget was already called");
    }
    this.widget = widget;
    return widget;
  }

  @Override
  public Widget asWidget() {
    if (widget == null) {
      throw new IllegalStateException("CompositeIsWidget.setWidget was not called");
    }
    return widget.asWidget();
  }

  @Override
  public void ensureDebugId(final String id) {
    widget.ensureDebugId(id);
    // This is hacky way as it won't get compiled out like regular
    // widget's onEnsureDebugId (which is called by GWT's DebugImpl)
    onEnsureDebugId(id);
  }

  /** Method for subclasses to override to set debug ids on their children. */
  protected void onEnsureDebugId(String id) {
    // We're not actually called by GWT's DebugImpl, as it calls the
    // widget's onEnsureDebugId, so we don't have to do anything.
  }

  @Override
  public int getAbsoluteLeft() {
    return widget.getAbsoluteLeft();
  }

  @Override
  public int getAbsoluteTop() {
    return widget.getAbsoluteTop();
  }

  @Override
  public int getOffsetHeight() {
    return widget.getOffsetHeight();
  }

  @Override
  public int getOffsetWidth() {
    return widget.getOffsetWidth();
  }

  @Override
  public void onBrowserEvent(final Event event) {
    widget.onBrowserEvent(event);
  }

  @Override
  public void fireEvent(final GwtEvent<?> event) {
    widget.fireEvent(event);
  }

  @Override
  public void addStyleName(final String styleName) {
    widget.addStyleName(styleName);
  }

  @Override
  public IsStyle getStyle() {
    return getIsElement().getStyle();
  }

  @Override
  public String getStyleName() {
    return widget.getStyleName();
  }

  @Override
  public void setStyleName(String styleName) {
    widget.setStyleName(styleName);
  }

  @Override
  public void removeStyleName(final String styleName) {
    widget.removeStyleName(styleName);
  }

  @Override
  public IsElement getIsElement() {
    return widget.getIsElement();
  }

  @Override
  public HandlerRegistration addAttachHandler(Handler handler) {
    return widget.addAttachHandler(handler);
  }

  @Override
  public <H extends EventHandler> HandlerRegistration addDomHandler(H handler, Type<H> type) {
    return widget.addDomHandler(handler, type);
  }

  @Override
  public boolean isAttached() {
    return widget.isAttached();
  }

  public IsWidget getIsWidget() {
    return widget;
  }

  @Override
  public IsWidget getIsParent() {
    return widget.getIsParent();
  }
}
