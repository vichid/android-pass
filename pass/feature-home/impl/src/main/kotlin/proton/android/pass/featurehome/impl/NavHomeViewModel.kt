package proton.android.pass.featurehome.impl

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import proton.android.pass.biometry.BiometryAuthTimeHolder
import proton.android.pass.biometry.NeedsAuthChecker
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.asResultWithoutLoading
import proton.android.pass.common.api.some
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import javax.inject.Inject

@HiltViewModel
class NavHomeViewModel @Inject constructor(
    private val clock: Clock,
    preferenceRepository: UserPreferencesRepository,
    authTimeHolder: BiometryAuthTimeHolder
) : ViewModel() {

    private val shouldAuthenticateState: Flow<Boolean> = combine(
        preferenceRepository.getBiometricLockState(),
        preferenceRepository.getHasAuthenticated(),
        preferenceRepository.getAppLockPreference(),
        authTimeHolder.getBiometryAuthTime()
    ) { biometricLock, hasAuthenticated, appLock, lastUnlockTime ->

        NeedsAuthChecker.needsAuth(
            biometricLock = biometricLock,
            hasAuthenticated = hasAuthenticated,
            appLockPreference = appLock,
            lastUnlockTime = lastUnlockTime,
            now = clock.now()
        )
    }

    val navHomeUiState: SharedFlow<NavHomeUiState> = combine(
        shouldAuthenticateState.asResultWithoutLoading(),
        preferenceRepository.getHasCompletedOnBoarding().asResultWithoutLoading()
    ) { shouldAuthenticate, hasCompletedOnBoarding ->
        NavHomeUiState(
            shouldAuthenticate = when (shouldAuthenticate) {
                is LoadingResult.Success -> shouldAuthenticate.data.some()
                else -> None
            },
            hasCompletedOnBoarding = when (hasCompletedOnBoarding) {
                is LoadingResult.Success -> hasCompletedOnBoarding.data.value().some()
                else -> None
            }
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NavHomeUiState.Initial
        )
}

@Immutable
data class NavHomeUiState(
    val shouldAuthenticate: Option<Boolean>,
    val hasCompletedOnBoarding: Option<Boolean>
) {
    companion object {
        val Initial = NavHomeUiState(None, None)
    }
}
