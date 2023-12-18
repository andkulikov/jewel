package org.jetbrains.jewel.foundation.lazy.tree

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.jetbrains.jewel.foundation.utils.Log

@Composable
public fun rememberTreeState(
): TreeState = remember { TreeState() }

public class TreeState() : ScrollableState {

    internal val lazyListState = LazyListState()

    override val canScrollBackward: Boolean
        get() = lazyListState.canScrollBackward
    override val canScrollForward: Boolean
        get() = lazyListState.canScrollForward
    override val isScrollInProgress: Boolean
        get() = lazyListState.isScrollInProgress

    override fun dispatchRawDelta(delta: Float): Float = lazyListState.dispatchRawDelta(delta)

    override suspend fun scroll(scrollPriority: MutatePriority, block: suspend ScrollScope.() -> Unit) {
        lazyListState.scroll(scrollPriority, block)
    }

    internal val allNodes = mutableStateListOf<Pair<Any, Int>>()

    public var openNodes: Set<Any> by mutableStateOf<Set<Any>>(emptySet())

    public fun toggleNode(nodeId: Any) {
        Log.d("toggleNode $nodeId")
        if (nodeId in openNodes) {
            openNodes -= nodeId
        } else {
            openNodes += nodeId
        }
        Log.d("open nodes ${openNodes.map { it.toString() }}")
    }

    public fun openNodes(nodes: List<Any>) {
        openNodes += nodes
    }
}
