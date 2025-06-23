package com.utc2.facilityui.controller.booking;

import com.utc2.facilityui.response.BookingResponse;
import com.utc2.facilityui.model.BookedEquipmentItem; // Cần import này
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class InfoBookingController {

    @FXML private Label nameRoom;
    @FXML private Label purpose;
    @FXML private Label equipmentDefault;
    @FXML private Label dayTime; // Ngày mượn
    @FXML private Label timeRange; // Thời gian mượn
    @FXML private Label userName;
    @FXML private Label createdAt; // Ngày tạo
    @FXML private Label updatedAt; // Ngày cập nhật
    @FXML private Label note;

    // Định dạng ngày giờ
    // Ví dụ: "Thứ Hai, 02/06/2025" (Mon Jun 02 2025 trong ảnh có vẻ dùng tiếng Anh cho ngày/tháng)
    // Để giống ảnh: "EEE MMM dd yyyy" với Locale.US
    // Hoặc thuần Việt: "EEE, dd/MM/yyyy" với new Locale("vi", "VN")
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEE, dd/MM/yyyy", new Locale("vi", "VN"));
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm"); // Sử dụng HH:mm cho 24h hoặc hh:mm a cho AM/PM
    private final DateTimeFormatter amPmTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US); // Giống ảnh "07:00 AM - 11:30 AM"
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy", new Locale("vi", "VN"));


    public void setBookingDetails(BookingResponse booking) {
        if (booking == null) {
            // Xử lý trường hợp booking null nếu cần, ví dụ: hiển thị "Không có dữ liệu"
            nameRoom.setText("N/A");
            purpose.setText("N/A");
            equipmentDefault.setText("N/A");
            dayTime.setText("N/A");
            timeRange.setText("N/A");
            userName.setText("N/A");
            createdAt.setText("N/A");
            updatedAt.setText("N/A");
            note.setText("N/A");
            return;
        }

        nameRoom.setText(safeGet(booking.getRoomName()));
        purpose.setText(safeGet(booking.getPurpose()));

        // Xử lý thiết bị
        List<BookedEquipmentItem> equipments = booking.getBookedEquipments();
        if (equipments != null && !equipments.isEmpty()) {
            String equipmentDisplay = equipments.stream()
                    .map(BookedEquipmentItem::getEquipmentModelName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(", "));
            equipmentDefault.setText(equipmentDisplay.isEmpty() ? "Không có thiết bị nào được yêu cầu" : equipmentDisplay);
        } else {
            equipmentDefault.setText("Không có thiết bị nào được yêu cầu");
        }

        // Ngày mượn và Thời gian mượn từ plannedStartTime và plannedEndTime
        if (booking.getPlannedStartTime() != null) {
            // Để giống ảnh "Mon Jun 02 2025", bạn có thể dùng:
            // DateTimeFormatter imageDateFormatter = DateTimeFormatter.ofPattern("EEE MMM dd yyyy", Locale.ENGLISH);
            // dayTime.setText(booking.getPlannedStartTime().format(imageDateFormatter));
            dayTime.setText(booking.getPlannedStartTime().format(dateFormatter)); // Hoặc dùng dateFormatter đã định nghĩa
        } else {
            dayTime.setText("N/A");
        }

        if (booking.getPlannedStartTime() != null && booking.getPlannedEndTime() != null) {
            String startTimeStr = booking.getPlannedStartTime().format(amPmTimeFormatter);
            String endTimeStr = booking.getPlannedEndTime().format(amPmTimeFormatter);
            timeRange.setText(String.format("%s - %s", startTimeStr, endTimeStr));
        } else {
            timeRange.setText("N/A");
        }

        userName.setText(safeGet(booking.getUserName()));

        if (booking.getCreatedAt() != null) {
            createdAt.setText(booking.getCreatedAt().format(dateTimeFormatter));
        } else {
            createdAt.setText("N/A");
        }

        // Giả sử BookingResponse có getUpdatedAt() hoặc một trường tương tự cho ngày cập nhật
        // Nếu không có, bạn có thể hiển thị "N/A" hoặc thông tin khác nếu phù hợp
        // Ví dụ: booking.getApprovedAt() hoặc booking.getModifiedAt()
        if (booking.getUpdatedAt() != null) { // Thay getUpdatedAt() bằng phương thức thực tế nếu có
            updatedAt.setText(booking.getUpdatedAt().format(dateTimeFormatter));
        } else {
            // Trong ảnh để trống, bạn có thể để trống hoặc "Chưa cập nhật", "N/A"
            updatedAt.setText(""); // Hoặc "N/A"
        }
        // Nếu bạn muốn hiển thị người duyệt nếu có và đã duyệt
        // else if (booking.getApprovedAt() != null && booking.getApprovedByUserName() != null) {
        //    updatedAt.setText("Duyệt lúc: " + booking.getApprovedAt().format(dateTimeFormatter) + " bởi " + booking.getApprovedByUserName());
        // }

        note.setText(booking.getNote() != null && !booking.getNote().isEmpty() ? booking.getNote() : "Không có ghi chú");
    }

    private String safeGet(String text) {
        return text != null ? text : "N/A";
    }

    // Bạn có thể thêm một nút đóng hoặc xử lý đóng cửa sổ nếu cần,
    // nhưng FXML này không có nút đóng nên nó sẽ dựa vào nút đóng của cửa sổ HĐH.
}