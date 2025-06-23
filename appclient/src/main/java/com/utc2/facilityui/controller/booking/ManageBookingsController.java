package com.utc2.facilityui.controller.booking;

import com.utc2.facilityui.model.BookedEquipmentItem;
import com.utc2.facilityui.model.RoomItem;
import com.utc2.facilityui.model.UserItem;
import com.utc2.facilityui.response.BookingResponse;
import com.utc2.facilityui.response.Page; // Client-side Page DTO
import com.utc2.facilityui.service.BookingService;
import com.utc2.facilityui.service.RoomService;
import com.utc2.facilityui.service.UserClientService;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos; // Import cho Pos.CENTER
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.StringConverter;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ManageBookingsController implements Initializable {

    @FXML private TableView<BookingResponse> bookingsTable;
    @FXML private TableColumn<BookingResponse, String> userIdColumn;
    @FXML private TableColumn<BookingResponse, String> roomIdColumn;
    @FXML private TableColumn<BookingResponse, String> purposeColumn;
    @FXML private TableColumn<BookingResponse, String> plannedTimeColumn;
    @FXML private TableColumn<BookingResponse, String> equipmentColumn;
    @FXML private TableColumn<BookingResponse, String> actualTimeColumn;
    @FXML private TableColumn<BookingResponse, String> statusColumn;
    @FXML private TableColumn<BookingResponse, String> approvedByColumn;
    @FXML private TableColumn<BookingResponse, String> cancellationReasonColumn;
    @FXML private TableColumn<BookingResponse, String> noteColumn;
    @FXML private TableColumn<BookingResponse, Void> actionColumn;

    @FXML private ComboBox<RoomItem> filterByRoomComboBox;
    @FXML private ComboBox<String> filterByMonthComboBox;
    @FXML private ComboBox<Integer> filterByYearComboBox;
    @FXML private ComboBox<UserItem> filterByUserComboBox;

    @FXML private Button resetButton;

    @FXML private ComboBox<Integer> rowsPerPageComboBox;
    @FXML private Label pageInfoLabel;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;

    private final BookingService bookingService = new BookingService();
    private final RoomService roomListService = new RoomService();
    private final UserClientService userListService = new UserClientService();

    private ObservableList<BookingResponse> currentTableData = FXCollections.observableArrayList();

    private int currentPageNumber = 0;
    private int currentPageSize = 10;
    private int totalPages = 0;
    private long totalElements = 0;

    private boolean initializingFilters = true;

    private Image tickImage;
    private Image cancelImage;
    private Image showInfoImage;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("ManageBookingsController: initialize()");

        try {
            InputStream tickStream = getClass().getResourceAsStream("/com/utc2/facilityui/images/approval.png");
            InputStream cancelStream = getClass().getResourceAsStream("/com/utc2/facilityui/images/showCancel.png");
            InputStream infoStream = getClass().getResourceAsStream("/com/utc2/facilityui/images/info.png");

            if (tickStream != null) tickImage = new Image(tickStream); else System.err.println("approval.png not found");
            if (cancelStream != null) cancelImage = new Image(cancelStream); else System.err.println("showCancel.png (for reject) not found");
            if (infoStream != null) showInfoImage = new Image(infoStream); else System.err.println("info.png not found");

        } catch (Exception e) {
            System.err.println("Error loading images for action buttons: " + e.getMessage());
            e.printStackTrace();
        }

        initTableColumns();
        initFilterControls();
        initializingFilters = false;

        if (resetButton != null) {
            resetButton.setOnAction(e -> handleResetFilters());
        }
        loadBookingsFromServer();
    }

    private void initTableColumns() {
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        roomIdColumn.setCellValueFactory(new PropertyValueFactory<>("roomName"));
        purposeColumn.setCellValueFactory(new PropertyValueFactory<>("purpose"));
        purposeColumn.setCellFactory(column -> createWrappingTextCell());

        plannedTimeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        formatDateTimeRange(cellData.getValue().getPlannedStartTime(), cellData.getValue().getPlannedEndTime())
                )
        );
        plannedTimeColumn.setCellFactory(column -> createWrappingTextCell());

        equipmentColumn.setCellValueFactory(cellData -> {
            List<BookedEquipmentItem> equipments = cellData.getValue().getBookedEquipments();
            String display = "Không có";
            if (equipments != null && !equipments.isEmpty()) {
                display = equipments.stream()
                        .map(BookedEquipmentItem::getEquipmentModelName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(", "));
                if (display.isEmpty()) display = "Không có";
            }
            return new SimpleStringProperty(display);
        });
        equipmentColumn.setCellFactory(column -> createWrappingTextCell());

        actualTimeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        formatSingleDateTime(cellData.getValue().getCreatedAt())
                )
        );
        actualTimeColumn.setCellFactory(column -> createWrappingTextCell());

        // Cấu hình cột Trạng thái với CellFactory tùy chỉnh
        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus()) // Trả về raw status
        );
        statusColumn.setCellFactory(column -> new TableCell<BookingResponse, String>() {
            private final Label statusLabel = new Label();

            @Override
            protected void updateItem(String rawStatus, boolean empty) {
                super.updateItem(rawStatus, empty);
                if (empty || rawStatus == null) {
                    setText(null);
                    setGraphic(null);
                    statusLabel.getStyleClass().removeIf(style -> style.startsWith("status-"));
                    statusLabel.getStyleClass().remove("status-badge");
                } else {
                    String translatedStatus = translateBookingStatus(rawStatus);
                    statusLabel.setText(translatedStatus);

                    statusLabel.getStyleClass().removeIf(style -> style.startsWith("status-"));
                    statusLabel.getStyleClass().remove("status-badge");
                    statusLabel.getStyleClass().add("status-badge");

                    switch (rawStatus.toUpperCase()) {
                        case "CONFIRMED":
                        case "APPROVED":
                            statusLabel.getStyleClass().add("status-confirmed");
                            break;
                        case "REJECTED":
                            statusLabel.getStyleClass().add("status-rejected");
                            break;
                        case "COMPLETED":
                            statusLabel.getStyleClass().add("status-completed");
                            break;
                        case "IN_PROGRESS":
                            statusLabel.getStyleClass().add("status-in-progress");
                            break;
                        case "CANCELLED":
                            statusLabel.getStyleClass().add("status-cancelled");
                            break;
                        case "PENDING_APPROVAL":
                            statusLabel.getStyleClass().add("status-pending");
                            break;
                        case "OVERDUE": // Thêm case cho trạng thái quá hạn
                            statusLabel.getStyleClass().add("status-overdue");
                            break;
                        default:
                            statusLabel.getStyleClass().add("status-default");
                            break;
                    }
                    setGraphic(statusLabel);
                    setText(null);
                }
            }
        });

        approvedByColumn.setCellValueFactory(new PropertyValueFactory<>("approvedByUserName"));
        cancellationReasonColumn.setCellValueFactory(new PropertyValueFactory<>("cancellationReason"));
        cancellationReasonColumn.setCellFactory(column -> createWrappingTextCell());
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));
        noteColumn.setCellFactory(column -> createWrappingTextCell());

        actionColumn.setCellFactory(createActionCellFactory());
    }

    private Callback<TableColumn<BookingResponse, Void>, TableCell<BookingResponse, Void>> createActionCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<BookingResponse, Void> call(final TableColumn<BookingResponse, Void> param) {
                return new TableCell<>() {
                    private final Button approveButton = new Button();
                    private final Button rejectButton = new Button();
                    private final Button showInfoButton = new Button();
                    private final HBox pane = new HBox(5);

                    {
                        pane.setAlignment(Pos.CENTER); // Căn giữa các nút trong HBox

                        String buttonStyle = "-fx-background-color: transparent; -fx-padding: 3; -fx-cursor: hand;";

                        if (tickImage != null) {
                            ImageView approveIcon = new ImageView(tickImage);
                            approveIcon.setFitHeight(20); approveIcon.setFitWidth(20);
                            approveButton.setGraphic(approveIcon);
                        } else { approveButton.setText("✓"); }
                        approveButton.setStyle(buttonStyle);
                        approveButton.setTooltip(new Tooltip("Duyệt đặt phòng"));

                        if (cancelImage != null) {
                            ImageView rejectIcon = new ImageView(cancelImage);
                            rejectIcon.setFitHeight(20); rejectIcon.setFitWidth(20);
                            rejectButton.setGraphic(rejectIcon);
                        } else { rejectButton.setText("X"); }
                        rejectButton.setStyle(buttonStyle);
                        rejectButton.setTooltip(new Tooltip("Từ chối đặt phòng"));

                        if (showInfoImage != null) {
                            ImageView infoIcon = new ImageView(showInfoImage);
                            infoIcon.setFitHeight(20); infoIcon.setFitWidth(20);
                            showInfoButton.setGraphic(infoIcon);
                        } else { showInfoButton.setText("i"); }
                        showInfoButton.setStyle(buttonStyle);
                        showInfoButton.setTooltip(new Tooltip("Xem chi tiết sự kiện"));

                        approveButton.setOnAction(event -> {
                            BookingResponse booking = getTableView().getItems().get(getIndex());
                            Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION,
                                    "Bạn có chắc chắn muốn duyệt yêu cầu đặt phòng này không?", ButtonType.YES, ButtonType.NO);
                            confirmationDialog.setTitle("Xác nhận duyệt");
                            confirmationDialog.setHeaderText("Duyệt yêu cầu đặt phòng cho phòng " + booking.getRoomName() + " bởi " + booking.getUserName());
                            confirmationDialog.initOwner(getWindow());
                            confirmationDialog.showAndWait().ifPresent(response -> {
                                if (response == ButtonType.YES) {
                                    new Thread(() -> {
                                        try {
                                            bookingService.approveBooking(booking.getId());
                                            Platform.runLater(() -> {
                                                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Yêu cầu đặt phòng đã được duyệt thành công.");
                                                loadBookingsFromServer();
                                            });
                                        } catch (IOException e) {
                                            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể duyệt yêu cầu đặt phòng: " + e.getMessage()));
                                            e.printStackTrace();
                                        }
                                    }).start();
                                }
                            });
                        });

                        rejectButton.setOnAction(event -> {
                            BookingResponse booking = getTableView().getItems().get(getIndex());
                            try {
                                URL cancelBookingUrl = getClass().getResource("/com/utc2/facilityui/form/cancelBooking.fxml");
                                if (cancelBookingUrl == null) {
                                    System.err.println("Lỗi: Không tìm thấy tệp /com/utc2/facilityui/form/cancelBooking.fxml");
                                    showAlert(Alert.AlertType.ERROR, "Lỗi Tệp", "Không tìm thấy tệp giao diện hủy đặt phòng.");
                                    return;
                                }
                                FXMLLoader loader = new FXMLLoader(cancelBookingUrl);
                                Parent root = loader.load();
                                CancelBookingController controller = loader.getController();
                                controller.initData(booking, bookingService);
                                Stage dialogStage = new Stage();
                                dialogStage.setTitle("Xác nhận Từ chối Đặt phòng");
                                dialogStage.initModality(Modality.WINDOW_MODAL);
                                dialogStage.initOwner(getWindow());
                                Scene scene = new Scene(root);
                                dialogStage.setScene(scene);
                                dialogStage.showAndWait();
                                loadBookingsFromServer();
                            } catch (IOException e) {
                                showAlert(Alert.AlertType.ERROR, "Lỗi Giao Diện", "Không thể mở cửa sổ từ chối đặt phòng: " + e.getMessage());
                                e.printStackTrace();
                            }
                        });

                        showInfoButton.setOnAction(event -> {
                            BookingResponse booking = getTableView().getItems().get(getIndex());
                            try {
                                URL infoBookingUrl = getClass().getResource("/com/utc2/facilityui/fxml/booking/InfoBooking.fxml");
                                if (infoBookingUrl == null) {
                                    System.err.println("Lỗi: Không tìm thấy tệp /com/utc2/facilityui/fxml/booking/InfoBooking.fxml");
                                    showAlert(Alert.AlertType.ERROR, "Lỗi Tệp", "Không tìm thấy tệp giao diện chi tiết đặt phòng.");
                                    return;
                                }
                                FXMLLoader loader = new FXMLLoader(infoBookingUrl);
                                Parent root = loader.load();
                                InfoBookingController controller = loader.getController();
                                controller.setBookingDetails(booking);
                                Stage dialogStage = new Stage();
                                dialogStage.setTitle("Chi tiết sự kiện Đặt phòng");
                                dialogStage.initModality(Modality.WINDOW_MODAL);
                                dialogStage.initOwner(getWindow());
                                Scene scene = new Scene(root);
                                dialogStage.setScene(scene);
                                dialogStage.showAndWait();
                            } catch (IOException e) {
                                showAlert(Alert.AlertType.ERROR, "Lỗi Giao Diện", "Không thể mở cửa sổ chi tiết đặt phòng: " + e.getMessage());
                                e.printStackTrace();
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            BookingResponse booking = getTableView().getItems().get(getIndex());
                            pane.getChildren().clear();
                            if ("PENDING_APPROVAL".equalsIgnoreCase(booking.getStatus())) {
                                pane.getChildren().addAll(approveButton, rejectButton);
                            } else {
                                pane.getChildren().add(showInfoButton);
                            }
                            setGraphic(pane);
                        }
                    }
                };
            }
        };
    }

    private TableCell<BookingResponse, String> createWrappingTextCell() {
        return new TableCell<>() {
            private final Text text = new Text();
            {
                text.setStyle("-fx-font-size: 12px;");
                text.wrappingWidthProperty().bind(this.widthProperty().subtract(10));
                setGraphic(text);
                setPrefHeight(Control.USE_COMPUTED_SIZE);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                text.setText((empty || item == null) ? null : item);
            }
        };
    }

    private void initFilterControls() {
        initializingFilters = true;
        rowsPerPageComboBox.setItems(FXCollections.observableArrayList(5, 10, 20, 50, 100));
        rowsPerPageComboBox.setValue(currentPageSize);
        rowsPerPageComboBox.setOnAction(e -> { if (!initializingFilters) handleRowsPerPageChange(e); });

        ObservableList<String> monthItems = FXCollections.observableArrayList("Tất cả tháng");
        for (int i = 1; i <= 12; i++) monthItems.add("Tháng " + i);
        filterByMonthComboBox.setItems(monthItems);
        filterByMonthComboBox.getSelectionModel().selectFirst();
        filterByMonthComboBox.setOnAction(e -> { if (!initializingFilters) { currentPageNumber = 0; loadBookingsFromServer(); } });

        ObservableList<Integer> yearItems = FXCollections.observableArrayList();
        yearItems.add(null);
        int currentSystemYear = java.time.Year.now().getValue();
        for (int i = currentSystemYear + 1; i >= currentSystemYear - 5; i--) yearItems.add(i);
        filterByYearComboBox.setItems(yearItems);
        filterByYearComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Integer year) { return year == null ? "Tất cả năm" : year.toString(); }
            @Override public Integer fromString(String string) { return (string == null || string.isEmpty() || string.equals("Tất cả năm")) ? null : Integer.parseInt(string); }
        });
        filterByYearComboBox.getSelectionModel().select(null);
        filterByYearComboBox.setOnAction(e -> { if (!initializingFilters) { currentPageNumber = 0; loadBookingsFromServer(); } });

        final RoomItem allRoomsPlaceholder = new RoomItem(null, "Tất cả phòng");
        filterByRoomComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(RoomItem room) { return room == null ? "Tất cả phòng" : room.getName(); }
            @Override public RoomItem fromString(String string) { return null; }
        });
        filterByRoomComboBox.setPlaceholder(new Label("Đang tải phòng..."));
        filterByRoomComboBox.getItems().add(allRoomsPlaceholder);
        filterByRoomComboBox.getSelectionModel().selectFirst();
        new Thread(() -> {
            try {
                List<RoomItem> rooms = roomListService.getAllRoomsForFilter();
                Platform.runLater(() -> {
                    filterByRoomComboBox.getItems().addAll(rooms);
                    if (rooms.isEmpty() && filterByRoomComboBox.getItems().size() == 1) filterByRoomComboBox.setPlaceholder(new Label("Không có phòng"));
                });
            } catch (IOException e) { Platform.runLater(() -> filterByRoomComboBox.setPlaceholder(new Label("Lỗi tải phòng"))); e.printStackTrace(); }
        }).start();
        filterByRoomComboBox.setOnAction(e -> { if (!initializingFilters) { currentPageNumber = 0; loadBookingsFromServer(); } });

        final UserItem allUsersPlaceholder = new UserItem(null, "Tất cả người dùng", null);
        filterByUserComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(UserItem user) { return user == null ? "Tất cả người dùng" : user.getDisplayName(); }
            @Override public UserItem fromString(String string) { return null; }
        });
        filterByUserComboBox.setPlaceholder(new Label("Đang tải người dùng..."));
        filterByUserComboBox.getItems().add(allUsersPlaceholder);
        filterByUserComboBox.getSelectionModel().selectFirst();
        new Thread(() -> {
            try {
                List<UserItem> users = userListService.getAllUsersForFilter();
                Platform.runLater(() -> {
                    filterByUserComboBox.getItems().addAll(users);
                    if (users.isEmpty() && filterByUserComboBox.getItems().size() == 1) filterByUserComboBox.setPlaceholder(new Label("Không có người dùng"));
                });
            } catch (IOException e) { Platform.runLater(() -> filterByUserComboBox.setPlaceholder(new Label("Lỗi tải người dùng"))); e.printStackTrace(); }
        }).start();
        filterByUserComboBox.setOnAction(e -> { if (!initializingFilters) { currentPageNumber = 0; loadBookingsFromServer(); } });

        prevPageButton.setOnAction(e -> handlePreviousPage());
        nextPageButton.setOnAction(e -> handleNextPage());
    }

    @FXML
    private void handleResetFilters() {
        initializingFilters = true;
        if (filterByRoomComboBox.getItems() != null && !filterByRoomComboBox.getItems().isEmpty()) filterByRoomComboBox.getSelectionModel().selectFirst();
        if (filterByMonthComboBox.getItems() != null && !filterByMonthComboBox.getItems().isEmpty()) filterByMonthComboBox.getSelectionModel().selectFirst();
        if (filterByYearComboBox.getItems() != null && !filterByYearComboBox.getItems().isEmpty()) filterByYearComboBox.getSelectionModel().select(null);
        if (filterByUserComboBox.getItems() != null && !filterByUserComboBox.getItems().isEmpty()) filterByUserComboBox.getSelectionModel().selectFirst();
        initializingFilters = false;
        currentPageNumber = 0;
        loadBookingsFromServer();
    }

    private void loadBookingsFromServer() {
        if (initializingFilters) return;
        RoomItem selectedRoom = filterByRoomComboBox.getSelectionModel().getSelectedItem();
        final String finalRoomId = (selectedRoom != null && selectedRoom.getId() != null) ? selectedRoom.getId() : null;
        String selectedMonthStr = filterByMonthComboBox.getValue();
        Integer tempSelectedMonth = null;
        if (selectedMonthStr != null && !selectedMonthStr.equals("Tất cả tháng")) {
            try { tempSelectedMonth = Integer.parseInt(selectedMonthStr.replace("Tháng ", "")); } catch (NumberFormatException e) { System.err.println("Lỗi parse tháng: " + selectedMonthStr); }
        }
        final Integer finalMonth = tempSelectedMonth;
        final Integer finalYear = filterByYearComboBox.getValue();
        UserItem selectedUser = filterByUserComboBox.getSelectionModel().getSelectedItem();
        final String finalUserId = (selectedUser != null && selectedUser.getId() != null) ? selectedUser.getId() : null;

        bookingsTable.setPlaceholder(new Label("Đang tải danh sách đặt phòng..."));
        new Thread(() -> {
            try {
                Page<BookingResponse> pagedResponse = bookingService.getAllBookings(finalRoomId, finalMonth, finalYear, finalUserId, currentPageNumber, currentPageSize);
                Platform.runLater(() -> {
                    if (pagedResponse != null) {
                        List<BookingResponse> content = pagedResponse.getContent() != null ? pagedResponse.getContent() : Collections.emptyList();
                        currentTableData.setAll(content);
                        bookingsTable.setItems(currentTableData);
                        this.currentPageNumber = pagedResponse.getNumber();
                        this.totalElements = pagedResponse.getTotalElements();
                        this.totalPages = pagedResponse.getTotalPages();
                        updatePaginationUI();
                        bookingsTable.setPlaceholder(new Label(content.isEmpty() ? "Không có đặt phòng nào phù hợp với tiêu chí." : null));
                    } else {
                        currentTableData.clear(); bookingsTable.setItems(currentTableData);
                        bookingsTable.setPlaceholder(new Label("Không nhận được phản hồi từ server."));
                        this.totalPages = 0; this.totalElements = 0; this.currentPageNumber = 0;
                        updatePaginationUI();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR,"Lỗi Tải Dữ Liệu", "Không thể tải danh sách đặt phòng: " + e.getMessage());
                    currentTableData.clear(); bookingsTable.setItems(currentTableData);
                    bookingsTable.setPlaceholder(new Label("Lỗi tải dữ liệu."));
                    this.totalPages = 0; this.totalElements = 0; this.currentPageNumber = 0;
                    updatePaginationUI();
                });
            }
        }).start();
    }

    private void updatePaginationUI() {
        if (pageInfoLabel != null) {
            pageInfoLabel.setText((totalElements == 0 && currentTableData.isEmpty()) ? "Không có dữ liệu" :
                    String.format("Trang %d/%d (Tổng: %d)", currentPageNumber + 1, totalPages > 0 ? totalPages : 1, totalElements));
        }
        boolean noData = totalElements == 0;
        if (prevPageButton != null) prevPageButton.setDisable(currentPageNumber == 0 || noData);
        if (nextPageButton != null) nextPageButton.setDisable(currentPageNumber >= totalPages - 1 || noData);
        if (rowsPerPageComboBox != null) rowsPerPageComboBox.setDisable(noData);
    }

    @FXML private void handlePreviousPage() { if (currentPageNumber > 0) { currentPageNumber--; loadBookingsFromServer(); } }
    @FXML private void handleNextPage() { if (currentPageNumber < totalPages - 1) { currentPageNumber++; loadBookingsFromServer(); } }
    @FXML private void handleRowsPerPageChange(ActionEvent event) {
        Integer selected = rowsPerPageComboBox.getValue();
        if (selected != null && selected != currentPageSize) {
            currentPageSize = selected; currentPageNumber = 0; loadBookingsFromServer();
        }
    }

    private String formatDateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return "N/A";
        Locale vietnameseLocale = new Locale("vi", "VN");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEE, dd/MM/yyyy", vietnameseLocale);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", vietnameseLocale);
        if (start.toLocalDate().equals(end.toLocalDate())) {
            return String.format("%s (%s - %s)", start.format(dateFormatter), start.format(timeFormatter), end.format(timeFormatter));
        } else {
            return String.format("%s %s - %s %s", start.format(dateFormatter), start.format(timeFormatter), end.format(dateFormatter), end.format(timeFormatter));
        }
    }

    private String formatSingleDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy", new Locale("vi", "VN")));
    }

    private String translateBookingStatus(String status) {
        if (status == null) return "N/A";
        return switch (status.toUpperCase()) {
            case "COMPLETED" -> "Đã hoàn thành";
            case "PENDING_APPROVAL" -> "Chờ duyệt";
            case "CANCELLED" -> "Đã hủy";
            case "REJECTED" -> "Đã từ chối";
            case "CONFIRMED", "APPROVED" -> "Đã duyệt";
            case "IN_PROGRESS" -> "Đang sử dụng";
            case "OVERDUE" -> "Quá hạn";
            default -> status;
        };
    }

    private String safeText(String value) { return value != null ? value : ""; }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        if (message.length() > 100) {
            TextArea textArea = new TextArea(message);
            textArea.setEditable(false); textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE); textArea.setMaxHeight(Double.MAX_VALUE);
            alert.getDialogPane().setContent(textArea);
            alert.setResizable(true);
        } else { alert.setContentText(message); }
        alert.initOwner(getWindow());
        alert.showAndWait();
    }

    private Window getWindow() {
        if (bookingsTable != null && bookingsTable.getScene() != null && bookingsTable.getScene().getWindow() != null) {
            return bookingsTable.getScene().getWindow();
        }
        if (resetButton != null && resetButton.getScene() != null && resetButton.getScene().getWindow() != null) {
            return resetButton.getScene().getWindow();
        }
        return null;
    }

    @FXML
    private void handleExportBookingsPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu file PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf"));
        File file = fileChooser.showSaveDialog(getWindow());
        if (file != null) {
            try (InputStream fontStream = getClass().getResourceAsStream("/com/utc2/facilityui/fonts/Roboto-Regular.ttf")) {
                if (fontStream == null) throw new IOException("Không tìm thấy file font Roboto-Regular.ttf");
                byte[] fontBytes = fontStream.readAllBytes();
                PdfFont unicodeFont = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, true);
                PdfWriter writer = new PdfWriter(file);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);
                document.setFont(unicodeFont);
                document.add(new Paragraph("DANH SÁCH ĐẶT PHÒNG").setBold().setFontSize(16).setTextAlignment(TextAlignment.CENTER).setMarginBottom(15));
                float[] columnWidths = {2.5f, 1.8f, 3f, 3.8f, 3f, 2.8f, 2.2f, 2.5f, 2.5f, 3f};
                Table pdfTable = new Table(UnitValue.createPercentArray(columnWidths)).setWidth(UnitValue.createPercentValue(100));
                String[] headers = {"Người đặt", "Phòng", "Mục đích", "Thời gian dự kiến", "Thiết bị", "Yêu cầu lúc", "Trạng thái", "Người xử lý", "Lý do hủy", "Ghi chú"};
                for (String header : headers) pdfTable.addHeaderCell(new Cell().add(new Paragraph(header).setBold().setFontSize(10)));
                currentTableData.forEach(booking -> {
                    pdfTable.addCell(new Cell().add(new Paragraph(safeText(booking.getUserName())).setFontSize(9)));
                    pdfTable.addCell(new Cell().add(new Paragraph(safeText(booking.getRoomName())).setFontSize(9)));
                    pdfTable.addCell(new Cell().add(new Paragraph(safeText(booking.getPurpose())).setFontSize(9)));
                    pdfTable.addCell(new Cell().add(new Paragraph(safeText(formatDateTimeRange(booking.getPlannedStartTime(), booking.getPlannedEndTime()))).setFontSize(9)));
                    String equipmentDisplay = "Không có";
                    if (booking.getBookedEquipments() != null && !booking.getBookedEquipments().isEmpty()) {
                        equipmentDisplay = booking.getBookedEquipments().stream().map(BookedEquipmentItem::getEquipmentModelName).filter(Objects::nonNull).collect(Collectors.joining(", "));
                        if (equipmentDisplay.isEmpty()) equipmentDisplay = "Không có";
                    }
                    pdfTable.addCell(new Cell().add(new Paragraph(safeText(equipmentDisplay)).setFontSize(9)));
                    pdfTable.addCell(new Cell().add(new Paragraph(safeText(formatSingleDateTime(booking.getCreatedAt()))).setFontSize(9)));
                    pdfTable.addCell(new Cell().add(new Paragraph(safeText(translateBookingStatus(booking.getStatus()))).setFontSize(9)));
                    pdfTable.addCell(new Cell().add(new Paragraph(safeText(booking.getApprovedByUserName())).setFontSize(9)));
                    pdfTable.addCell(new Cell().add(new Paragraph(safeText(booking.getCancellationReason())).setFontSize(9)));
                    pdfTable.addCell(new Cell().add(new Paragraph(safeText(booking.getNote())).setFontSize(9)));
                });
                document.add(pdfTable);
                document.close();
                showAlert(Alert.AlertType.INFORMATION, "Thành Công", "Xuất file PDF thành công!");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi Xuất PDF", "Đã xảy ra lỗi khi xuất PDF: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExportBookingsExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu file Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx"));
        File file = fileChooser.showSaveDialog(getWindow());
        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Bookings");
                String[] headers = {"Người đặt", "Phòng", "Mục đích", "Bắt đầu dự kiến", "Kết thúc dự kiến", "Thiết bị", "Yêu cầu lúc", "Trạng thái", "Người xử lý", "Lý do hủy", "Ghi chú"};
                Row headerRowExcel = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) headerRowExcel.createCell(i).setCellValue(headers[i]);
                DateTimeFormatter excelDateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                int rowIndex = 1;
                for (BookingResponse booking : currentTableData) {
                    Row rowData = sheet.createRow(rowIndex++);
                    rowData.createCell(0).setCellValue(safeText(booking.getUserName()));
                    rowData.createCell(1).setCellValue(safeText(booking.getRoomName()));
                    rowData.createCell(2).setCellValue(safeText(booking.getPurpose()));
                    rowData.createCell(3).setCellValue(booking.getPlannedStartTime() != null ? booking.getPlannedStartTime().format(excelDateTimeFormatter) : "");
                    rowData.createCell(4).setCellValue(booking.getPlannedEndTime() != null ? booking.getPlannedEndTime().format(excelDateTimeFormatter) : "");
                    String equipmentDisplay = "";
                    if (booking.getBookedEquipments() != null && !booking.getBookedEquipments().isEmpty()) {
                        equipmentDisplay = booking.getBookedEquipments().stream().map(BookedEquipmentItem::getEquipmentModelName).filter(Objects::nonNull).collect(Collectors.joining(", "));
                    }
                    rowData.createCell(5).setCellValue(safeText(equipmentDisplay.isEmpty() ? "Không có" : equipmentDisplay));
                    rowData.createCell(6).setCellValue(booking.getCreatedAt() != null ? booking.getCreatedAt().format(excelDateTimeFormatter) : "");
                    rowData.createCell(7).setCellValue(safeText(translateBookingStatus(booking.getStatus())));
                    rowData.createCell(8).setCellValue(safeText(booking.getApprovedByUserName() != null ? booking.getApprovedByUserName() : booking.getCancelledByUserName()));
                    rowData.createCell(9).setCellValue(safeText(booking.getCancellationReason()));
                    rowData.createCell(10).setCellValue(safeText(booking.getNote()));
                }
                for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }
                showAlert(Alert.AlertType.INFORMATION,"Thành Công", "Xuất file Excel thành công!");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR,"Lỗi Xuất Excel", "Đã xảy ra lỗi khi xuất Excel: " + e.getMessage());
            }
        }
    }
}