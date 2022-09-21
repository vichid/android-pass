package me.proton.android.pass.ui.create.login

import androidx.compose.runtime.Immutable
import me.proton.android.pass.ui.shared.uievents.IsLoadingState
import me.proton.android.pass.ui.shared.uievents.ItemSavedState

@Immutable
data class CreateUpdateLoginUiState(
    val loginItem: LoginItem,
    val errorList: Set<LoginItemValidationErrors>,
    val isLoadingState: IsLoadingState,
    val isItemSaved: ItemSavedState
) {
    companion object {
        val Initial = CreateUpdateLoginUiState(
            isLoadingState = IsLoadingState.NotLoading,
            loginItem = LoginItem.Empty,
            errorList = emptySet(),
            isItemSaved = ItemSavedState.Unknown
        )
    }
}
