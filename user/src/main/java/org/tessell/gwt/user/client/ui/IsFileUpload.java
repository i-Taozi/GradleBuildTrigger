package org.tessell.gwt.user.client.ui;


import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasName;

public interface IsFileUpload extends IsWidget, HasName, HasEnabled, HasChangeHandlers {

  String getFilename();

}
