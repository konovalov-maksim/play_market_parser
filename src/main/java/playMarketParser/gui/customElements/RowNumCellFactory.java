package playMarketParser.gui.customElements;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class RowNumCellFactory<T, E> implements Callback<TableColumn<T, E>, TableCell<T, E>> {

    @Override
    public TableCell<T, E> call(TableColumn<T, E> column) {
        return new TableCell<T, E>() {
            @Override
            protected void updateItem(E item, boolean empty) {
                super.updateItem(item, empty);
                setText( !empty ? this.getTableRow().getIndex() + 1 + "" : "");
            }
        };
    }

}
