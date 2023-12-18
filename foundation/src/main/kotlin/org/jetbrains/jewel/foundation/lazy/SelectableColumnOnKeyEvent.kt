package org.jetbrains.jewel.foundation.lazy

import org.jetbrains.jewel.foundation.lazy.SelectableLazyListKey.Selectable
import kotlin.math.max
import kotlin.math.min

public interface SelectableColumnOnKeyEvent {

    public val keybindings: SelectableColumnKeybindings

    /**
     * Select First Node.
     */
    public fun onSelectFirstItem(
        allKeys: List<SelectableLazyListKey>,
        selectionManager: SelectionManager,
    ) {
        for (index in allKeys.indices) {
            val key = allKeys[index]
            if (key is Selectable) {
                selectionManager.selectedKeys = listOf(key.key)
                selectionManager.lastActiveItemIndex = index
                return
            }
        }
    }

    /**
     * Extend Selection to First Node inherited from Move Caret to Text Start
     * with Selection.
     */
    public fun onExtendSelectionToFirst(
        keys: List<SelectableLazyListKey>,
        selectionManager: SelectionManager,
    ) {
        val initialIndex = selectionManager.lastActiveItemIndex ?: return
        val newSelection = ArrayList<Any>(max(initialIndex, selectionManager.selectedKeys.size)).apply {
            addAll(selectionManager.selectedKeys)
        }
        var lastActiveItemIndex = initialIndex
        for (index in initialIndex - 1 downTo 0) {
            val key = keys[index]
            if (key is Selectable) {
                newSelection.add(key.key)
                lastActiveItemIndex = index
            }
        }
        selectionManager.lastActiveItemIndex = lastActiveItemIndex
        selectionManager.selectedKeys = newSelection
    }

    /**
     * Select Last Node inherited from Move Caret to Text End.
     */
    public fun onSelectLastItem(
        keys: List<SelectableLazyListKey>,
        selectionManager: SelectionManager,
    ) {
        for (index in keys.lastIndex downTo 0) {
            val key = keys[index]
            if (key is Selectable) {
                selectionManager.selectedKeys = listOf(key.key)
                selectionManager.lastActiveItemIndex = index
                return
            }
        }
    }

    /**
     * Extend Selection to Last Node inherited from Move Caret to Text End with
     * Selection.
     */
    public fun onExtendSelectionToLastItem(
        keys: List<SelectableLazyListKey>,
        selectionManager: SelectionManager,
    ) {
        val initialIndex = selectionManager.lastActiveItemIndex ?: return
        val newSelection = ArrayList<Any>(max(keys.size - initialIndex, selectionManager.selectedKeys.size)).apply {
            addAll(selectionManager.selectedKeys)
        }
        var lastActiveItemIndex = initialIndex
        for (index in initialIndex + 1..keys.lastIndex) {
            val key = keys[index]
            if (key is Selectable) {
                newSelection.add(key.key)
                lastActiveItemIndex = index
            }
        }
        selectionManager.lastActiveItemIndex = lastActiveItemIndex
        selectionManager.selectedKeys = newSelection
    }

    /**
     * Select Previous Node inherited from Up.
     */
    public fun onSelectPreviousItem(
        keys: List<SelectableLazyListKey>,
        selectionManager: SelectionManager,
    ) {
        val initialIndex = selectionManager.lastActiveItemIndex ?: return
        for (index in initialIndex - 1 downTo 0) {
            val key = keys[index]
            if (key is Selectable) {
                selectionManager.selectedKeys = listOf(key.key)
                selectionManager.lastActiveItemIndex = index
                return
            }
        }
    }

    /**
     * Extend Selection with Previous Node inherited from Up with Selection.
     */
    public fun onExtendSelectionWithPreviousItem(
        keys: List<SelectableLazyListKey>,
        selectionManager: SelectionManager,
    ) {
        // todo we need deselect if we are changing direction
        val initialIndex = selectionManager.lastActiveItemIndex ?: return
        for (index in initialIndex - 1 downTo 0) {
            val key = keys[index]
            if (key is Selectable) {
                selectionManager.selectedKeys += key.key
                selectionManager.lastActiveItemIndex = index
                return
            }
        }
    }

    /**
     * Select Next Node inherited from Down.
     */
    public fun onSelectNextItem(
        keys: List<SelectableLazyListKey>,
        selectionManager: SelectionManager,
    ) {
        val initialIndex = selectionManager.lastActiveItemIndex ?: return
        for (index in initialIndex + 1..keys.lastIndex) {
            val key = keys[index]
            if (key is Selectable) {
                selectionManager.selectedKeys = listOf(key.key)
                selectionManager.lastActiveItemIndex = index
                return
            }
        }
    }

    /**
     * Extend Selection with Next Node inherited from Down with Selection.
     */
    public fun onExtendSelectionWithNextItem(
        keys: List<SelectableLazyListKey>,
        selectionManager: SelectionManager,
    ) {
        // todo we need deselect if we are changing direction
        val initialIndex = selectionManager.lastActiveItemIndex ?: return
        for (index in initialIndex + 1..keys.lastIndex) {
            val key = keys[index]
            if (key is Selectable) {
                selectionManager.selectedKeys += key.key
                selectionManager.lastActiveItemIndex = index
                return
            }
        }
    }

    /**
     * Scroll Page Up and Select Node inherited from Page Up.
     */
    public fun onScrollPageUpAndSelectItem(
        keys: List<SelectableLazyListKey>,
        selectionManager: SelectionManager,
    ) {
        val visibleSize = selectionManager.visibleSize
        val targetIndex = max((selectionManager.lastActiveItemIndex ?: 0) - visibleSize, 0)
        selectionManager.selectedKeys = listOf(keys[targetIndex].key)
        selectionManager.lastActiveItemIndex = targetIndex
    }

    /**
     * Scroll Page Up and Extend Selection inherited from Page Up with
     * Selection.
     */
    public fun onScrollPageUpAndExtendSelection(
        keys: List<SelectableLazyListKey>,
        selectionManager: SelectionManager,
    ) {
        val visibleSize = selectionManager.visibleSize
        val targetIndex = max((selectionManager.lastActiveItemIndex ?: 0) - visibleSize, 0)
        val newSelectionList =
            keys
                .subList(targetIndex, (selectionManager.lastActiveItemIndex ?: 0))
                .withIndex()
                .filter { it.value is Selectable }
                .let { selectionManager.selectedKeys + it.map { selectableKey -> selectableKey.value.key } }
        selectionManager.selectedKeys = newSelectionList
        selectionManager.lastActiveItemIndex = targetIndex
    }

    /**
     * Scroll Page Down and Select Node inherited from Page Down.
     */
    public fun onScrollPageDownAndSelectItem(
        keys: List<SelectableLazyListKey>,
        selectionManager: SelectionManager,
    ) {
        val visibleSize = selectionManager.visibleSize
        val targetIndex = min((selectionManager.lastActiveItemIndex ?: 0) + visibleSize, keys.lastIndex)
        selectionManager.selectedKeys = listOf(keys[targetIndex].key)
        selectionManager.lastActiveItemIndex = targetIndex
    }

    /**
     * Scroll Page Down and Extend Selection inherited from Page Down with
     * Selection.
     */
    public fun onScrollPageDownAndExtendSelection(
        keys: List<SelectableLazyListKey>,
        selectionManager: SelectionManager,
    ) {
        val visibleSize = selectionManager.visibleSize
        val targetIndex = min((selectionManager.lastActiveItemIndex ?: 0) + visibleSize, keys.lastIndex)
        val newSelectionList =
            keys.subList(selectionManager.lastActiveItemIndex ?: 0, targetIndex)
                .filterIsInstance<Selectable>()
                .let { selectionManager.selectedKeys + it.map { selectableKey -> selectableKey.key } }
        selectionManager.selectedKeys = newSelectionList
        selectionManager.lastActiveItemIndex = targetIndex
    }

    /**
     * Edit Item.
     */
    public fun onEdit() {
        // IntelliJ focuses the first element with an issue when this is pressed.
        // It is thus unavailable here.
    }

    /**
     * Select All.
     */
    public fun onSelectAll(keys: List<SelectableLazyListKey>, selectionManager: SelectionManager) {
        selectionManager.selectedKeys = keys.filterIsInstance<Selectable>().map { it.key }
    }
}

public open class DefaultSelectableOnKeyEvent(
    override val keybindings: SelectableColumnKeybindings,
) : SelectableColumnOnKeyEvent {

    public companion object : DefaultSelectableOnKeyEvent(DefaultSelectableColumnKeybindings)
}
