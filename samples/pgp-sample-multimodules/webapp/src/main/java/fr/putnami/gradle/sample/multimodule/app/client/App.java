package fr.putnami.gradle.sample.multimodule.app.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import fr.putnami.gradle.sample.multimodule.person.client.PersonEditor;

public class App extends Composite implements EntryPoint {

	interface Binder extends UiBinder<Widget, App> {
	}

	private final Binder binder = GWT.create(Binder.class);

	@UiField
	PersonEditor personEditor;

	public App() {
		initWidget(binder.createAndBindUi(this));
	}

  @Override
  public void onModuleLoad() {
		RootPanel.get().add(this);
  }

	@UiHandler("clickMe")
	void onClickMe(ClickEvent event) {
		Window.alert("Hi " + personEditor.flush().getName());

	}
}
