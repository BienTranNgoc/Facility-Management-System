package com.utc2.facilityui.controller;

import com.utc2.facilityui.model.User;
import com.utc2.facilityui.response.PageMetadata;
import com.utc2.facilityui.response.UserResponse;
import com.utc2.facilityui.service.UserServices;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

// Bỏ comment nếu bạn có EditUserController và AddUserController thực tế
// import com.utc2.facilityui.controller.EditUserController;
// import com.utc2.facilityui.controller.AddUserController;

public class AccountManagementController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private Button btnAddUser; // Trong FXML: fx:id="btnAddFacility", onAction="#handleOpenAddUserDialog"

    @FXML private TableView<User> facilityTable; // Trong FXML: fx:id="accountTable"
    @FXML private TableColumn<User, String> userId;
    @FXML private TableColumn<User, String> fullName;
    @FXML private TableColumn<User, String> email;
    @FXML private TableColumn<User, String> roleName;
    @FXML private TableColumn<User, String> createdAt;
    @FXML private TableColumn<User, String> updatedAt;
    @FXML private TableColumn<User, Void> fix_Delete;
    @FXML private TextField filterYearTextField;
    @FXML private Button btnResetFilters;
    @FXML private ComboBox<Integer> rowsPerPageComboBox;
    @FXML private Button prevPageButton; // Trong FXML: onAction="#handlePrevPage"
    @FXML private Label pageInfoLabel;
    @FXML private Button nextPageButton; // Trong FXML: onAction="#handleNextPage"

    private ObservableList<User> userObservableList = FXCollections.observableArrayList();
    private int currentPage = 0; // 0-indexed cho API
    private int pageSize = 10;
    private int totalPages = 0;
    private long totalElements = 0; // Thêm biến để lưu trữ tổng số phần tử
    private Integer currentYearFilter = null;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("AccountManagementController: Initializing...");

        setupTableColumns();
        setupPaginationControls();

        if (filterYearTextField != null) {
            filterYearTextField.setOnAction(event -> applyAccountFiltersAndReloadData());
        } else {
            System.err.println("CTRL_ACC: filterYearTextField is NULL!");
        }

        if (btnResetFilters != null) {
            btnResetFilters.setOnAction(event -> handleResetAccountFilters());
        } else {
            System.err.println("CTRL_ACC: btnResetFilters is NULL!");
        }

        // Gọi loadUsers với 2 tham số, nó sẽ tự lấy currentYearFilter (ban đầu là null)
        loadUsers(currentPage, pageSize);
        System.out.println("AccountManagementController: Initialization complete.");
    }

    private void setupTableColumns() {
        System.out.println("AccountManagementController: Setting up table columns...");
        if (userId != null) userId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        else System.err.println("CTRL_ACC: userId TableColumn is NULL!");

        if (fullName != null) fullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        else System.err.println("CTRL_ACC: fullName TableColumn is NULL!");

        if (email != null) email.setCellValueFactory(new PropertyValueFactory<>("email"));
        else System.err.println("CTRL_ACC: email TableColumn is NULL!");

        if (roleName != null) roleName.setCellValueFactory(new PropertyValueFactory<>("roleName"));
        else System.err.println("CTRL_ACC: roleName TableColumn is NULL!");

        if (createdAt != null) createdAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        else System.err.println("CTRL_ACC: createdAt TableColumn is NULL!");

        if (updatedAt != null) updatedAt.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));
        else System.err.println("CTRL_ACC: updatedAt TableColumn is NULL!");

        if (fix_Delete != null) {
            fix_Delete.setText("Hành động"); // Đặt lại tên cột cho phù hợp
            // Không cần setCellValueFactory vì cột này không hiển thị trực tiếp dữ liệu từ User

            Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = new Callback<>() {
                @Override
                public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                    final TableCell<User, Void> cell = new TableCell<>() {

                        private final Button btnEdit = new Button();
                        private final Button btnDelete = new Button();
                        private final HBox pane = new HBox(8); // 8 là khoảng cách giữa 2 nút

                        {
                            // Thiết lập nút Sửa (Edit)
                            try {
                                // Giả sử icon của bạn nằm trong /com/utc2/facilityui/img/
                                // Điều chỉnh đường dẫn nếu cần thiết
                                Image editIcon = new Image(getClass().getResourceAsStream("/com/utc2/facilityui/images/pencil.png"));
                                ImageView ivEdit = new ImageView(editIcon);
                                ivEdit.setFitHeight(16); // Kích thước icon
                                ivEdit.setFitWidth(16);
                                btnEdit.setGraphic(ivEdit);
                                btnEdit.setTooltip(new Tooltip("Sửa tài khoản"));
                                // Style để nút chỉ hiển thị icon và có padding nhỏ
                                btnEdit.setStyle("-fx-background-color: transparent; -fx-padding: 3;-fx-cursor: hand;");
                            } catch (Exception e) {
                                System.err.println("Không thể tải icon sửa: " + e.getMessage());
                                btnEdit.setText("Sửa"); // Hiển thị chữ nếu không tải được icon
                            }

                            // Thiết lập nút Xóa (Delete)
                            try {
                                Image deleteIcon = new Image(getClass().getResourceAsStream("/com/utc2/facilityui/images/delete.png"));
                                ImageView ivDelete = new ImageView(deleteIcon);
                                ivDelete.setFitHeight(16);
                                ivDelete.setFitWidth(16);
                                btnDelete.setGraphic(ivDelete);
                                btnDelete.setTooltip(new Tooltip("Xóa tài khoản"));
                                btnDelete.setStyle("-fx-background-color: transparent; -fx-padding: 3;-fx-cursor: hand;");
                            } catch (Exception e) {
                                System.err.println("Không thể tải icon xóa: " + e.getMessage());
                                btnDelete.setText("Xóa"); // Hiển thị chữ nếu không tải được icon
                            }

                            // Gán hành động cho nút Sửa
                            btnEdit.setOnAction((ActionEvent event) -> {
                                User user = getTableView().getItems().get(getIndex());
                                if (user != null) {
                                    handleOpenEditUserDialog(user);
                                }
                            });

                            // Gán hành động cho nút Xóa
                            btnDelete.setOnAction((ActionEvent event) -> {
                                User user = getTableView().getItems().get(getIndex());
                                if (user != null) {
                                    handleDeleteUser(user);
                                }
                            });

                            pane.getChildren().addAll(btnEdit, btnDelete);
                            pane.setAlignment(Pos.CENTER); // Căn giữa các nút trong HBox
                        }

                        @Override
                        public void updateItem(Void item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setGraphic(null); // Không hiển thị gì nếu ô trống
                            } else {
                                setGraphic(pane); // Hiển thị HBox chứa các nút
                            }
                        }
                    };
                    return cell;
                }
            };

            fix_Delete.setCellFactory(cellFactory);
            fix_Delete.setStyle("-fx-alignment: CENTER;"); // Căn giữa nội dung của cả ô (HBox)

        } else System.err.println("CTRL_ACC: fix_Delete (actions) TableColumn is NULL!");


        if (facilityTable != null) {
            facilityTable.setItems(userObservableList);
            facilityTable.setPlaceholder(new Label("Không có dữ liệu tài khoản để hiển thị."));
        } else {
            System.err.println("CTRL_ACC: accountTable is NULL! FXML injection failed?");
        }
        System.out.println("AccountManagementController: Table columns setup complete.");
    }

    private void setupPaginationControls() {
        System.out.println("AccountManagementController: Setting up pagination controls...");
        if (rowsPerPageComboBox != null) {
            rowsPerPageComboBox.setItems(FXCollections.observableArrayList(5, 10, 15, 20, 25, 50));
            Integer currentComboBoxValue = rowsPerPageComboBox.getValue();
            if (currentComboBoxValue != null && currentComboBoxValue > 0) {
                pageSize = currentComboBoxValue;
            } else {
                rowsPerPageComboBox.setValue(pageSize);
            }
            rowsPerPageComboBox.setOnAction(event -> {
                pageSize = rowsPerPageComboBox.getValue();
                currentPage = 0; // Reset về trang đầu khi đổi kích thước trang
                System.out.println("CTRL_ACC: Page size changed to " + pageSize + ". Loading page 0.");
                loadUsers(currentPage, pageSize);
            });
        } else {
            System.err.println("CTRL_ACC: rowsPerPageComboBox is NULL!");
        }
        // Gọi updatePaginationUI với giá trị khởi tạo (chưa có dữ liệu)
        updatePaginationUI(null, 0);
        System.out.println("AccountManagementController: Pagination controls setup complete.");
    }

    @FXML
    private void handlePrevPage(ActionEvent event) {
        if (currentPage > 0) {
            currentPage--;
            System.out.println("CTRL_ACC: PrevPage clicked. Loading page " + currentPage);
            loadUsers(currentPage, pageSize);
        } else {
            System.out.println("CTRL_ACC: PrevPage clicked, but already on first page (currentPage=" + currentPage + ").");
        }
    }

    @FXML
    private void handleNextPage(ActionEvent event) {
        // totalPages là 1-indexed, currentPage là 0-indexed
        if (currentPage < totalPages - 1) {
            currentPage++;
            System.out.println("CTRL_ACC: NextPage clicked. Loading page " + currentPage);
            loadUsers(currentPage, pageSize);
        } else {
            System.out.println("CTRL_ACC: NextPage clicked, but already on last page (currentPage=" + currentPage + ", totalPages=" + totalPages + ").");
        }
    }
    @FXML
    private void handleResetAccountFilters() {
        System.out.println("CTRL_ACC: Reset Account Filters button clicked.");
        if (filterYearTextField != null) {
            filterYearTextField.clear();
        }
        this.currentYearFilter = null; // Reset biến thành viên
        applyAccountFiltersAndReloadData();
    }

    private void applyAccountFiltersAndReloadData() {
        System.out.println("CTRL_ACC: Applying account filters and reloading data.");
        currentPage = 0;
        // Cập nhật currentYearFilter từ TextField
        if (filterYearTextField != null && !filterYearTextField.getText().trim().isEmpty()) {
            try {
                this.currentYearFilter = Integer.parseInt(filterYearTextField.getText().trim());
                // Có thể thêm kiểm tra tính hợp lệ của năm ở đây
            } catch (NumberFormatException e) {
                System.err.println("CTRL_ACC: Invalid year format: " + filterYearTextField.getText());
                showAlert(Alert.AlertType.WARNING, "Năm không hợp lệ", "Vui lòng nhập năm là một số (ví dụ: 2024).");
                this.currentYearFilter = null; // Nếu sai định dạng, coi như không lọc
            }
        } else {
            this.currentYearFilter = null; // Nếu ô trống, không lọc
        }
        loadUsers(currentPage, pageSize); // Gọi loadUsers với 2 tham số
    }
    @FXML
    private void handleOpenAddUser(ActionEvent event) {
        System.out.println("CTRL_ACC: Add User button clicked.");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/utc2/facilityui/form/addUser.fxml"));
            Parent addUserDialogRoot = loader.load();
            // AddUserController addUserCtrl = loader.getController();
            // addUserCtrl.someInitializationMethodIfNeccessary();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Thêm Tài Khoản Mới");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            Window owner = getWindow();
            if (owner != null) dialogStage.initOwner(owner);
            dialogStage.setScene(new Scene(addUserDialogRoot));
            dialogStage.showAndWait();
            System.out.println("CTRL_ACC: AddUserDialog closed. Reloading users from page 0.");
            loadUsers(0, pageSize); // Tải lại trang đầu sau khi thêm thành công
        } catch (IOException e) {
            System.err.println("CTRL_ACC: IOException while loading AddUserDialog.fxml");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Giao Diện", "Không thể mở cửa sổ thêm tài khoản.\nChi tiết: " + e.getMessage());
        } catch (NullPointerException e) {
            System.err.println("CTRL_ACC: NullPointerException - AddUserDialog.fxml not found?");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Tải FXML", "Không tìm thấy file AddUserDialog.fxml. Vui lòng kiểm tra đường dẫn.");
        }
    }

    private void handleOpenEditUserDialog(User userToEdit) {
        if (userToEdit == null) {
            System.out.println("CTRL_ACC: Edit request for null user.");
            return;
        }
        // Quan trọng: Cần lấy ID gốc, không định dạng, để truy vấn API
        // String rawUserId = userToEdit.getRawUserId(); // Giả sử bạn đã thêm getRawUserId() vào User.java
        // if (rawUserId == null || rawUserId.equals("N/A")) {
        //     showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lấy mã người dùng hợp lệ để sửa.");
        //     return;
        // }

        System.out.println("CTRL_ACC: Opening edit dialog for user ID: " + userToEdit.getUserId() + ", Name: " + userToEdit.getFullName());

        try {
            // Đường dẫn đến file FXML, giả sử nằm trong /com/utc2/facilityui/form/
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/utc2/facilityui/form/editUser.fxml"));
            Parent editUserDialogRoot = loader.load();

            // Lấy controller của dialog sửa
            EditUserController editUserController = loader.getController();
            editUserController.setUserData(userToEdit); // Truyền dữ liệu người dùng cần sửa sang

            // Tạo một Stage mới cho dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Chỉnh sửa thông tin tài khoản");
            dialogStage.initModality(Modality.APPLICATION_MODAL); // Chặn tương tác với cửa sổ cha
            Window owner = getWindow(); // Lấy cửa sổ cha
            if (owner != null) {
                dialogStage.initOwner(owner);
            }
            dialogStage.setScene(new Scene(editUserDialogRoot));

            // Đặt callback để làm mới bảng sau khi lưu
            editUserController.setOnUserUpdatedCallback(() -> {
                System.out.println("CTRL_ACC: User updated callback triggered. Reloading users.");
                loadUsers(currentPage, pageSize); // Tải lại trang hiện tại
            });

            dialogStage.showAndWait(); // Hiển thị dialog và đợi cho đến khi nó đóng

        } catch (IOException e) {
            System.err.println("CTRL_ACC: IOException while loading editUser.fxml");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Giao Diện", "Không thể mở cửa sổ chỉnh sửa tài khoản.\nChi tiết: " + e.getMessage());
        } catch (NullPointerException e) {
            System.err.println("CTRL_ACC: NullPointerException - editUser.fxml not found?");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Tải FXML", "Không tìm thấy file editUser.fxml. Vui lòng kiểm tra đường dẫn.");
        }
    }
    private void handleDeleteUser(User userToDelete) {
        if (userToDelete == null) {
            showAlert(Alert.AlertType.WARNING, "Lỗi Thao Tác", "Không có người dùng nào được chọn để xóa.");
            return;
        }

        // QUAN TRỌNG: Sử dụng ID gốc (business key) để gọi API.
        // Giả định bạn đã có phương thức getRawUserId() trong lớp User.java trả về mã người dùng gốc.
        // Ví dụ: userToDelete.getRawUserId() thay vì userToDelete.getUserId() (có thể đã bị định dạng)
        String userId = userToDelete.getUserId(); // HOẶC userToDelete.getUserId() NẾU BẠN CHẮC CHẮN NÓ LÀ ID GỐC

        // Lấy tên để hiển thị, ưu tiên họ tên, nếu không có thì dùng username
        String userDisplayName = (userToDelete.getFullName() != null && !userToDelete.getFullName().isEmpty())
                ? userToDelete.getFullName()
                : userToDelete.getUsername();
        // Kiểm tra lại userId sau khi lấy giá trị gốc
        if (userId == null || userId.isEmpty() || userId.equalsIgnoreCase("N/A")) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Dữ Liệu", "Không thể xác định mã người dùng hợp lệ để xóa.");
            return;
        }

        System.out.println("CTRL_ACC: Yêu cầu xóa người dùng ID: " + userId + ", Tên hiển thị: " + userDisplayName);

        // Tạo hộp thoại xác nhận
        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle("Xác nhận Xóa Tài Khoản");
        confirmationDialog.setHeaderText("Bạn có chắc chắn muốn xóa tài khoản: " + userDisplayName + " (Mã: " + userId + ")?");
        confirmationDialog.setContentText("Hành động này không thể hoàn tác!");

        // Đặt owner cho dialog nếu có thể để nó hiển thị đúng trên cửa sổ chính
        Window owner = getWindow(); // getWindow() là phương thức bạn đã có
        if (owner != null) {
            confirmationDialog.initOwner(owner);
        }

        Optional<ButtonType> result = confirmationDialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Người dùng đã xác nhận xóa
            System.out.println("CTRL_ACC: Người dùng xác nhận xóa ID: " + userId);

            // Thực hiện xóa trên luồng nền
            Task<Void> deleteTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    // Gọi service để xóa người dùng bằng userId (business key)
                    UserServices.deleteUser(userId);
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.INFORMATION, "Thành Công", "Đã xóa thành công tài khoản: " + userDisplayName);
                        // Tải lại danh sách người dùng trên trang hiện tại.
                        // Phương thức loadUsers đã có logic xử lý nếu trang hiện tại trống sau khi xóa.
                        loadUsers(currentPage, pageSize);
                    });
                }

                @Override
                protected void failed() {
                    super.failed();
                    Throwable exception = getException();
                    exception.printStackTrace(); // In lỗi chi tiết ra console để debug
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Xóa Thất Bại", "Không thể xóa tài khoản: " + userDisplayName + ".\nLỗi: " + exception.getMessage());
                    });
                }
            };
            new Thread(deleteTask).start();

        } else {
            // Người dùng hủy bỏ thao tác xóa
            System.out.println("CTRL_ACC: Người dùng hủy thao tác xóa cho ID: " + userId);
        }
    }

    private void loadUsers(int page, int size) {
        if (facilityTable == null) {
            System.err.println("CTRL_ACC: accountTable is NULL in loadUsers. FXML not injected correctly.");
            return;
        }
        System.out.println("CTRL_ACC: loadUsers called for page: " + page + ", size: " + size);
        facilityTable.setPlaceholder(new Label("Đang tải dữ liệu..."));
        // userObservableList.clear(); // Không cần thiết vì setAll() sẽ thay thế toàn bộ

        Task<UserServices.PaginatedUsers> loadUsersTask = new Task<>() {
            @Override
            protected UserServices.PaginatedUsers call() throws Exception {
                System.out.println("CTRL_ACC: Task call() - Requesting users from service for page: " + page + ", size: " + size);
                return UserServices.getUsers(page, size);
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                UserServices.PaginatedUsers paginatedResult = getValue();
                System.out.println("CTRL_ACC: Task succeeded(). Received PaginatedResult isPresent: " + (paginatedResult != null));

                List<UserResponse> userDtoList = new ArrayList<>();
                PageMetadata metadata = null;

                if (paginatedResult != null) {
                    if (paginatedResult.getContent() != null) {
                        userDtoList = paginatedResult.getContent();
                    }
                    metadata = paginatedResult.getPageMetadata(); // Có thể là null

                    if (metadata != null) {
                        System.out.println("CTRL_ACC: Received Metadata - TotalElements: " + metadata.getTotalElements() +
                                ", TotalPages: " + metadata.getTotalPages() +
                                ", CurrentPageFromDTO (0-indexed): " + metadata.getNumber() +
                                ", Size: " + metadata.getSize());
                        // Cập nhật state của controller từ metadata
                        AccountManagementController.this.currentPage = metadata.getNumber();
                        AccountManagementController.this.totalPages = metadata.getTotalPages();
                        AccountManagementController.this.totalElements = metadata.getTotalElements();
                    } else {
                        // Nếu metadata là null, cố gắng suy luận từ content nếu có
                        System.err.println("CTRL_ACC: PageMetadata from API is null!");
                        AccountManagementController.this.totalElements = userDtoList.size();
                        AccountManagementController.this.currentPage = 0; // Giả định là trang đầu
                        if (pageSize > 0 && totalElements > 0) {
                            AccountManagementController.this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
                        } else {
                            AccountManagementController.this.totalPages = userDtoList.isEmpty() ? 0 : 1;
                        }
                        if (AccountManagementController.this.totalPages == 0 && !userDtoList.isEmpty()) {
                            AccountManagementController.this.totalPages = 1;
                        }
                    }
                } else {
                    System.err.println("CTRL_ACC: PaginatedResult from service is NULL. Setting pagination to empty.");
                    AccountManagementController.this.currentPage = 0;
                    AccountManagementController.this.totalPages = 0;
                    AccountManagementController.this.totalElements = 0;
                }

                userObservableList.setAll(
                        userDtoList.stream()
                                .map(com.utc2.facilityui.model.User::new) // Sử dụng model.User "thông minh"
                                .collect(Collectors.toList())
                );
                System.out.println("CTRL_ACC: userObservableList updated. Size: " + userObservableList.size());

                // Logic kiểm tra và lùi trang nếu trang hiện tại trống (sau khi xóa)
                if (userObservableList.isEmpty() && AccountManagementController.this.currentPage > 0 && AccountManagementController.this.totalElements > 0) {
                    System.out.println("CTRL_ACC: Current page " + AccountManagementController.this.currentPage + " is empty, but totalElements=" + AccountManagementController.this.totalElements + ". Loading previous page.");
                    AccountManagementController.this.currentPage--; // Lùi về trang trước đó
                    loadUsers(AccountManagementController.this.currentPage, pageSize); // Gọi lại loadUsers cho trang mới
                    return; // Dừng xử lý của lần succeeded() này, vì loadUsers mới sẽ xử lý
                }

                updatePaginationUI(metadata, AccountManagementController.this.totalElements); // Truyền totalElements từ state của controller

                if (userObservableList.isEmpty()) {
                    facilityTable.setPlaceholder(new Label("Không có dữ liệu tài khoản nào."));
                } else {
                    facilityTable.setPlaceholder(null); // Xóa placeholder nếu có dữ liệu
                }
                System.out.println("CTRL_ACC: UI updated. Controller state: currentPage=" + AccountManagementController.this.currentPage +
                        ", totalPages=" + AccountManagementController.this.totalPages +
                        ", totalElements=" + AccountManagementController.this.totalElements);
            }

            @Override
            protected void failed() {
                super.failed();
                System.err.println("CTRL_ACC: Task failed to load users.");
                userObservableList.clear();
                // Reset state phân trang khi lỗi
                AccountManagementController.this.currentPage = 0;
                AccountManagementController.this.totalPages = 0;
                AccountManagementController.this.totalElements = 0;
                updatePaginationUI(null, 0);

                Throwable exception = getException();
                exception.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi Tải Dữ Liệu", "Không thể tải danh sách tài khoản:\n" + exception.getMessage());
                if (facilityTable != null) facilityTable.setPlaceholder(new Label("Lỗi khi tải dữ liệu. Vui lòng thử lại."));
            }
        };
        new Thread(loadUsersTask).start();
    }

    private void updatePaginationUI(PageMetadata metadata, long currentTotalElements) {
        Platform.runLater(() -> {
            int displayCurrentPage = 0; // Trang hiển thị cho người dùng (1-indexed)
            int displayTotalPages = 0;

            // Sử dụng state của controller đã được cập nhật trong loadUsers().succeeded()
            // thay vì chỉ dựa vào metadata truyền vào (vì metadata có thể null ở lần gọi đầu)
            if (this.totalElements > 0) {
                displayCurrentPage = this.currentPage + 1;
                displayTotalPages = this.totalPages;
            }

            if (pageInfoLabel != null) {
                pageInfoLabel.setText("Trang " + displayCurrentPage + " / " + displayTotalPages);
            }

            boolean noData = (currentTotalElements == 0); // Dựa vào totalElements truyền vào để biết có dữ liệu hay không

            if (prevPageButton != null) {
                prevPageButton.setDisable(this.currentPage == 0 || noData);
            }
            if (nextPageButton != null) {
                nextPageButton.setDisable(this.totalPages == 0 || this.currentPage >= this.totalPages - 1 || noData);
            }
            if (rowsPerPageComboBox != null) {
                rowsPerPageComboBox.setDisable(noData);
            }
            System.out.println("CTRL_ACC: Pagination UI updated. Display: " + displayCurrentPage + "/" + displayTotalPages +
                    ". Controller state: currentPage=" + this.currentPage + ", totalPages=" + this.totalPages +
                    ", currentTotalElementsArg=" + currentTotalElements);
        });
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            TextArea textArea = new TextArea(message);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            alert.getDialogPane().setContent(textArea);
            alert.setResizable(true);
            Window owner = getWindow();
            if (owner != null) alert.initOwner(owner);
            alert.showAndWait();
        });
    }

    private Window getWindow() {
        try {
            if (facilityTable != null && facilityTable.getScene() != null && facilityTable.getScene().getWindow() != null) {
                return facilityTable.getScene().getWindow();
            }
            if (btnAddUser != null && btnAddUser.getScene() != null && btnAddUser.getScene().getWindow() != null) {
                return btnAddUser.getScene().getWindow();
            }
        } catch (Exception e) { /* Bỏ qua */ }
        return null;
    }
}