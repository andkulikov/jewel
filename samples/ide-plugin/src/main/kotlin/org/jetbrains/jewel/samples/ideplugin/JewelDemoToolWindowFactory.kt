package org.jetbrains.jewel.samples.ideplugin

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.jetbrains.jewel.bridge.addComposeTab
import org.jetbrains.jewel.samples.ideplugin.releasessample.ReleasesSampleCompose
import org.jetbrains.jewel.samples.ideplugin.releasessample.ReleasesSamplePanel

@ExperimentalCoroutinesApi
internal class JewelDemoToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        toolWindow.addComposeTab("Components") {
            ComponentShowcaseTab()
        }

        addSwingTab(toolWindow)

        toolWindow.addComposeTab("Compose Sample") {
            ReleasesSampleCompose(project)
        }
    }

    private fun addSwingTab(toolWindow: ToolWindow) {
        val manager = toolWindow.contentManager
        val tabContent =
            manager.factory.createContent(
                ReleasesSamplePanel(toolWindow.disposable.createCoroutineScope()),
                "Swing Sample",
                true,
            )
        tabContent.isCloseable = false
        manager.addContent(tabContent)
    }
}

@Suppress("InjectDispatcher") // This is likely wrong anyway, it's only for the demo
private fun Disposable.createCoroutineScope(): CoroutineScope {
    val job = SupervisorJob()
    val scopeDisposable = Disposable { job.cancel("Disposing") }
    Disposer.register(this, scopeDisposable)
    return CoroutineScope(job + Dispatchers.Default)
}
