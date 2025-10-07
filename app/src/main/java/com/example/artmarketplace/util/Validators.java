package com.example.artmarketplace.util;

import android.text.TextUtils;
import android.util.Patterns;

import androidx.annotation.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Common validation helpers shared across customer and provider flows.
 */
public final class Validators {

    private Validators() {
        // No instances.
    }

    public static boolean isNonEmpty(@Nullable CharSequence value) {
        if (value == null) {
            return false;
        }
        return !TextUtils.isEmpty(value.toString().trim());
    }

    @Nullable
    public static Integer parsePriceToCents(@Nullable CharSequence value) {
        if (!isNonEmpty(value)) {
            return null;
        }
        try {
            BigDecimal decimal = new BigDecimal(value.toString().trim());
            if (decimal.compareTo(BigDecimal.ZERO) < 0) {
                return null;
            }
            BigDecimal scaled = decimal.setScale(2, RoundingMode.HALF_UP);
            return scaled.multiply(BigDecimal.valueOf(100)).intValue();
        } catch (NumberFormatException | ArithmeticException ex) {
            return null;
        }
    }

    public static boolean isValidUrl(@Nullable CharSequence value) {
        if (!isNonEmpty(value)) {
            return true;
        }
        return Patterns.WEB_URL.matcher(value.toString().trim()).matches();
    }
}
