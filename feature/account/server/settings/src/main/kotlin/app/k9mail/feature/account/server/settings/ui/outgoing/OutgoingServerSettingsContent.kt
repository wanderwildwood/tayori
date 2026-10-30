package app.k9mail.feature.account.server.settings.ui.outgoing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import app.k9mail.feature.account.common.domain.entity.InteractionMode
import app.k9mail.feature.account.server.settings.ui.outgoing.OutgoingServerSettingsContract.Event
import app.k9mail.feature.account.server.settings.ui.outgoing.OutgoingServerSettingsContract.State
import app.k9mail.feature.account.server.settings.ui.outgoing.content.outgoingFormItems
import net.thunderbird.components.ui.bolt.template.ResponsiveWidthContainer
import net.thunderbird.components.ui.bolt.theme.BoltTheme

@Composable
internal fun OutgoingServerSettingsContent(
    mode: InteractionMode,
    state: State,
    onEvent: (Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val resources = LocalResources.current
    val listState = rememberLazyListState()
    val errorIndex = when {
        state.server.error != null -> 1
        state.port.error != null -> 3
        state.isUsernameFieldVisible && state.username.error != null -> 5
        state.isPasswordFieldVisible && state.password.error != null -> if (state.isUsernameFieldVisible) 6 else 5
        else -> null
    }
    // A failed Next leaves the first field with a problem in view, whatever the keyboard leaves of the screen.
    LaunchedEffect(state.server.error, state.port.error, state.username.error, state.password.error) {
        errorIndex?.let { listState.scrollToItem(it) }
    }

    ResponsiveWidthContainer(
        modifier = Modifier.testTag("OutgoingServerSettingsContent")
            .then(modifier),
    ) { contentPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(BoltTheme.spacings.default),
        ) {
            outgoingFormItems(
                mode = mode,
                state = state,
                onEvent = onEvent,
                resources = resources,
            )
        }
    }
}
