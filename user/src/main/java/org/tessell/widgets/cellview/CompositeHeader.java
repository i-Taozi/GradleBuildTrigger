package org.tessell.widgets.cellview;

import static org.tessell.widgets.cellview.Cells.newCompositeCell;
import static org.tessell.widgets.cellview.Cells.newHeader;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Renders multiple headers as one header.
 *
 * We reuse {@link CompositeCell}'s magic, however it was built assuming it was
 * going to put together composite columns ({@link HasCell}s), which headers
 * are not, so we have to cheat a little bit.
 *
 * <b>Note:</b> that to react to browser events, headers must extend {@link ExposedUpdaterHeader},
 * which allows this implementation to get the header's {@link ValueUpdater}.
 */
public class CompositeHeader extends DelegateIsHeader<Object> {

  public CompositeHeader(final IsHeader<?>... headers) {
    super(newHeader(new ConstantHeaderValue<Object>(null), newCompositeCell(toHasCells(headers))));
  }

  public CompositeHeader(final IsCompositeCell<Object> cell) {
    super(newHeader(new ConstantHeaderValue<Object>(null), cell));
  }

  /** @return {@code headers} wrapped into fake {@link HasCell}s for {@link CompositeCell}. */
  public static List<HasCell<Object, ?>> toHasCells(final IsHeader<?>... headers) {
    final List<HasCell<Object, ?>> cells = new ArrayList<HasCell<Object, ?>>();
    for (final IsHeader<?> header : headers) {
      cells.add(newHasCell(header));
    }
    return cells;
  }

  /** Creates a fake {@link HasCell} for {@code header} to satisfy the {@link CompositeCell} cstr. */
  private static <X> HasCell<Object, X> newHasCell(final IsHeader<X> header) {
    return new HeaderHasCell<X>(header);
  }

  public static final class HeaderHasCell<X> implements HasCell<Object, X> {
    private final IsHeader<X> header;
    private final Cell<X> headerCell;

    private HeaderHasCell(final IsHeader<X> header) {
      this.header = header;
      headerCell = new DelegateCell<X>(header.getCell()) {
        /** Instead of rendering the cell directly, give the header a chance to run it's render logic. */
        @Override
        public void render(Cell.Context context, X value, SafeHtmlBuilder sb) {
          header.asHeader().render(context, sb);
        }

        /** Instead of passing the event to the cell directly, give the header a chance to run it's event logic. */
        @Override
        public void onBrowserEvent(Context context, Element parent, X value, NativeEvent event, ValueUpdater<X> valueUpdater) {
          header.asHeader().onBrowserEvent(context, parent, event);
        }
      };
    }

    @Override
    public Cell<X> getCell() {
      return headerCell;
    }

    @Override
    public FieldUpdater<Object, X> getFieldUpdater() {
      if (header.getUpdater() == null) {
        return null;
      }
      return new FieldUpdater<Object, X>() {
        public void update(final int index, final Object object, final X value) {
          header.getUpdater().update(value);
        }
      };
    }

    @Override
    public X getValue(final Object object) {
      return header.getValue();
    }
  }

}
