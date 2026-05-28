package com.example.mobile_survey_application.util;

import android.text.InputFilter;
import android.text.Spanned;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public final class RegisterInputValidator {

    public static final String ERROR_NAME =
            "이름은 2~20자의 한글 또는 영문으로 입력해주세요.";
    public static final String ERROR_NICKNAME =
            "닉네임은 2~12자의 한글, 영문, 숫자로 입력해주세요.";
    public static final String ERROR_TELEPHONE =
            "전화번호는 010으로 시작하는 숫자 11자리로 입력해주세요.";
    public static final String ERROR_BIRTH_DATE =
            "생년월일은 숫자 8자리로 입력해주세요. 예: 20010505";

    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[\\uAC00-\\uD7A3a-zA-Z\\s]{2,20}$");
    private static final Pattern NICKNAME_PATTERN =
            Pattern.compile("^[\\uAC00-\\uD7A3a-zA-Z0-9]{2,12}$");
    private static final Pattern TELEPHONE_PATTERN =
            Pattern.compile("^010-\\d{4}-\\d{4}$");
    private static final Pattern BIRTH_DATE_PATTERN =
            Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    private RegisterInputValidator() {
    }

    public static String validateName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        if (!NAME_PATTERN.matcher(name).matches()) {
            return ERROR_NAME;
        }
        return null;
    }

    public static String validateNickname(String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            return null;
        }
        if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
            return ERROR_NICKNAME;
        }
        return null;
    }

    public static String validateTelephone(String telephone) {
        if (telephone == null || telephone.isEmpty()) {
            return null;
        }
        if (!TELEPHONE_PATTERN.matcher(telephone).matches()) {
            return ERROR_TELEPHONE;
        }
        return null;
    }

    public static String validateBirthDate(String birthDate) {
        if (birthDate == null || birthDate.isEmpty()) {
            return null;
        }
        if (!BIRTH_DATE_PATTERN.matcher(birthDate).matches()) {
            return ERROR_BIRTH_DATE;
        }
        try {
            LocalDate date = LocalDate.parse(birthDate, DateTimeFormatter.ISO_LOCAL_DATE);
            if (date.isAfter(LocalDate.now())) {
                return ERROR_BIRTH_DATE;
            }
        } catch (DateTimeParseException e) {
            return ERROR_BIRTH_DATE;
        }
        return null;
    }

    public static InputFilter nameInputFilter() {
        return new CharClassInputFilter(RegisterInputValidator::isNameChar);
    }

    public static InputFilter nicknameInputFilter() {
        return new CharClassInputFilter(RegisterInputValidator::isNicknameChar);
    }

    private static boolean isNameChar(char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || c == ' '
                || (c >= '\uAC00' && c <= '\uD7A3');
    }

    private static boolean isNicknameChar(char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || (c >= '0' && c <= '9')
                || (c >= '\uAC00' && c <= '\uD7A3');
    }

    private static final class CharClassInputFilter implements InputFilter {

        private final CharPredicate predicate;

        private CharClassInputFilter(CharPredicate predicate) {
            this.predicate = predicate;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!predicate.test(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        }
    }

    private interface CharPredicate {
        boolean test(char c);
    }
}
