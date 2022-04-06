package fr.putnami.gradle.sample.webapp.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class App extends Composite implements EntryPoint {

	interface Binder extends UiBinder<Widget, App> {
	}

	private final Binder binder = GWT.create(Binder.class);

	@UiField
	TextBox name;
	@UiField
	FlowPanel outDiv;

	public App() {
		initWidget(binder.createAndBindUi(this));
	}

  @Override
  public void onModuleLoad() {
		RootPanel.get().add(this);
  }

	@UiHandler("sayHi")
	void onClickMe(ClickEvent event) {
		outDiv.add(new Label("Hi " + name.getValue() + "!"));
	}
}
