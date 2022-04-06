package org.tessell.gwt.user.client.ui;


public class StubHTML extends StubLabel implements IsHTML {

  private String html;

  @Override
  public String getHTML() {
    return html;
  }

  @Override
  public void setHTML(final String html) {
    this.html = html;
  }

}
