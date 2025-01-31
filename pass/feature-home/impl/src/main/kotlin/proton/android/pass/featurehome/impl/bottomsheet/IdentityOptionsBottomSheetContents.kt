/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.featurehome.impl.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemAction
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemRow
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemSubtitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.pin
import proton.android.pass.composecomponents.impl.bottomsheet.unpin
import proton.android.pass.composecomponents.impl.bottomsheet.viewHistory
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.composecomponents.impl.item.icon.IdentityIcon
import proton.android.pass.domain.AddressDetailsContent
import proton.android.pass.domain.ContactDetailsContent
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.PersonalDetailsContent
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.WorkDetailsContent
import proton.android.pass.featurehome.impl.R

@ExperimentalMaterialApi
@Composable
fun IdentityOptionsBottomSheetContents(
    modifier: Modifier = Modifier,
    itemUiModel: ItemUiModel,
    action: BottomSheetItemAction,
    isRecentSearch: Boolean = false,
    onCopyFullName: (String) -> Unit,
    onPinned: (ShareId, ItemId) -> Unit,
    onUnpinned: (ShareId, ItemId) -> Unit,
    onViewHistory: (ShareId, ItemId) -> Unit,
    onEdit: (ShareId, ItemId) -> Unit,
    onMoveToTrash: (ItemUiModel) -> Unit,
    onRemoveFromRecentSearch: (ShareId, ItemId) -> Unit,
    isFreePlan: Boolean
) {
    val contents = itemUiModel.contents as ItemContents.Identity

    Column(modifier.bottomSheet()) {
        BottomSheetItemRow(
            title = { BottomSheetItemTitle(text = contents.title) },
            subtitle = {
                if (contents.personalDetailsContent.hasFullName) {
                    BottomSheetItemSubtitle(text = contents.personalDetailsContent.fullName)
                }
            },
            leftIcon = { IdentityIcon() }
        )

        val bottomSheetItems = mutableListOf(
            copyFullName(contents.personalDetailsContent.fullName, onCopyFullName)
        ).apply {
            if (itemUiModel.isPinned) {
                add(unpin(action) { onUnpinned(itemUiModel.shareId, itemUiModel.id) })
            } else {
                add(pin(action) { onPinned(itemUiModel.shareId, itemUiModel.id) })
            }

            add(viewHistory(isFreePlan) { onViewHistory(itemUiModel.shareId, itemUiModel.id) })

            if (itemUiModel.canModify) {
                add(edit(itemUiModel, onEdit))
                add(moveToTrash(itemUiModel, onMoveToTrash))
            }

            if (isRecentSearch) {
                add(removeFromRecentSearch(itemUiModel, onRemoveFromRecentSearch))
            }
        }

        BottomSheetItemList(
            items = bottomSheetItems.withDividers().toPersistentList()
        )
    }
}

private fun copyFullName(fullName: String, onFullName: (String) -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(text = stringResource(id = R.string.bottomsheet_copy_full_name)) }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: (@Composable () -> Unit)
        get() = { BottomSheetItemIcon(iconId = R.drawable.ic_squares) }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: () -> Unit
        get() = { onFullName(fullName) }
    override val isDivider = false
}

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
internal fun IdentityOptionsBSContentsPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            IdentityOptionsBottomSheetContents(
                itemUiModel = ItemUiModel(
                    id = ItemId(id = ""),
                    userId = UserId(id = "user-id"),
                    shareId = ShareId(id = ""),
                    contents = ItemContents.Identity(
                        title = "My Identity",
                        note = "Note content",
                        personalDetailsContent = PersonalDetailsContent.EMPTY,
                        addressDetailsContent = AddressDetailsContent.EMPTY,
                        contactDetailsContent = ContactDetailsContent.EMPTY,
                        workDetailsContent = WorkDetailsContent.EMPTY,
                        extraSectionContentList = emptyList()
                    ),
                    state = 0,
                    createTime = Clock.System.now(),
                    modificationTime = Clock.System.now(),
                    lastAutofillTime = Clock.System.now(),
                    isPinned = false,
                    revision = 1
                ),
                isRecentSearch = input.second,
                action = BottomSheetItemAction.None,
                onCopyFullName = {},
                onPinned = { _, _ -> },
                onUnpinned = { _, _ -> },
                onViewHistory = { _, _ -> },
                onEdit = { _, _ -> },
                onMoveToTrash = {},
                onRemoveFromRecentSearch = { _, _ -> },
                isFreePlan = input.second
            )
        }
    }
}
