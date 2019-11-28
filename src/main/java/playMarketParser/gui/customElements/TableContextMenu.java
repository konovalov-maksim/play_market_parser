package playMarketParser.gui.customElements;

import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import playMarketParser.Global;

import java.util.ResourceBundle;

public class TableContextMenu extends ContextMenu {

    public <S> TableContextMenu(TableView<S> table) {
        ResourceBundle rb = Global.getBundle();
        final int IMG_SIZE = 20;

        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.getSelectionModel().setCellSelectionEnabled(true);

        //ѕункт " опирование выделенных €чеек"
        MenuItem copyItem = new MenuItem(rb.getString("copy"));
        ImageView copyIcon = new ImageView("/images/icons/file_copy.png");
        copyIcon.setFitHeight(IMG_SIZE);
        copyIcon.setFitWidth(IMG_SIZE);
        copyItem.setGraphic(copyIcon);
        copyItem.setOnAction(a -> {
            StringBuilder selectedData = new StringBuilder();
            Integer prevRowIndex = null;
            Integer prevColIndex = null;
            int startColIndex = Integer.MAX_VALUE;
            for (TablePosition pos : table.getSelectionModel().getSelectedCells())
                if (pos.getColumn() < startColIndex) startColIndex = pos.getColumn();
            //ѕеребираем все выделенные €чейки и записываем их в буфер
            for (TablePosition pos : table.getSelectionModel().getSelectedCells()) {
                int rowIndex = pos.getRow();
                int colIndex = pos.getColumn();
                Object cellData = table.getColumns().get(pos.getColumn()).getCellData(rowIndex);
                String cellString = cellData != null ? cellData.toString() : "";
                if (prevRowIndex == null) selectedData.append(cellString);
                else if (prevRowIndex == rowIndex) {
                    for (int i = prevColIndex; i < colIndex; i++) selectedData.append("\t");
                    selectedData.append(cellString);
                }
                else {
                    selectedData.append("\n");
                    for (int i = startColIndex; i < colIndex; i++) selectedData.append("\t");
                    selectedData.append(cellString);
                }
                prevColIndex = colIndex;
                prevRowIndex = rowIndex;
            }
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(selectedData.toString());
            Clipboard.getSystemClipboard().setContent(clipboardContent);

        });
        copyItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));

        //ѕункт "”даление выделенных строк"
        MenuItem removeItem = new MenuItem(rb.getString("remove"));
        ImageView delIcon = new ImageView("/images/icons/delete.png");
        delIcon.setFitHeight(IMG_SIZE);
        delIcon.setFitWidth(IMG_SIZE);
        removeItem.setGraphic(delIcon);
        removeItem.setOnAction(a -> table.getItems().removeAll(table.getSelectionModel().getSelectedItems()));
        removeItem.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));

        this.getItems().addAll(copyItem, removeItem);

        //ƒелаем меню видимым только дл€ непустых строк таблицы
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
