package fr.putnami.gradle.sample.lib.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import fr.putnami.gradle.sample.lib.shared.Person;

public class PersonEditor extends Composite implements Editor<Person> {

	interface Binder extends UiBinder<Widget, PersonEditor> {
	}

	interface Driver extends SimpleBeanEditorDriver<Person, PersonEditor> {
	}

	private final Binder binder = GWT.create(Binder.class);
	private final Driver driver = GWT.create(Driver.class);

	@UiField
	@Path("name")
	TextBox name;
	@UiField
	@Path("email")
	PasswordTextBox email;

	public PersonEditor() {
		initWidget(binder.createAndBindUi(this));
		driver.initialize(this);
		driver.edit(new Person());
	}

	public void edit(Person person) {
		this.driver.edit(person);
	}

	public Person flush() {
		return driver.flush();
	}
}
