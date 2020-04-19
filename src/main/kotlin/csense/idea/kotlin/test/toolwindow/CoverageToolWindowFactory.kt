package csense.idea.kotlin.test.toolwindow

import com.intellij.openapi.project.*
import com.intellij.openapi.wm.*
import com.intellij.ui.content.*


class CoverageToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = CoverageToolWindowWrapper(project)
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content: Content = contentFactory.createContent(myToolWindow.content, "", false)
        toolWindow.contentManager.addContent(content)
    }
}