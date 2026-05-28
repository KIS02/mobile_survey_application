package com.example.mobile_survey_application.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class BirthDateTextWatcher implements TextWatcher {

    private static final int MAX_DIGITS = 8;

    private final EditText editText;
    private boolean updating;

    public BirthDateTextWatcher(EditText editText) {
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

        String formatted = formatBirthDate(digits);
        if (formatted.contentEquals(editable)) {
            return;
        }

        updating = true;
        editText.setText(formatted);
        editText.setSelection(formatted.length());
        editText.setError(null);
        updating = false;
    }

    static String formatBirthDate(String digits) {
        if (digits.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        builder.append(digits, 0, Math.min(4, digits.length()));

        if (digits.length() > 4) {
            builder.append('-').append(digits, 4, Math.min(6, digits.length()));
        }
        if (digits.length() > 6) {
            builder.append('-').append(digits.substring(6));
        }

        return builder.toString();
    }
}
