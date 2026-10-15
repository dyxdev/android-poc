package com.venuenexa.pocmux.core.designsystem

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

// Los precios de ejemplo son en euros por día; el formato (separador, posición del símbolo) sigue al idioma.
fun formatPrice(
    amount: Int,
    locale: Locale,
): String =
    NumberFormat
        .getCurrencyInstance(locale)
        .apply {
            currency = Currency.getInstance("EUR")
            maximumFractionDigits = 0
        }.format(amount)

fun formatRating(
    rating: Double,
    reviewCount: Int,
    locale: Locale,
): String = String.format(locale, "%.1f (%d)", rating, reviewCount)
