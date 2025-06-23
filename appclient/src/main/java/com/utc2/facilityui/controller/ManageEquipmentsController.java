package com.utc2.facilityui.controller;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.UnitValue;
import com.utc2.facilityui.controller.equipment.EditEquipmentController;
import com.utc2.facilityui.controller.equipment.ViewEquipmentController;
import com.utc2.facilityui.model.Equipment;
import com.itextpdf.layout.element.Cell;
import com.utc2.facilityui.service.EquipmentService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ManageEquipmentsController {

    @FXML private TableView<Equipment> equipmentTable;

    @FXML private TableColumn<Equipment, String> colName;
    @FXML private TableColumn<Equipment, String> colModel;
    @FXML private TableColumn<Equipment, String> colRoom;
    @FXML private TableColumn<Equipment, String> colNotes;
    @FXML private TableColumn<Equipment, String> colStatus;
    @FXML private TableColumn<Equipment, String> colSerial;
    @FXML private TableColumn<Equipment, Void> colActions;
    @FXML private TableColumn<Equipment, String> colImage;
    @FXML private TableColumn<Equipment, String> colDescription;


    @FXML private ComboBox<String> filterByNameComboBox;
    @FXML private ComboBox<String> filterByRoomComboBox;
    @FXML private ComboBox<String> filterByModelComboBox;

    @FXML private ComboBox<Integer> rowsPerPageComboBox;
    @FXML private Label pageInfoLabel;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Button resetButton;


    private List<Equipment> allEquipments = new ArrayList<>();
    private List<Equipment> filteredEquipments = new ArrayList<>();
    private EquipmentService service = new EquipmentService();


    private int currentPage = 1;
    private int rowsPerPage = 10;

    public void initialize() {
        setupTableColumns();

        rowsPerPageComboBox.getItems().addAll(5, 10, 15, 20, 50);
        rowsPerPageComboBox.setValue(10);
        rowsPerPageComboBox.setOnAction(e -> {
            currentPage = 1;
            updatePagination();
        });

        prevPageButton.setOnAction(e -> handlePreviousPage());
        nextPageButton.setOnAction(e -> handleNextPage());

        filterByNameComboBox.setOnAction(e -> applyFilters());
        filterByRoomComboBox.setOnAction(e -> applyFilters());
        filterByModelComboBox.setOnAction(e -> applyFilters());

        resetButton.setOnAction(e -> handleReset());

        loadEquipments();
    }

    private void setupTableColumns() {
        colName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTypeName()));
        colModel.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getModelName()));
        colRoom.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDefaultRoomName()));
        colNotes.setCellFactory(tc -> {
            TableCell<Equipment, String> cell = new TableCell<>() {
                private final Label label = new Label();
                {
                    label.setWrapText(true);
                    label.setTextAlignment(TextAlignment.CENTER);
                    label.setAlignment(Pos.CENTER);
                    label.setMaxWidth(Double.MAX_VALUE); // để label có thể mở rộng theo cell
                    setGraphic(label);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setAlignment(Pos.CENTER); // Căn giữa cell theo chiều dọc            // Căn giữa container trong cell
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        label.setText("");
                    } else {
                        label.setText(item);
                    }
                }
            };
            return cell;
        });
        colNotes.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNotes()));
        colStatus.setCellValueFactory(cell -> {
            String status = cell.getValue().getStatus();
            String vietnameseStatus;
            switch (status) {
                case "AVAILABLE" -> vietnameseStatus = "Có sẵn";
                case "UNDER_MAINTENANCE" -> vietnameseStatus = "Đang bảo trì";
                case "BROKEN" -> vietnameseStatus = "Bị hỏng";
                case "DISPOSED" -> vietnameseStatus = "Đã thanh lý";
                default -> vietnameseStatus = status; // Trường hợp không khớp
            }
            return new SimpleStringProperty(vietnameseStatus);
        });
        colSerial.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSerialNumber()));
        colDescription.setCellFactory(tc -> {
            TableCell<Equipment, String> cell = new TableCell<>() {
                private final Label label = new Label();

                {
                    label.setWrapText(true);
                    label.setTextAlignment(TextAlignment.CENTER);
                    label.setAlignment(Pos.CENTER);
                    label.setMaxWidth(Double.MAX_VALUE); // để label có thể mở rộng theo cell
                    setGraphic(label);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setAlignment(Pos.CENTER); // Căn giữa cell theo chiều dọc
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        label.setText("");
                    } else {
                        label.setText(item);
                    }
                }
            };
            return cell;
        });
        colDescription.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDescription()));
        colImage.setCellFactory(tc -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(100);
                imageView.setFitHeight(90);
                imageView.setPreserveRatio(true);
                setGraphic(imageView);
            }

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null || imagePath.isEmpty()) {
                    imageView.setImage(null);
                } else {
                    // Ví dụ: imgModel = "images/models/dell_lat5430.jpg"
                    String effectivePath = "/com/utc2/facilityui/images/models/" + Paths.get(imagePath).getFileName();
                    InputStream is = getClass().getResourceAsStream(effectivePath);
                    if (is != null) {
                        imageView.setImage(new Image(is));
                    } else {
                        System.err.println("❌ Không tìm thấy ảnh: " + effectivePath);
                        imageView.setImage(null);
                    }
                }
            }
        });
        colImage.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getImgModel()));
        colActions.setCellFactory(param -> new TableCell<>() {
            private final MenuButton menuButton = new MenuButton();

            {
                menuButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.NAVICON)); // hoặc .ELLIPSIS_V

                MenuItem viewItem = new MenuItem("Xem");
                MenuItem editItem = new MenuItem("Sửa");
                MenuItem deleteItem = new MenuItem("Xóa");

                viewItem.setOnAction(e -> {
                    Equipment equipment = getTableView().getItems().get(getIndex());
                    handleViewEquipment(equipment);
                });

                editItem.setOnAction(e -> {
                    Equipment equipment = getTableView().getItems().get(getIndex());
                    handleEditEquipment(equipment);
                });

                deleteItem.setOnAction(e -> {
                    Equipment equipment = getTableView().getItems().get(getIndex());
                    handleDeleteEquipment(equipment);
                });

                menuButton.getItems().addAll(viewItem, editItem, deleteItem);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(menuButton);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        colActions.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(null));
    }

    private void handleViewEquipment(Equipment equipment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/utc2/facilityui/view/viewEquipment.fxml"));
            Parent root = loader.load();

            ViewEquipmentController controller = loader.getController();
            controller.setEquipment(equipment); // ✅ truyền dữ liệu

            Stage stage = new Stage();
            stage.setTitle("Chi tiết thiết bị");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleEditEquipment(Equipment equipment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/utc2/facilityui/view/editEquipment.fxml"));
            Parent root = loader.load();

            EditEquipmentController controller = loader.getController();
            controller.setEquipmentToEdit(equipment);

            Stage stage = new Stage();
            stage.setTitle("Sửa thiết bị");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // ✅ Kiểm tra nếu thiết bị đã được cập nhật
            if ("updated".equals(stage.getUserData())) {
                loadEquipments(); // Gọi lại để làm mới bảng
                equipmentTable.refresh();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteEquipment(Equipment equipment) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa thiết bị: " + equipment.getTypeName() +
                " có Serial: " + equipment.getSerialNumber() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                boolean deleted = service.deleteEquipmentByIdAndSerial(
                        equipment.getId(), equipment.getSerialNumber());

                Platform.runLater(() -> {
                    if (deleted) {
                        showAlert(Alert.AlertType.INFORMATION, "Đã xóa thiết bị thành công.");
                        loadEquipments(); // reload danh sách nếu cần
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Không thể xóa thiết bị.");
                    }
                });
            }).start();
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadEquipments() {
        EquipmentService service = new EquipmentService();
        new Thread(() -> {
            try {
                List<Equipment> list = service.getAllEquipments();

                // ✅ Sắp xếp theo createdAt giảm dần (mới nhất lên đầu)
                list.sort((e1, e2) -> {
                    LocalDateTime created1 = parseDateTime(e1.getCreatedAt());
                    LocalDateTime created2 = parseDateTime(e2.getCreatedAt());
                    return created2.compareTo(created1); // mới nhất lên trước
                });

                for (Equipment eq : list) {
                    String description = getDescriptionFromDB(eq.getModelName());
                    eq.setDescription(description != null ? description : "Không có mô tả.");
                }

                Platform.runLater(() -> {
                    allEquipments = list;
                    filteredEquipments = new ArrayList<>(list);
                    initFilterComboBoxes(allEquipments);
                    currentPage = 1;
                    updatePagination();
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ✅ Hàm parse created_at từ chuỗi
    private LocalDateTime parseDateTime(String datetime) {
        try {
            return LocalDateTime.parse(datetime); // hoặc dùng định dạng nếu cần
        } catch (Exception e) {
            return LocalDateTime.MIN;
        }
    }



    private String getDescriptionFromDB(String modelName) {
        String description = null;
        String sql = "SELECT description FROM equipment_models WHERE name = ?";
        String url = "jdbc:mysql://localhost:3306/facility?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "Tranbien2809@";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection conn = DriverManager.getConnection(url, user, password);
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, modelName);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    description = rs.getString("description");
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("❌ Lỗi khi truy vấn mô tả thiết bị: " + e.getMessage());
        }
        return description;
    }

    private void initFilterComboBoxes(List<Equipment> data) {
        filterByNameComboBox.getItems().clear();
        filterByRoomComboBox.getItems().clear();
        filterByModelComboBox.getItems().clear();

        filterByNameComboBox.getItems().add("Tất cả");
        filterByRoomComboBox.getItems().add("Tất cả");
        filterByModelComboBox.getItems().add("Tất cả");

        Set<String> names = data.stream().map(Equipment::getTypeName).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<String> rooms = data.stream().map(Equipment::getDefaultRoomName).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<String> models = data.stream().map(Equipment::getModelName).filter(Objects::nonNull).collect(Collectors.toSet());

        filterByNameComboBox.getItems().addAll(names);
        filterByRoomComboBox.getItems().addAll(rooms);
        filterByModelComboBox.getItems().addAll(models);

        filterByNameComboBox.setValue("Tất cả");
        filterByRoomComboBox.setValue("Tất cả");
        filterByModelComboBox.setValue("Tất cả");
    }

    private void applyFilters() {
        String selectedName = filterByNameComboBox.getValue();
        String selectedRoom = filterByRoomComboBox.getValue();
        String selectedModel = filterByModelComboBox.getValue();

        filteredEquipments = allEquipments.stream()
                .filter(e -> selectedName == null || selectedName.equals("Tất cả") || e.getTypeName().equals(selectedName))
                .filter(e -> selectedRoom == null || selectedRoom.equals("Tất cả") || e.getDefaultRoomName().equals(selectedRoom))
                .filter(e -> selectedModel == null || selectedModel.equals("Tất cả") || e.getModelName().equals(selectedModel))
                .toList();

        currentPage = 1;
        updatePagination();
    }

    private void updatePagination() {
        rowsPerPage = rowsPerPageComboBox.getValue();
        int totalItems = filteredEquipments.size();
        int fromIndex = (currentPage - 1) * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, totalItems);

        if (fromIndex >= totalItems || totalItems == 0) {
            equipmentTable.getItems().clear();
            pageInfoLabel.setText("0-0 of 0");
            return;
        }

        List<Equipment> currentPageItems = filteredEquipments.subList(fromIndex, toIndex);
        equipmentTable.getItems().setAll(currentPageItems);
        pageInfoLabel.setText((fromIndex + 1) + "-" + toIndex + " of " + totalItems);

        prevPageButton.setDisable(currentPage == 1);
        nextPageButton.setDisable(toIndex >= totalItems);
    }

    @FXML
    private void handlePreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            updatePagination();
        }
    }

    @FXML
    private void handleNextPage() {
        int totalItems = filteredEquipments.size();
        int maxPage = (int) Math.ceil((double) totalItems / rowsPerPage);
        if (currentPage < maxPage) {
            currentPage++;
            updatePagination();
        }
    }

    public void handleRowsPerPageChange(ActionEvent actionEvent) {
        currentPage = 1;
        rowsPerPage = rowsPerPageComboBox.getValue();
        updatePagination();
    }

    @FXML
    private void handleReset() {
        filterByNameComboBox.setValue("Tất cả");
        filterByRoomComboBox.setValue("Tất cả");
        filterByModelComboBox.setValue("Tất cả");
        filteredEquipments = new ArrayList<>(allEquipments);
        currentPage = 1;
        updatePagination();
    }

    @FXML
    public void handleAddEquipment(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/utc2/facilityui/view/addEquipment.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Thêm thiết bị mới");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // chặn cửa sổ cha nếu muốn
            stage.showAndWait();

            // Kiểm tra nếu có dữ liệu mới được thêm thì reload
            if ("created".equals(stage.getUserData())) {
                loadEquipments(); // giả sử đây là phương thức bạn dùng để reload dữ liệu
            }

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Không thể mở cửa sổ thêm thiết bị:\n" + e.getMessage()).showAndWait();
        }
    }

    private String safeText(String s) {
        return s == null ? "" : s;
    }


    public void handleExportBookingsPDF(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu file PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf"));
        File file = fileChooser.showSaveDialog(equipmentTable.getScene().getWindow());

        if (file != null) {
            try {
                // 1. Tạo writer và document
                PdfWriter writer = new PdfWriter(file);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                // 2. Load font Unicode
                InputStream fontStream = getClass().getResourceAsStream("/com/utc2/facilityui/fonts/Roboto-Regular.ttf");
                byte[] fontBytes = fontStream.readAllBytes();
                PdfFont unicodeFont = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, true);
                document.setFont(unicodeFont);

                // 3. Tiêu đề
                Paragraph title = new Paragraph("DANH SÁCH THIẾT BỊ")
                        .setFont(unicodeFont)
                        .setBold()
                        .setFontSize(16)
                        .setTextAlignment(com.itextpdf.layout.property.TextAlignment.CENTER)
                        .setMarginBottom(15);
                document.add(title);

                // 4. Cấu hình bảng
                float[] columnWidths = {3, 3, 2, 2, 2, 3, 4};
                Table table = new Table(columnWidths).setWidth(UnitValue.createPercentValue(100));

                String[] headers = {
                        "Tên thiết bị", "Model", "Phòng", "Trạng thái",
                        "Serial", "Ghi chú", "Mô tả"
                };

                for (String header : headers) {
                    table.addHeaderCell(
                            new Cell().add(new Paragraph(header).setFont(unicodeFont).setBold())
                    );
                }

                // 5. Lấy dữ liệu từ TableView
                for (Equipment equipment : equipmentTable.getItems()) {
                    table.addCell(new Cell().add(new Paragraph(safeText(equipment.getTypeName())).setFont(unicodeFont)));
                    table.addCell(new Cell().add(new Paragraph(safeText(equipment.getModelName())).setFont(unicodeFont)));
                    table.addCell(new Cell().add(new Paragraph(safeText(equipment.getDefaultRoomName())).setFont(unicodeFont)));
                    table.addCell(new Cell().add(new Paragraph(safeText(equipment.getStatus())).setFont(unicodeFont)));
                    table.addCell(new Cell().add(new Paragraph(safeText(equipment.getSerialNumber())).setFont(unicodeFont)));
                    table.addCell(new Cell().add(new Paragraph(safeText(equipment.getNotes())).setFont(unicodeFont)));
                    table.addCell(new Cell().add(new Paragraph(safeText(equipment.getDescription())).setFont(unicodeFont)));
                }

                document.add(table);
                document.close();

                new Alert(Alert.AlertType.INFORMATION, "Xuất PDF thành công!").showAndWait();

            } catch (Exception e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Lỗi khi xuất PDF: " + e.getMessage()).showAndWait();
            }
        }
    }

}
