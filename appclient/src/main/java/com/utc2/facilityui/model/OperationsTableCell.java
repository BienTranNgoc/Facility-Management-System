package com.utc2.facilityui.model;

import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import java.io.InputStream;
import javafx.scene.Cursor; // <<< ĐÃ THÊM IMPORT NÀY

public class OperationsTableCell<S> extends TableCell<S, Void> {

    private final ImageView editButton = new ImageView();
    private final ImageView deleteButton = new ImageView();
    private final HBox actionGroup = new HBox(10); // Khoảng cách giữa các nút là 10
    private final ObjectProperty<OperationsEventHandler<S>> eventHandler = new SimpleObjectProperty<>();

    public OperationsTableCell() {
        // Load hình ảnh
        try {
            InputStream editStream = getClass().getResourceAsStream("/com/utc2/facilityui/images/pencil.png");
            InputStream deleteStream = getClass().getResourceAsStream("/com/utc2/facilityui/images/delete.png");

            if (editStream != null) {
                editButton.setImage(new Image(editStream));
                editStream.close(); // Nên đóng stream sau khi sử dụng
            } else {
                System.err.println("Lỗi: Không tìm thấy tệp /com/utc2/facilityui/images/pencil.png");
            }

            if (deleteStream != null) {
                deleteButton.setImage(new Image(deleteStream));
                deleteStream.close(); // Nên đóng stream sau khi sử dụng
            } else {
                System.err.println("Lỗi: Không tìm thấy tệp /com/utc2/facilityui/images/delete.png");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải hình ảnh cho OperationsTableCell: " + e.getMessage());
            e.printStackTrace();
        }

        // Thiết lập kích thước cố định cho ImageView nếu cần, để giữ bố cục ổn định
        // Ví dụ:
        // editButton.setFitWidth(20);
        // editButton.setFitHeight(20);
        // deleteButton.setFitWidth(20);
        // deleteButton.setFitHeight(20);

        // Thiết lập con trỏ chuột hình bàn tay cho các ImageView
        editButton.setCursor(Cursor.HAND);    // <<< ĐÃ THÊM
        deleteButton.setCursor(Cursor.HAND);  // <<< ĐÃ THÊM

        actionGroup.getChildren().addAll(editButton, deleteButton);
        actionGroup.setAlignment(Pos.CENTER); // Căn giữa các nút trong HBox

        // Thông báo hành động cho Controller thông qua EventHandler
        editButton.setOnMouseClicked(event -> {
            S itemData = getTableRow() != null ? getTableRow().getItem() : null;
            if (itemData != null && eventHandler.get() != null) {
                eventHandler.get().onEdit(itemData);
            }
        });

        deleteButton.setOnMouseClicked(event -> {
            S itemData = getTableRow() != null ? getTableRow().getItem() : null;
            if (itemData != null && eventHandler.get() != null) {
                eventHandler.get().onDelete(itemData);
            }
        });

        // Không cần gọi setGraphic ở đây vì updateItem sẽ xử lý
        // setGraphic(actionGroup);
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || getTableRow() == null || getTableRow().getItem() == null) {
            setGraphic(null);
        } else {
            setGraphic(actionGroup);
        }
    }

    public ObjectProperty<OperationsEventHandler<S>> eventHandlerProperty() {
        return eventHandler;
    }

    public OperationsEventHandler<S> getEventHandler() {
        return eventHandler.get();
    }

    public void setEventHandler(OperationsEventHandler<S> handler) {
        this.eventHandler.set(handler);
    }

    public interface OperationsEventHandler<T> {
        void onEdit(T item);
        void onDelete(T item);
    }
}