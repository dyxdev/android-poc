package com.venuenexa.pocmux.core.context

import androidx.annotation.StringRes

@StringRes
fun UserContext.titleRes(): Int =
    when (this) {
        UserContext.GUEST -> R.string.context_guest_title
        UserContext.OWNER -> R.string.context_owner_title
    }

@StringRes
internal fun UserContext.descriptionRes(): Int =
    when (this) {
        UserContext.GUEST -> R.string.context_guest_description
        UserContext.OWNER -> R.string.context_owner_description
    }
