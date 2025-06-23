package com.utc2.facilityui.utils;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException; // Import để bắt lỗi cụ thể

public class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public JsonElement serialize(LocalDateTime localDateTime, Type typeOfSrc, JsonSerializationContext context) {
        // Giả định rằng nếu localDateTime là null, Gson().nullSafe() đã xử lý,
        // nhưng vẫn tốt nếu có kiểm tra ở đây để adapter có thể đứng độc lập.
        if (localDateTime == null) {
            return JsonNull.INSTANCE; // Trả về JsonNull nếu giá trị là null
        }
        return new JsonPrimitive(localDateTime.format(FORMATTER));
    }

    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        // Giả định rằng nếu json là JsonNull, Gson().nullSafe() đã xử lý,
        // nhưng vẫn tốt nếu có kiểm tra ở đây.
        if (json == null || json.isJsonNull()) {
            return null; // Trả về null nếu JSON element là null
        }
        String jsonString = json.getAsString();
        try {
            return LocalDateTime.parse(jsonString, FORMATTER);
        } catch (DateTimeParseException e) {
            // Bạn có thể thêm logic thử parse các định dạng khác ở đây nếu cần
            System.err.println("LocalDateTimeAdapter: Không thể parse chuỗi ngày tháng '" + jsonString + "'. Lỗi: " + e.getMessage());
            // Ném lỗi hoặc trả về null/giá trị mặc định tùy theo yêu cầu
            throw new JsonParseException("Không thể parse ngày: " + jsonString, e);
        }
    }
}