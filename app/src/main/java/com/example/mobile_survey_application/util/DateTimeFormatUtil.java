package com.example.mobile_survey_application.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateTimeFormatUtil {

    private static final DateTimeFormatter DISPLAY_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
    private static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter ISO_LOCAL_DATE_TIME =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private DateTimeFormatUtil() {
    }

    /**
     * ISO 날짜/시간 문자열을 화면 표시용 "yyyy.MM.dd HH:mm" 형식으로 변환합니다.
     * 파싱에 실패하면 원본 문자열을 그대로 반환합니다.
     */
    public static String formatDisplayDateTime(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.trim().isEmpty()) {
            return "";
        }

        try {
            LocalDateTime dateTime = parseToLocalDateTime(isoDateTime.trim());
            return dateTime.format(DISPLAY_DATE_TIME);
        } catch (DateTimeParseException | IllegalArgumentException e) {
            return isoDateTime;
        }
    }

    /**
     * ISO 날짜/시간 문자열에서 날짜 부분만 "yyyy.MM.dd" 형식으로 변환합니다.
     * 파싱에 실패하면 원본 문자열을 그대로 반환합니다.
     */
    public static String formatDisplayDate(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.trim().isEmpty()) {
            return "";
        }

        try {
            LocalDateTime dateTime = parseToLocalDateTime(isoDateTime.trim());
            return dateTime.format(DISPLAY_DATE);
        } catch (DateTimeParseException | IllegalArgumentException e) {
            return isoDateTime;
        }
    }

    private static LocalDateTime parseToLocalDateTime(String value) {
        String normalized = normalizeDateTimeString(value);

        if (normalized.length() == 10) {
            return LocalDate.parse(normalized).atStartOfDay();
        }

        if (normalized.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}")) {
            return LocalDateTime.parse(normalized + ":00", ISO_LOCAL_DATE_TIME);
        }

        return LocalDateTime.parse(normalized, ISO_LOCAL_DATE_TIME);
    }

    private static String normalizeDateTimeString(String value) {
        String normalized = value.trim().replace(' ', 'T');

        if (normalized.endsWith("Z")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        normalized = stripTimezoneOffset(normalized);

        int fractionalSecondIndex = normalized.indexOf('.');
        if (fractionalSecondIndex > 0) {
            normalized = normalized.substring(0, fractionalSecondIndex);
        }

        return normalized;
    }

    private static String stripTimezoneOffset(String value) {
        int timeSeparatorIndex = value.indexOf('T');
        if (timeSeparatorIndex < 0) {
            return value;
        }

        for (int i = value.length() - 1; i > timeSeparatorIndex; i--) {
            char ch = value.charAt(i);
            if (ch == '+' || ch == '-') {
                return value.substring(0, i);
            }
        }

        return value;
    }
}
