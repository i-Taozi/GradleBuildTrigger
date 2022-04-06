package org.tessell.gwt.user.client.ui;

import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.google.gwt.user.client.ui.IsWidget;

public interface IsHorizontalPanel extends IsCellPanel {

  HorizontalAlignmentConstant getHorizontalAlignment();

  VerticalAlignmentConstant getVerticalAlignment();

  void insert(IsWidget w, int beforeIndex);

  void setHorizontalAlignment(HorizontalAlignmentConstant align);

  void setVerticalAlignment(VerticalAlignmentConstant align);

}
