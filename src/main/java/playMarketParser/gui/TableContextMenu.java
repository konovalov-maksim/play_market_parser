package playMarketParser.gui;

import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import playMarketParser.Global;

import java.util.ResourceBundle;

class TableContextMenu extends ContextMenu {

    <S> TableContextMenu(TableView<S> table) {
        ResourceBundle rb = Global.getBundle();
        int IMG_SIZE = 20;

        MenuItem copyItem = new MenuItem(rb.getString("copy"));
        ImageView copyIcon = new ImageView("/images/icons/file copy.png");
        copyIcon.setFitHeight(IMG_SIZE);
        copyIcon.setFitWidth(IMG_SIZE);
        copyItem.setGraphic(copyIcon);
        copyItem.setOnAction(a -> {
            TablePosition pos = table.getSelectionModel().getSelectedCells().get(0);
            S item = table.getItems().get(pos.getRow());
            TableColumn col = pos.getTableColumn();
            ClipboardContent cellValue = new ClipboardContent();
            cellValue.putString(String.valueOf(col.getCellObservableValue(item).getValue()));
            Clipboard.getSystemClipboard().setContent(cellValue);
        });

        MenuItem removeItem = new MenuItem(rb.getString("remove"));
        ImageView delIcon = new ImageView("/images/icons/delete.png");
        delIcon.setFitHeight(IMG_SIZE);
        delIcon.setFitWidth(IMG_SIZE);
        removeItem.setGraphic(delIcon);
        removeItem.setOnAction(a -> table.getItems().remove(table.getSelectionModel().getSelectedItem()));

        this.getItems().addAll(copyItem, removeItem);

        table.setRowFactory(c -> {
            TableRow<S> row = new TableRow<>();
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(this));
            return row;
        });
    }

    public MenuItem getRemoveItem() {
        return this.getItems().get(1);
    }

    public MenuItem getCopyItem() {
        return this.getItems().get(0);
    }
}
