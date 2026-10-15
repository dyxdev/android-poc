package com.venuenexa.pocmux.feature.home

import androidx.annotation.StringRes
import com.venuenexa.pocmux.core.model.VenueCategory
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

// Los precios de ejemplo son en euros por día; el formato (separador, posición del símbolo) sigue al idioma.
internal fun formatPrice(
    amount: Int,
    locale: Locale,
): String =
    NumberFormat
        .getCurrencyInstance(locale)
        .apply {
            currency = Currency.getInstance("EUR")
            maximumFractionDigits = 0
        }.format(amount)

internal fun formatRating(
    rating: Double,
    reviewCount: Int,
    locale: Locale,
): String = String.format(locale, "%.1f (%d)", rating, reviewCount)

@StringRes
internal fun VenueCategory.labelRes(): Int =
    when (this) {
        VenueCategory.WEDDINGS -> R.string.category_weddings
        VenueCategory.CORPORATE -> R.string.category_corporate
        VenueCategory.SHOOTS -> R.string.category_shoots
        VenueCategory.BIRTHDAYS -> R.string.category_birthdays
        VenueCategory.MEETINGS -> R.string.category_meetings
        VenueCategory.POPUPS -> R.string.category_popups
    }
