package com.example.mobile_survey_application.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class PhoneNumberTextWatcher implements TextWatcher {

    private static final int MAX_DIGITS = 11;

    private final EditText editText;
    private boolean updating;

    public PhoneNumberTextWatcher(EditText editText) {
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (updating) {
            return;
        }

        String digits = editable.toString().replaceAll("[^0-9]", "");
        if (digits.length() > MAX_DIGITS) {
            digits = digits.substring(0, MAX_DIGITS);
        }

        String formatted = formatPhone(digits);
        if (formatted.contentEquals(editable)) {
            return;
        }

        updating = true;
        editText.setText(formatted);
        editText.setSelection(formatted.length());
        editText.setError(null);
        updating = false;
    }

    static String formatPhone(String digits) {
        if (digits.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        builder.append(digits, 0, Math.min(3, digits.length()));

        if (digits.length() > 3) {
            builder.append('-').append(digits, 3, Math.min(7, digits.length()));
        }
        if (digits.length() > 7) {
            builder.append('-').append(digits.substring(7));
        }

        return builder.toString();
    }
}
