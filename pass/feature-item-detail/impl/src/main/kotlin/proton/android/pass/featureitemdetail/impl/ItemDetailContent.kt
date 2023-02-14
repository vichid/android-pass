package proton.android.pass.featureitemdetail.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.featureitemdetail.impl.alias.AliasDetail
import proton.android.pass.featureitemdetail.impl.login.LoginDetail
import proton.android.pass.featureitemdetail.impl.note.NoteDetail
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

@Composable
fun ItemDetailContent(
    modifier: Modifier = Modifier,
    uiState: ItemDetailScreenUiState,
    onUpClick: () -> Unit,
    onEditClick: (ShareId, ItemId, ItemType) -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.model != null) {
            val item = uiState.model.item
            when (item.itemType) {
                is ItemType.Login -> LoginDetail(
                    item = item,
                    onUpClick = onUpClick,
                    onEditClick = onEditClick
                )
                is ItemType.Note -> NoteDetail(
                    item = item,
                    onUpClick = onUpClick,
                    onEditClick = onEditClick
                )
                is ItemType.Alias -> AliasDetail(
                    item = item,
                    onUpClick = onUpClick,
                    onEditClick = onEditClick
                )
                ItemType.Password -> {}
            }
        }
    }
}
