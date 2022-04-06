package org.tessell.gwt.user.client.ui;

import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;

public class StubHorizontalPanel extends StubCellPanel implements IsHorizontalPanel {

  private HorizontalAlignmentConstant horizontal;
  private VerticalAlignmentConstant vertical;

  @Override
  public HorizontalAlignmentConstant getHorizontalAlignment() {
    return horizontal;
  }

  @Override
  public VerticalAlignmentConstant getVerticalAlignment() {
    return vertical;
  }

  @Override
  public void insert(com.google.gwt.user.client.ui.IsWidget w, int beforeIndex) {
    add(beforeIndex, w);
  }

  @Override
  public void setHorizontalAlignment(HorizontalAlignmentConstant align) {
    horizontal = align;
  }

  @Override
  public void setVerticalAlignment(VerticalAlignmentConstant align) {
    vertical = align;
  }

}
