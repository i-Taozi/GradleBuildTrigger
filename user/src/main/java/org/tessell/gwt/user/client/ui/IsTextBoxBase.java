package org.tessell.gwt.user.client.ui;

import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.TextBoxBase.TextAlignConstant;

@SuppressWarnings("deprecation")
public interface IsTextBoxBase extends IsValueBoxBase<String> {

  @Deprecated
  void setTextAlignment(TextAlignConstant align);

  void setPlaceholder(String placeholder);

  String getPlaceholder();

  int getMaxLength();

  void setMaxLength(int length);

  TextBoxBase asWidget();

}
