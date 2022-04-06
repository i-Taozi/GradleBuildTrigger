package org.swellrt.beta.client.platform.web.editor.caret.view;

import org.swellrt.beta.client.platform.web.editor.caret.CaretInfo;
import org.swellrt.beta.client.platform.web.editor.caret.CaretView;

import com.google.gwt.dom.client.Element;

/**
 * Wraps a GWT caret view with a generic interface.
 *
 */
public class LegacyCaretView implements CaretView {

  private CaretWidget widget;

  public LegacyCaretView() {
    widget = new CaretWidget();
  }

  @Override
  public Element getElement() {
    return widget.getElement();
  }

  @Override
  public CaretInfo update(CaretInfo info) {
    if (info == null)
      return null;
    this.widget.setColor(info.getSession().getColor());
    this.widget.setName(info.getSession().getName());
    this.widget.setCompositionState(info.getCompositionState());
    return info;
  }

}
