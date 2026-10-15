package com.venuenexa.pocmux.feature.home

import androidx.annotation.StringRes
import com.venuenexa.pocmux.core.model.VenueCategory

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
