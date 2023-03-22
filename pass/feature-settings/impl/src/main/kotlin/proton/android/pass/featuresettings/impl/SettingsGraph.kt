package proton.android.pass.featuresettings.impl

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable

object Settings : NavItem(baseRoute = "settings")
object LogView : NavItem(baseRoute = "log/view")
object ThemeSelector : NavItem(baseRoute = "theme/bottomsheet")
object ClipboardSettings : NavItem(baseRoute = "clipboard/settings/bottomsheet")
object ClearClipboardOptions : NavItem(baseRoute = "clipboard/clearOptions/bottomsheet")

@Suppress("LongParameterList")
@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.settingsGraph(
    onReportProblemClick: () -> Unit,
    onSelectThemeClick: () -> Unit,
    onClipboardClick: () -> Unit,
    onClearClipboardSettingClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onUpClick: () -> Unit,
    onViewLogsClick: () -> Unit,
    dismissBottomSheet: () -> Unit
) {
    composable(Settings) {
        SettingsScreen(
            onLogoutClick = onLogoutClick,
            onReportProblemClick = onReportProblemClick,
            onSelectThemeClick = onSelectThemeClick,
            onUpClick = onUpClick,
            onViewLogsClick = onViewLogsClick,
            onClipboardClick = onClipboardClick
        )
    }

    bottomSheet(ThemeSelector) {
        val viewModel: ThemeSelectorViewModel = hiltViewModel()
        ThemeSelectionBottomSheetContents {
            viewModel.onThemePreferenceChange(it)
            dismissBottomSheet()
        }
    }

    composable(LogView) {
        LogViewScreen(onUpClick = onUpClick)
    }

    bottomSheet(ClipboardSettings) {
        ClipboardBottomSheet(
            onClearClipboardSettingClick = onClearClipboardSettingClick
        )
    }

    bottomSheet(ClearClipboardOptions) {
        ClearClipboardOptionsBottomSheet(onSuccess = dismissBottomSheet)
    }
}
