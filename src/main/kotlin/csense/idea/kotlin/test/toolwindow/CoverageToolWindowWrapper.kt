package csense.idea.kotlin.test.toolwindow

import com.intellij.openapi.application.*
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import csense.idea.kotlin.test.bll.analyzers.*
import csense.kotlin.*
import org.jetbrains.kotlin.idea.refactoring.*
import org.jetbrains.kotlin.idea.util.*
import org.jetbrains.kotlin.idea.util.projectStructure.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*
import javax.swing.*


class CoverageToolWindowWrapper(project: Project) {
    
    val window = CoverageToolWindow()
    
    val content: JComponent
        get() = window.content
    
    init {
        window.refreshButton.addActionListener {
            val selectedModule = project.allModules().getOrNull(window.selectedModule.selectedIndex)
                    ?: return@addActionListener
            try {
                ApplicationManager.getApplication().invokeAndWait(Runnable {
                    val backgroundableWrapper = BackgroundableWrapper(project, selectedModule, "Computing coverage for module ${selectedModule.name}") {
                        SwingUtilities.invokeLater {
                            window.update(it)
                        }
                    }
                    ProgressManager.getInstance().run(backgroundableWrapper)
                }, ModalityState.NON_MODAL)
            } catch (e: Exception) {
            
            }
        }
        project.allModules().forEach {
            window.selectedModule.addItem(it.name)
        }
        
    }
}

fun CoverageToolWindow.update(result: BackgroundAnalyzeResult) {
    classesTestedLabel.text = "${result.testedClasses}/${result.seenClasses}"
    methodsTestedLabel.text = "${result.testedMethods}/${result.seenMethods}"
}

data class BackgroundAnalyzeResult(
        val seenClasses: Int,
        val testedClasses: Int,
        val seenMethods: Int,
        val testedMethods: Int
)

class BackgroundableWrapper(
        project: Project,
        val module: Module,
        title: String,
        val updateCallback: FunctionUnit<BackgroundAnalyzeResult>) : Task.Backgroundable(project, title, true, PerformInBackgroundOption.DEAF) {
    
    private var seenClasses = 0
    private var testedClasses = 0
    private var seenMethods = 0
    private var testedMethods = 0
    private var testedProperties = 0
    private var seenProperties = 0
    private var missingClassesLinks = mutableListOf<String>()
    
    override fun run(indicator: ProgressIndicator) {
        ApplicationManager.getApplication().runReadAction {
            module.sourceRoots.forEach {
                it.visit()
                updateCallback(BackgroundAnalyzeResult(seenClasses, testedClasses, seenMethods, testedMethods))
            }
        }
    }
    
    override fun shouldStartInBackground(): Boolean {
        return true
    }
    
    private fun VirtualFile.visit() {
        if (extension == "kt") {
            val ktFile = this.toPsiFile(project) as? KtFile ?: return
            //open file and analyze it.
            
            ktFile.collectDescendantsOfType<KtClassOrObject>().forEach {
                val analyzeResult = MissingTestsForClassAnalyzer.analyze(it)
                seenClasses += 1
                if (analyzeResult.errors.isEmpty()) {
                    testedClasses += 1
                } else {
                    missingClassesLinks.add(it.name ?: "")//HOW TO MAKE A LINK THING ???!
                }
            }
            ktFile.collectDescendantsOfType<KtNamedFunction>().forEach {
                val analyzeResult = MissingtestsForFunctionAnalyzers.analyze(it)
                seenMethods += 1
                if (analyzeResult.errors.isEmpty()) {
                    testedMethods += 1
                }
            }
            ktFile.collectDescendantsOfType<KtProperty>().forEach {
                val analyzeResult = MissingTestsForPropertyAnalyzer.analyze(it)
                seenProperties += 1
                if (analyzeResult.errors.isEmpty()) {
                    testedProperties += 1
                }
            }
        } else {
            children.forEach {
                it.visit()
            }
        }
    }
    
}

