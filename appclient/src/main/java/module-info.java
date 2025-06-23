module com.utc2.facilityui {
    // --- Phần Requires ---
    // Giữ lại các requires hiện có của bạn
    requires javafx.fxml;
    requires javafx.web;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires okhttp3;
    requires com.google.gson;
    requires de.jensd.fx.glyphs.fontawesome;
    requires javafx.controls;
    requires javafx.graphics;
    requires com.gluonhq.attach.util;
    requires com.gluonhq.charm.glisten;
    requires kernel; // iText
    requires layout; // iText
    requires java.net.http;

    // Thêm dòng này nếu chưa có, để rõ ràng hơn (mặc dù javafx.controls thường đã kéo theo)
    requires javafx.base;
    requires io;
    requires org.apache.poi.ooxml;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.databind;
    requires java.sql;

    // --- Phần Opens ---
    // Giữ lại các opens cho javafx.fxml
    opens com.utc2.facilityui.controller to javafx.fxml;
    opens com.utc2.facilityui.chatbot to javafx.fxml;
    opens com.utc2.facilityui.app to javafx.fxml;
    opens com.utc2.facilityui.service to javafx.fxml; // Mở service nếu có inject FXML
    opens com.utc2.facilityui.view to javafx.fxml; // Thường không cần thiết
    opens com.utc2.facilityui.controller.auth to javafx.fxml;
    opens com.utc2.facilityui.controller.booking to javafx.fxml;
    opens com.utc2.facilityui.controller.equipment to javafx.fxml;
    opens com.utc2.facilityui.controller.room to javafx.fxml;
    opens com.utc2.facilityui.controller.nav to javafx.fxml;

    // Mở package model cho Gson và JavaFX base (quan trọng nếu model dùng trong TableView)
    opens com.utc2.facilityui.model to com.google.gson, javafx.base;

    // *** SỬA DÒNG NÀY: Thêm javafx.base ***
    opens com.utc2.facilityui.response to com.google.gson, javafx.base;


    // --- Phần Exports ---
    // Giữ lại các exports hiện có của bạn
    exports com.utc2.facilityui.controller;
    exports com.utc2.facilityui.app;
    exports com.utc2.facilityui.controller.auth;
    exports com.utc2.facilityui.controller.booking;
    exports com.utc2.facilityui.controller.equipment;
    exports com.utc2.facilityui.controller.room;
    exports com.utc2.facilityui.controller.nav;
    exports com.utc2.facilityui.model;

    // Bạn có thể cần export cả response và model nếu các module khác cần dùng trực tiếp
    // exports com.utc2.facilityui.response;
    // exports com.utc2.facilityui.model;
}