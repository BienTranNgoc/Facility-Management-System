package com.utc2.facilityui.controller;

import com.utc2.facilityui.response.MaintenanceResponse;
import com.utc2.facilityui.response.Page; // Client-side Page
import com.utc2.facilityui.service.MaintenanceApiService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MaintenanceController implements Initializable {

    @FXML
    private TableView<MaintenanceResponse> bookingsTable;
    @FXML
    private TableColumn<MaintenanceResponse, String> roomName;
    @FXML
    private TableColumn<MaintenanceResponse, String> modelName;
    @FXML
    private TableColumn<MaintenanceResponse, String> description;
    @FXML
    private TableColumn<MaintenanceResponse, String> status;
    @FXML
    private TableColumn<MaintenanceResponse, String> reportByUser;
    @FXML
    private TableColumn<MaintenanceResponse, String> reportedAt;
    @FXML
    private TableColumn<MaintenanceResponse, String> technicianName;
    @FXML
    private TableColumn<MaintenanceResponse, Void> actionColumn;

    @FXML
    private CheckBox statusReported;
    @FXML
    private CheckBox statusIn_Progress;
    @FXML
    private CheckBox statusCompleted;
    @FXML
    private CheckBox statusCannot_Repair;
    @FXML
    private CheckBox statusCancelled;

    @FXML
    private ComboBox<Integer> rowsPerPageComboBox;
    @FXML
    private Label pageInfoLabel;
    @FXML
    private Button prevPageButton;
    @FXML
    private Button nextPageButton;

    @FXML
    private ProgressIndicator loadingIndicator;

    private ObservableList<MaintenanceResponse> maintenanceList = FXCollections.observableArrayList();
    private MaintenanceApiService maintenanceApiService;

    private int currentPage = 0;
    private int pageSize = 10;
    private int totalPages = 0;
    private long totalElements = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("[Controller] initialize - Called.");
        this.maintenanceApiService = new MaintenanceApiService();

        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        } else {
            System.err.println("[Controller] initialize - FXML Error: loadingIndicator is null. Check fx:id.");
        }

        // Cấu hình các cột
        roomName.setCellValueFactory(new PropertyValueFactory<>("roomName"));
        modelName.setCellValueFactory(new PropertyValueFactory<>("modelName"));
        description.setCellValueFactory(new PropertyValueFactory<>("description"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));
        reportByUser.setCellValueFactory(new PropertyValueFactory<>("reportByUser"));
        reportedAt.setCellValueFactory(new PropertyValueFactory<>("reportDate"));
        technicianName.setCellValueFactory(new PropertyValueFactory<>("technicianName"));

        setupActionColumn();
        bookingsTable.setItems(maintenanceList);
        setupFiltersAndPagination();

        Integer initialPageSize = rowsPerPageComboBox.getValue();
        if (initialPageSize != null) {
            pageSize = initialPageSize;
        }
        System.out.println("[Controller] initialize - Initial pageSize: " + pageSize);
        loadMaintenanceData(currentPage, pageSize, getSelectedStatusFilters());
    }

    private void setupActionColumn() {
        // ... (Giữ nguyên như cũ) ...
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("Xem");
            private final Button updateButton = new Button("Sửa");
            private final HBox pane = new HBox(5, viewButton, updateButton);

            {
                pane.setAlignment(Pos.CENTER);
                viewButton.setOnAction(event -> {
                    MaintenanceResponse maintenance = getTableView().getItems().get(getIndex());
                    if (maintenance != null) {
                        System.out.println("[Controller] Action: View clicked for ID: " + maintenance.getId());
                    }
                });
                updateButton.setOnAction(event -> {
                    MaintenanceResponse maintenance = getTableView().getItems().get(getIndex());
                    if (maintenance != null) {
                        System.out.println("[Controller] Action: Update clicked for ID: " + maintenance.getId());
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadMaintenanceData(int page, int size, List<String> statuses) {
        System.out.println("[Controller] loadMaintenanceData - Called with: page=" + page + ", size=" + size + ", statuses=" + statuses);
        if (loadingIndicator != null) loadingIndicator.setVisible(true);
        if (bookingsTable != null) bookingsTable.setDisable(true);

        Task<Page<MaintenanceResponse>> task = new Task<>() {
            @Override
            protected Page<MaintenanceResponse> call() throws Exception {
                System.out.println("[Controller] Task call - Attempting to fetch data from ApiService...");
                return maintenanceApiService.getMaintenanceTickets(page, size, statuses);
            }
        };

        task.setOnSucceeded(event -> {
            System.out.println("[Controller] Task succeeded.");
            Page<MaintenanceResponse> pageResult = task.getValue();
            System.out.println("[Controller] Task succeeded - Page Result from ApiService: " + pageResult);

            if (pageResult != null) {
                System.out.println("[Controller] Task succeeded - Page Content: " + (pageResult.getContent() != null ? "Size=" + pageResult.getContent().size() : "NULL Content"));
                if(pageResult.getContent() != null) {
                    maintenanceList.setAll(pageResult.getContent());
                } else {
                    maintenanceList.clear();
                    System.out.println("[Controller] Task succeeded - Page Content was null, cleared maintenanceList.");
                }
                currentPage = pageResult.getNumber();
                totalPages = pageResult.getTotalPages();
                totalElements = pageResult.getTotalElements();
                pageSize = pageResult.getSize();
                updatePageInfo();
            } else {
                System.out.println("[Controller] Task succeeded - Page Result from ApiService was NULL.");
                maintenanceList.clear();
                updatePageInfo(); // Cập nhật để hiển thị thông tin rỗng
            }
            cleanupAfterLoad();
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            System.err.println("[Controller] Task failed.");
            if (exception != null) {
                System.err.println("[Controller] Task failed - Exception: " + exception.getMessage());
                exception.printStackTrace();
                showErrorAlert("Lỗi Tải Dữ Liệu", "Không thể tải danh sách phiếu bảo trì: " + exception.getMessage());
            } else {
                System.err.println("[Controller] Task failed - Unknown error (exception is null).");
                showErrorAlert("Lỗi Tải Dữ Liệu", "Không thể tải danh sách phiếu bảo trì: Lỗi không xác định.");
            }
            maintenanceList.clear();
            cleanupAfterLoad();
            updatePageInfo();
        });
        System.out.println("[Controller] loadMaintenanceData - Starting new Thread for Task.");
        new Thread(task).start();
    }

    private void cleanupAfterLoad() {
        System.out.println("[Controller] cleanupAfterLoad - Called.");
        if (loadingIndicator != null) loadingIndicator.setVisible(false);
        if (bookingsTable != null) bookingsTable.setDisable(false);
    }

    private List<String> getSelectedStatusFilters() {
        List<String> selectedStatuses = new ArrayList<>();
        if (statusReported != null && statusReported.isSelected()) selectedStatuses.add("REPORTED");
        if (statusIn_Progress != null && statusIn_Progress.isSelected()) selectedStatuses.add("IN_PROGRESS");
        if (statusCompleted != null && statusCompleted.isSelected()) selectedStatuses.add("COMPLETED");
        if (statusCannot_Repair != null && statusCannot_Repair.isSelected()) selectedStatuses.add("CANNOT_REPAIR");
        if (statusCancelled != null && statusCancelled.isSelected()) selectedStatuses.add("CANCELLED");
        System.out.println("[Controller] getSelectedStatusFilters - Selected: " + selectedStatuses);
        return selectedStatuses;
    }

    private void applyFiltersAndReload() {
        System.out.println("[Controller] applyFiltersAndReload - Called.");
        currentPage = 0;
        loadMaintenanceData(currentPage, pageSize, getSelectedStatusFilters());
    }

    private void updatePageInfo() {
        System.out.println("[Controller] updatePageInfo - Called. CurrentPage=" + currentPage + ", TotalPages=" + totalPages + ", TotalElements=" + totalElements + ", PageSize=" + pageSize);
        if (bookingsTable == null || pageInfoLabel == null || prevPageButton == null || nextPageButton == null || rowsPerPageComboBox == null) {
            System.err.println("[Controller] updatePageInfo - Error: One or more pagination UI components are null.");
            return;
        }

        if (totalElements == 0) {
            pageInfoLabel.setText("Không có dữ liệu");
        } else {
            long startElement = (long)currentPage * pageSize + 1;
            long endElement = Math.min((long)(currentPage + 1) * pageSize, totalElements);
            pageInfoLabel.setText(String.format("%d-%d của %d", startElement, endElement, totalElements));
        }
        prevPageButton.setDisable(currentPage == 0);
        nextPageButton.setDisable(currentPage >= totalPages - 1);

        if (rowsPerPageComboBox.getValue() == null || rowsPerPageComboBox.getValue() != pageSize) {
            System.out.println("[Controller] updatePageInfo - Setting rowsPerPageComboBox value to: " + pageSize);
            rowsPerPageComboBox.setValue(pageSize);
        }
    }

    private void setupFiltersAndPagination() {
        System.out.println("[Controller] setupFiltersAndPagination - Called.");
        // CheckBox listeners
        if (statusReported != null) statusReported.setOnAction(event -> applyFiltersAndReload());
        // ... (tương tự cho các checkbox khác) ...
        if (statusIn_Progress != null) statusIn_Progress.setOnAction(event -> applyFiltersAndReload());
        if (statusCompleted != null) statusCompleted.setOnAction(event -> applyFiltersAndReload());
        if (statusCannot_Repair != null) statusCannot_Repair.setOnAction(event -> applyFiltersAndReload());
        if (statusCancelled != null) statusCancelled.setOnAction(event -> applyFiltersAndReload());


        if (rowsPerPageComboBox != null) {
            rowsPerPageComboBox.getItems().addAll(5, 10, 20, 50);
            rowsPerPageComboBox.setValue(pageSize);
            rowsPerPageComboBox.setOnAction(event -> {
                Integer selectedPageSize = rowsPerPageComboBox.getValue();
                System.out.println("[Controller] rowsPerPageComboBox - Action, selectedPageSize: " + selectedPageSize);
                if (selectedPageSize != null && selectedPageSize != pageSize) {
                    pageSize = selectedPageSize;
                    currentPage = 0;
                    loadMaintenanceData(currentPage, pageSize, getSelectedStatusFilters());
                }
            });
        }

        if (prevPageButton != null) {
            prevPageButton.setOnAction(event -> {
                System.out.println("[Controller] prevPageButton - Action.");
                if (currentPage > 0) {
                    currentPage--;
                    loadMaintenanceData(currentPage, pageSize, getSelectedStatusFilters());
                }
            });
        }
        if (nextPageButton != null) {
            nextPageButton.setOnAction(event -> {
                System.out.println("[Controller] nextPageButton - Action.");
                if (currentPage < totalPages - 1) {
                    currentPage++;
                    loadMaintenanceData(currentPage, pageSize, getSelectedStatusFilters());
                }
            });
        }
    }

    private void showErrorAlert(String title, String content) {
        System.err.println("[Controller] showErrorAlert - Title: " + title + ", Content: " + content);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}