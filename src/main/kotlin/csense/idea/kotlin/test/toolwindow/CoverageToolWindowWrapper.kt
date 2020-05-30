package csense.idea.kotlin.test.toolwindow

import com.intellij.openapi.application.*
import com.intellij.openapi.module.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.impl.*
import com.intellij.psi.search.*
import com.intellij.psi.util.*
import csense.idea.base.bll.psi.*
import csense.idea.kotlin.test.bll.analyzers.*
import csense.kotlin.*
import org.jetbrains.kotlin.idea.refactoring.*
import org.jetbrains.kotlin.idea.util.*
import org.jetbrains.kotlin.idea.util.projectStructure.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*
import org.jetbrains.uast.*
import javax.swing.*
import javax.swing.event.*


class CoverageToolWindowWrapper(project: Project) {
    
    val window = CoverageToolWindow()
    
    val content: JComponent
        get() = window.content
    
    private val navigateSelectionListener = ListSelectionListener {
        if (it.valueIsAdjusting) {
            return@ListSelectionListener
        }
        val source = it.source as? JList<*> ?: return@ListSelectionListener
        val fIndex = source.selectedIndex
        if (fIndex >= 0 && fIndex < source.model.size) {
            val data = source.model.getElementAt(fIndex) as? CoverageListData
            val asClz = data?.psiElement?.findParentOfType<PsiElementBase>()
            if (asClz != null) {
                asClz.navigate(true)
            } else {
                val clz = data?.psiElement?.containingFile?.navigate(true)
            }
            
            
        }
    }
    
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
        window.missingClassesList.addListSelectionListener(navigateSelectionListener)
        window.missingFunctionsList.addListSelectionListener(navigateSelectionListener)
        window.missingPropertiesList.addListSelectionListener(navigateSelectionListener)
    }
    
    
}

data class CoverageListData(val fqName: String, val psiElement: PsiElement) {
    override fun toString(): String {
        return fqName
    }
}

fun CoverageToolWindow.update(result: BackgroundAnalyzeResult) {
    classesTestedLabel.text = "${result.testedClasses}/${result.seenClasses}"
    methodsTestedLabel.text = "${result.testedMethods}/${result.seenMethods}"
    missingClassesList.setListData(result.missingClassFq.map { it.toCoverageListData() }.toTypedArray())
    missingFunctionsList.setListData(result.missingFunctionFq.map { it.toCoverageListData() }.toTypedArray())
    missingPropertiesList.setListData(result.missingPropertyFq.map { it.toCoverageListData() }.toTypedArray())
}

fun PsiElement.toCoverageListData(): CoverageListData {
    return CoverageListData(this.getKotlinFqNameString() ?: "", this)
}

data class BackgroundAnalyzeResult(
        val seenClasses: Int,
        val testedClasses: Int,
        val seenMethods: Int,
        val testedMethods: Int,
        val missingClassFq: List<PsiElement>,
        val missingFunctionFq: List<PsiElement>,
        val missingPropertyFq: List<PsiElement>
)

class BackgroundableWrapper(
        project: Project,
        val module: Module,
        title: String,
        val updateCallback: FunctionUnit<BackgroundAnalyzeResult>
) : Task.Backgroundable(project, title, true, PerformInBackgroundOption.DEAF) {
    
    private var seenClasses = 0
    private var testedClasses = 0
    private var seenMethods = 0
    private var testedMethods = 0
    private var testedProperties = 0
    private var seenProperties = 0
    private var missingClassesFqPsi = mutableListOf<PsiElement>()
    private var missingFunctionFqPsi = mutableListOf<PsiElement>()
    private var missingPropertiesFqPsi = mutableListOf<PsiElement>()
    
    override fun run(indicator: ProgressIndicator) {
        ApplicationManager.getApplication().runReadAction {
            module.sourceRoots.forEach {
                it.visit()
                updateCallback(
                        BackgroundAnalyzeResult(
                                seenClasses, testedClasses, seenMethods, testedMethods,
                                missingClassesFqPsi,
                                missingFunctionFqPsi,
                                missingPropertiesFqPsi))
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
                    missingClassesFqPsi.add(it)//HOW TO MAKE A LINK THING ???!
                }
            }
            ktFile.collectDescendantsOfType<KtNamedFunction>().forEach {
                val analyzeResult = MissingtestsForFunctionAnalyzers.analyze(it)
                seenMethods += 1
                if (analyzeResult.errors.isEmpty()) {
                    testedMethods += 1
                } else {
                    missingFunctionFqPsi.add(it)
                }
            }
            ktFile.collectDescendantsOfType<KtProperty>().forEach {
                val analyzeResult = MissingTestsForPropertyAnalyzer.analyze(it)
                seenProperties += 1
                if (analyzeResult.errors.isEmpty()) {
                    testedProperties += 1
                } else {
                    missingPropertiesFqPsi.add(it)
                }
            }
        } else {
            children.forEach {
                it.visit()
            }
        }
    }
    
}

