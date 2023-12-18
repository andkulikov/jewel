package org.jetbrains.jewel.foundation.lazy.tree

import org.jetbrains.jewel.foundation.lazy.SelectableLazyListKey
import org.jetbrains.jewel.foundation.lazy.SelectionManager

public open class DefaultTreeViewOnKeyEvent(
    override val keybindings: TreeViewKeybindings,
    private val treeState: TreeState,
) : TreeViewOnKeyEvent {

    override fun onSelectParent(keys: List<SelectableLazyListKey>, selectionManager: SelectionManager) {
        val currentKey = keys[selectionManager.lastActiveItemIndex ?: 0].key
        val keyNodeList = treeState.allNodes.map { it.first }

        if (currentKey !in keyNodeList) {
            handleLeafCase(keys, currentKey, keyNodeList, selectionManager)
        } else {
            handleNodeCase(currentKey, keys, selectionManager)
        }
    }

    private fun handleNodeCase(
        currentKey: Any,
        keys: List<SelectableLazyListKey>,
        selectionManager: SelectionManager,
    ) {
        if (treeState.openNodes.contains(currentKey)) {
            treeState.toggleNode(currentKey)
            return
        }
        treeState.allNodes
            .first { it.first == currentKey }
            .let { currentNode ->
                treeState.allNodes
                    .subList(0, treeState.allNodes.indexOf(currentNode))
                    .reversed()
                    .firstOrNull { it.second < currentNode.second }
                    ?.let { (parentNodeKey, _) ->
                        keys.first { it.key == parentNodeKey }
                            .takeIf { it is SelectableLazyListKey.Selectable }
                            ?.let {
                                selectionManager.lastActiveItemIndex =
                                    keys.indexOfFirst { selectableKey -> selectableKey.key == parentNodeKey }
                                selectionManager.selectedKeys = listOf(parentNodeKey)
                            }
                    }
            }
    }

    private fun handleLeafCase(
        keys: List<SelectableLazyListKey>,
        currentKey: Any,
        keyNodeList: List<Any>,
        selectionManager: SelectionManager,
    ) {
        val index = keys.indexOf(currentKey)
        if (index < 0) return
        for (i in index downTo 0) {
            if (keys[i].key in keyNodeList) {
                if (keys[i] is SelectableLazyListKey.Selectable) {
                    selectionManager.lastActiveItemIndex = i
                    selectionManager.selectedKeys = listOf(keys[i].key)
                }
                break
            }
        }
    }

    override fun onSelectChild(keys: List<SelectableLazyListKey>, selectionManager: SelectionManager) {
        val currentKey = keys[selectionManager.lastActiveItemIndex ?: 0].key
        if (
            currentKey in treeState.allNodes.map { it.first } &&
            currentKey !in treeState.openNodes
        ) {
            treeState.toggleNode(currentKey)
        } else {
            super.onSelectNextItem(keys, selectionManager)
        }
    }
}
