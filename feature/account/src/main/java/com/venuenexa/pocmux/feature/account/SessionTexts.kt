package com.venuenexa.pocmux.feature.account

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.venuenexa.pocmux.core.identity.IdentityError
import com.venuenexa.pocmux.core.identity.IdentityState
import com.venuenexa.pocmux.core.identity.IdentityStatus
import java.text.DateFormat
import java.util.Date

@Composable
internal fun sessionSubtitle(identity: IdentityState): String {
    if (identity.status != IdentityStatus.SIGNED_IN) return stringResource(R.string.account_guest_subtitle)
    val expiresAtMs = identity.expiresAtMs ?: return stringResource(R.string.account_signed_in_active)
    return stringResource(
        R.string.account_signed_in_until,
        DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(expiresAtMs)),
    )
}

@StringRes
internal fun errorLabel(error: IdentityError): Int =
    when (error) {
        IdentityError.NOT_CONFIGURED -> R.string.account_error_not_configured
        IdentityError.CANCELED -> R.string.account_error_canceled
        IdentityError.SESSION_EXPIRED -> R.string.account_error_session_expired
        IdentityError.NETWORK -> R.string.account_error_network
        IdentityError.FAILED -> R.string.account_error_failed
    }
