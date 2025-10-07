package com.example.artmarketplace.util;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility formatters for display text shown in the UI.
 */
public final class Formatters {

    private Formatters() {
        // Utility.
    }

    @NonNull
    public static String formatPrice(int priceCents) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
        return format.format(priceCents / 100.0);
    }

    @NonNull
    public static String formatStatus(@NonNull String status) {
        if (TextUtils.isEmpty(status)) {
            return "";
        }
        String normalized = status.replace('_', ' ').toLowerCase(Locale.getDefault());
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }
}
