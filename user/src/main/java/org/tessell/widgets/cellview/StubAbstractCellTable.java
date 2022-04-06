package org.tessell.widgets.cellview;

import static org.tessell.widgets.cellview.Cells.newSafeHtmlHeader;
import static org.tessell.widgets.cellview.Cells.newTextHeader;

import java.util.ArrayList;
import java.util.List;

import org.tessell.util.ObjectUtils;
import org.tessell.widgets.StubAbstractHasDataWidget;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.RowStyles;

public class StubAbstractCellTable<T> extends StubAbstractHasDataWidget<T> implements IsAbstractCellTable<T> {

  private final List<IsColumn<T, ?>> columns = new ArrayList<IsColumn<T, ?>>();
  private final List<IsHeader<?>> headers = new ArrayList<IsHeader<?>>();
  private final List<IsHeader<?>> footers = new ArrayList<IsHeader<?>>();
  private final ColumnSortList sortList = new ColumnSortList();
  private RowStyles<T> rowStyles;

  public StubAbstractCellTable() {
  }

  public StubAbstractCellTable(int pageSize) {
    super(pageSize);
  }

  /** @return the stub headers for testing. */
  public IsHeader<?> getHeader(int index) {
    return headers.get(index);
  }

  /** @return the stub footers for testing. */
  public IsHeader<?> getFooter(int index) {
    return footers.get(index);
  }

  /** @return the stub column for testing. */
  public StubColumn<T, ?> getColumn(int index) {
    return (StubColumn<T, ?>) columns.get(index);
  }

  public String getValues(int displayedIndex) {
    String values = "";
    for (int i = 0; i < columns.size(); i++) {
      values += ObjectUtils.toStr(getColumn(i).getValue(displayedIndex), "null");
      if (i < columns.size() - 1) {
        values += " || ";
      }
    }
    return values;
  }

  @Override
  public void addColumn(final IsColumn<T, ?> col) {
    addColumn(col, (IsHeader<?>) null, (IsHeader<?>) null);
  }

  @Override
  public void addColumn(final IsColumn<T, ?> col, final IsHeader<?> header) {
    addColumn(col, header, null);
  }

  @Override
  public void addColumn(final IsColumn<T, ?> col, final IsHeader<?> header, final IsHeader<?> footer) {
    ((StubColumn<T, ?>) col).setStubCellTable(this);
    columns.add(col);
    headers.add(header); // could be null
    footers.add(footer); // could be null
  }

  @Override
  public void redrawHeaders() {
    redraw();
  }

  @Override
  public void redrawFooters() {
    redraw();
  }

  @Override
  public void removeColumn(IsColumn<T, ?> col) {
    removeColumn(columns.indexOf(col));
  }

  @Override
  public void removeColumn(int index) {
    columns.remove(index);
    headers.remove(index);
    footers.remove(index);
  }

  @Override
  public int getColumnCount() {
    return columns.size();
  }

  @Override
  public IsColumn<T, ?> getIsColumn(int col) {
    return columns.get(col);
  }

  @Override
  public int getColumnIndex(IsColumn<T, ?> column) {
    return columns.indexOf(column);
  }

  @Override
  public ColumnSortList getColumnSortList() {
    return sortList;
  }

  @Override
  public HandlerRegistration addColumnSortHandler(ColumnSortEvent.Handler handler) {
    return handlers.addHandler(ColumnSortEvent.getType(), handler);
  }

  @Override
  public void addColumn(IsColumn<T, ?> col, String headerString) {
    addColumn(col, newTextHeader(headerString));
  }

  @Override
  public void addColumn(IsColumn<T, ?> col, SafeHtml headerHtml) {
  }

  @Override
  public void addColumn(IsColumn<T, ?> col, String headerString, String footerString) {
    addColumn(col, newTextHeader(headerString), newTextHeader(footerString));
  }

  @Override
  public void addColumn(IsColumn<T, ?> col, SafeHtml headerHtml, SafeHtml footerHtml) {
    addColumn(col, newSafeHtmlHeader(headerHtml), newSafeHtmlHeader(footerHtml));
  }

  @Override
  public void setColumnWidth(IsColumn<T, ?> col, String width) {
  }

  @Override
  public void setColumnWidth(IsColumn<T, ?> col, double width, Unit unit) {
  }

  @Override
  public void setRowStyles(RowStyles<T> rowStyles) {
    this.rowStyles = rowStyles;
  }

  public String getRowStyles(int i) {
    return rowStyles.getStyleNames(getVisibleItem(i), i);
  }

}
