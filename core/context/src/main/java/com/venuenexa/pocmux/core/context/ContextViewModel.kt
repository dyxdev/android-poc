package com.venuenexa.pocmux.core.context

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val STOP_TIMEOUT_MS = 5_000L

@HiltViewModel
class ContextViewModel
    @Inject
    constructor(
        private val manager: ContextManager,
    ) : ViewModel() {
        val state: StateFlow<ContextState> =
            manager.state.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ContextState())

        fun select(context: UserContext) {
            viewModelScope.launch { manager.select(context) }
        }

        fun acknowledgeHint() {
            viewModelScope.launch { manager.acknowledgeHint() }
        }
    }
