package org.jetbrains.jewel.foundation.lazy.tree

import org.jetbrains.jewel.foundation.lazy.SelectableColumnOnKeyEvent
import org.jetbrains.jewel.foundation.lazy.SelectableLazyListKey
import org.jetbrains.jewel.foundation.lazy.SelectableLazyListState
import org.jetbrains.jewel.foundation.lazy.SelectionManager

public interface TreeViewOnKeyEvent : SelectableColumnOnKeyEvent {

    /**
     * Select Parent Node.
     */
    public fun onSelectParent(keys: List<SelectableLazyListKey>, selectionManager: SelectionManager)

    /**
     * Select Child Node inherited from Right.
     */
    public fun onSelectChild(keys: List<SelectableLazyListKey>, selectionManager: SelectionManager)
}
