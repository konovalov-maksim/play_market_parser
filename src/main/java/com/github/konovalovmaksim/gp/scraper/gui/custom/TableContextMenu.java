package com.github.konovalovmaksim.gp.scraper.gui.custom;

import com.github.konovalovmaksim.gp.scraper.Global;
import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;

import java.util.ResourceBundle;

public class TableContextMenu extends ContextMenu {

    public <S> TableContextMenu(TableView<S> table) {
        ResourceBundle rb = Global.getBundle();
        final int IMG_SIZE = 16;

        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.getSelectionModel().setCellSelectionEnabled(true);

        //Пункт "Копирование выделенных ячеек"
        MenuItem copyItem = new MenuItem(rb.getString("copy"));
        ImageView copyIcon = new ImageView("/image/icon/file_copy.png");
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
            //Перебираем все выделенные ячейки и записываем их в буфер
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

        //Пункт "Копирование выделенных ячеек"
        MenuItem copyColItem = new MenuItem(rb.getString("copyCol"));
        ImageView copyColIcon = new ImageView("/image/icon/copy_col.png");
        copyColIcon.setFitHeight(IMG_SIZE);
        copyColIcon.setFitWidth(IMG_SIZE);
        copyColItem.setGraphic(copyColIcon);
        copyColItem.setOnAction(a -> {
            int colIndex = table.getFocusModel().getFocusedCell().getColumn();
            StringBuilder selectedData = new StringBuilder();
            for (int i = 0; i < table.getItems().size(); i++) {
                Object cellData = table.getColumns().get(colIndex).getCellData(i);
                if (cellData != null && cellData.toString().length() > 0) selectedData.append(cellData.toString()).append("\n");
            }
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(selectedData.toString());
            Clipboard.getSystemClipboard().setContent(clipboardContent);
        });
        copyColItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN));


        //Пункт "Удаление выделенных строк"
        MenuItem removeItem = new MenuItem(rb.getString("remove"));
        ImageView delIcon = new ImageView("/image/icon/remove.png");
        delIcon.setFitHeight(IMG_SIZE);
        delIcon.setFitWidth(IMG_SIZE);
        removeItem.setGraphic(delIcon);
        removeItem.setOnAction(a -> table.getItems().removeAll(table.getSelectionModel().getSelectedItems()));
        removeItem.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));

        this.getItems().addAll(copyItem, copyColItem, removeItem);

        //Делаем меню видимым только для непустых строк таблицы
        table.setRowFactory(c -> {
            TableRow<S> row = new TableRow<>();
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(this));
            return row;
        });
    }

    public MenuItem getCopyItem() {
        return this.getItems().get(0);
    }

    public MenuItem getCopyColItem() {
        return this.getItems().get(1);
    }

    public MenuItem getRemoveItem() {
        return this.getItems().get(2);
    }

}
