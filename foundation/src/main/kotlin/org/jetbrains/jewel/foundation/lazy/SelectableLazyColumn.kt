package org.jetbrains.jewel.foundation.lazy

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.jewel.foundation.lazy.SelectableLazyListScopeContainer.Entry
import org.jetbrains.jewel.foundation.lazy.tree.DefaultSelectableLazyColumnEventAction
import org.jetbrains.jewel.foundation.lazy.tree.DefaultSelectableLazyColumnKeyActions
import org.jetbrains.jewel.foundation.lazy.tree.KeyActions
import org.jetbrains.jewel.foundation.lazy.tree.PointerEventActions

public fun Modifier.onPreviewKeyEvent(
    keys: List<SelectableLazyListKey>,
    keyActions: KeyActions = DefaultSelectableLazyColumnKeyActions,
    selectionMode: SelectionMode = SelectionMode.Multiple,
    selectionManager: SelectionManager,
    onActionHandledForIndex: (Int) -> Unit,
): Modifier = onPreviewKeyEvent { event ->
    if (selectionManager.lastActiveItemIndex != null) {
        val actionHandled =
            keyActions.handleOnKeyEvent(event, keys, selectionManager, selectionMode)
                .invoke(event)
        if (actionHandled) {
            onActionHandledForIndex(selectionManager.lastActiveItemIndex!!)
        }
    }
    true
}

public class ItemWrappingState(
    internal val interactionSource: MutableInteractionSource = MutableInteractionSource()
) {
    internal var isFocused: Boolean by mutableStateOf(false)
    internal val focusRequester: FocusRequester = FocusRequester()
}

public fun Modifier.wrappingContainer(state: ItemWrappingState): Modifier =
    onFocusChanged { state.isFocused = it.hasFocus }
        .focusRequester(state.focusRequester)
        .focusable(interactionSource = state.interactionSource)

/**
 * A composable that displays a scrollable and selectable list of items in
 * a column arrangement.
 */
@Composable
public fun SelectableLazyColumn(
    modifier: Modifier = Modifier,
    selectionMode: SelectionMode = SelectionMode.Multiple,
    state: SelectableLazyListState = rememberSelectableLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    // we are usually not having such callbacks in the api. as you done in the implementation, users
    // can always do the same - collect a snapshotFlow if they need those values.
    onSelectedIndexesChanged: (List<Int>) -> Unit = {},
    verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    keyActions: KeyActions = DefaultSelectableLazyColumnKeyActions,
    pointerEventActions: PointerEventActions = DefaultSelectableLazyColumnEventAction(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: SelectableLazyListScope.() -> Unit,
) {
    val container = SelectableLazyListScopeContainer().apply(content)

    val keys = remember(container) { container.getKeys() }
    val wrappingState = remember(interactionSource) { ItemWrappingState(interactionSource) }

    val latestOnSelectedIndexesChanged = rememberUpdatedState(onSelectedIndexesChanged)
    LaunchedEffect(state, container) {
        snapshotFlow { state.selectionManager.selectedKeys }.collect { selectedKeys ->
            val indices = selectedKeys.mapNotNull { key -> container.getKeyIndex(key) }
            latestOnSelectedIndexesChanged.value.invoke(indices)
        }
    }
    val scope = rememberCoroutineScope()
    LazyColumn(
        modifier = modifier.wrappingContainer(wrappingState)
            .onPreviewKeyEvent(
                keys = keys,
                keyActions = keyActions,
                selectionMode = selectionMode,
                selectionManager = state.selectionManager,
                onActionHandledForIndex = {
                    scope.launch { state.lazyListState.scrollToItem(it) }
                }
            ),
        state = state.lazyListState,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        flingBehavior = flingBehavior,
    ) {
        container.getEntries().forEach { entry ->
            appendEntry(
                entry,
                state,
                wrappingState,
                keys,
                keyActions,
                pointerEventActions,
                selectionMode,
                container::isKeySelectable,
            )
        }
    }
}

@Composable
public fun WrapSelectableItem(
    key: Any,
    selectionManager: SelectionManager,
    wrappingState: ItemWrappingState,
    keys: List<SelectableLazyListKey>,
    pointerEventActions: PointerEventActions,
    keyActions: KeyActions = DefaultSelectableLazyColumnKeyActions,
    selectionMode: SelectionMode = SelectionMode.Multiple,
    content: @Composable SelectableLazyItemScope.() -> Unit,
) {
    val itemScope =
        SelectableLazyItemScope(
            isSelected = key in selectionManager.selectedKeys,
            isActive = wrappingState.isFocused,
        )
    Box(
        modifier = Modifier.selectable(
            requester = wrappingState.focusRequester,
            keybindings = keyActions.keybindings,
            actionHandler = pointerEventActions,
            selectionMode = selectionMode,
            selectionManager = selectionManager,
            allKeys = keys,
            itemKey = key,
        ),
    ) {
        content.invoke(itemScope)
    }
}

private fun LazyListScope.appendEntry(
    entry: Entry,
    state: SelectableLazyListState,
    wrappingState: ItemWrappingState,
    keys: List<SelectableLazyListKey>,
    keyActions: KeyActions,
    pointerEventActions: PointerEventActions,
    selectionMode: SelectionMode,
    isKeySelectable: (Any) -> Boolean,
) {
    when (entry) {
        is Entry.Item -> item(entry.key, entry.contentType) {
            if (isKeySelectable(entry.key)) {
                WrapSelectableItem(
                    key = entry.key,
                    selectionManager = state.selectionManager,
                    wrappingState = wrappingState,
                    keys = keys,
                    selectionMode = selectionMode,
                    keyActions = keyActions,
                    pointerEventActions = pointerEventActions,
                    content = entry.content
                )
            } else {
                val itemScope =
                    SelectableLazyItemScope(
                        isSelected = entry.key in state.selectionManager.selectedKeys,
                        isActive = wrappingState.isFocused,
                    )
                entry.content.invoke(itemScope)
            }
        }

        is Entry.Items -> items(
            count = entry.count,
            key = { entry.key(it) },
            contentType = { entry.contentType(it) },
        ) { index ->
            val key = remember(entry, index) { entry.key(index) }
            if (isKeySelectable(key)) {
                WrapSelectableItem(
                    key = entry.key(index),
                    selectionManager = state.selectionManager,
                    wrappingState = wrappingState,
                    keys = keys,
                    selectionMode = selectionMode,
                    keyActions = keyActions,
                    pointerEventActions = pointerEventActions,
                    content = {
                        entry.itemContent.invoke(this, index)
                    }
                )
            } else {
                val itemScope = SelectableLazyItemScope(key in state.selectionManager.selectedKeys, wrappingState.isFocused)
                entry.itemContent.invoke(itemScope, index)
            }
        }

        is Entry.StickyHeader -> stickyHeader(entry.key, entry.contentType) {
            if (isKeySelectable(entry.key)) {
                WrapSelectableItem(
                    key = entry.key,
                    selectionManager = state.selectionManager,
                    wrappingState = wrappingState,
                    keys = keys,
                    selectionMode = selectionMode,
                    keyActions = keyActions,
                    pointerEventActions = pointerEventActions,
                    content = entry.content
                )
            } else {
                SelectableLazyItemScope(entry.key in state.selectionManager.selectedKeys, wrappingState.isFocused)
                    .apply { entry.content.invoke(this) }
            }
        }
    }
}

private fun Modifier.selectable(
    requester: FocusRequester? = null,
    keybindings: SelectableColumnKeybindings,
    actionHandler: PointerEventActions,
    selectionMode: SelectionMode,
    selectionManager: SelectionManager,
    allKeys: List<SelectableLazyListKey>,
    itemKey: Any,
) =
    pointerInput(allKeys, itemKey) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                when (event.type) {
                    PointerEventType.Press -> {
                        requester?.requestFocus()
                        actionHandler.handlePointerEventPress(
                            pointerEvent = event,
                            keyBindings = keybindings,
                            selectionManager = selectionManager,
                            selectionMode = selectionMode,
                            allKeys = allKeys,
                            key = itemKey,
                        )
                    }
                }
            }
        }
    }
