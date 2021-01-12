package com.appgrade.gp.scraper.gui.customElements;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class RowNumCellFactory<T, E> implements Callback<TableColumn<T, E>, TableCell<T, E>> {

    public static final int WIDTH = 32;

    @Override
    public TableCell<T, E> call(TableColumn<T, E> column) {
        TableCell cell = new TableCell<T, E>() {
            @Override
            protected void updateItem(E item, boolean empty) {
                super.updateItem(item, empty);
                setText(!empty ? this.getTableRow().getIndex() + 1 + "" : "");
            }
        };

        cell.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                cell.getTableView().getSelectionModel().clearSelection();
                cell.getTableView().getSelectionModel().selectRange(
                        cell.getIndex(),
                        (TableColumn) cell.getTableView().getColumns().get(1),
                        cell.getIndex(),
                        (TableColumn) cell.getTableView().getColumns().get(cell.getTableView().getColumns().size() - 1));
            }
        });
        cell.getStyleClass().add("row-num-col-cell");
        return cell;
    }

}
