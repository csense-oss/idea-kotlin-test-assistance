package csense.idea.kotlin.test.toolwindow

import com.intellij.openapi.application.*
import com.intellij.openapi.module.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.impl.*
import csense.idea.base.bll.kotlin.isAnonymous
import csense.idea.base.bll.psi.*
import csense.idea.base.module.*
import csense.idea.kotlin.test.bll.analyzers.*
import csense.kotlin.*
import csense.kotlin.logger.*
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import org.jetbrains.kotlin.idea.util.*
import org.jetbrains.kotlin.idea.util.projectStructure.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*
import java.text.*
import javax.swing.*
import javax.swing.event.*


class CoverageToolWindowWrapper(project: Project) {

    val window: CoverageToolWindow = CoverageToolWindow()

    val content: JComponent
        get() = window.content

    private var modules = computeModules(project)

    private fun computeModules(project: Project): List<Module> {
        return project.allModules().filterNot {
            it.isTestModule()
        }
    }

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
                data?.psiElement?.containingFile?.navigate(true)
            }


        }
    }

    init {
        window.refreshButton.addActionListener {
            modules = computeModules(project).sortedBy {
                it.name
            }
            calculateCoverageForSelection(project)
        }

        modules.forEach {
            window.selectedModule.addItem(it.name)
        }

        window.selectedModule.addActionListener {
            calculateCoverageForSelection(project)
        }

        window.missingClassesList.addListSelectionListener(navigateSelectionListener)
        window.missingFunctionsList.addListSelectionListener(navigateSelectionListener)
        window.missingPropertiesList.addListSelectionListener(navigateSelectionListener)

        window.skippedClassesList.addListSelectionListener(navigateSelectionListener)
        window.skippedFunctionsList.addListSelectionListener(navigateSelectionListener)
        window.skippedPropertiesList.addListSelectionListener(navigateSelectionListener)
    }

    private fun calculateCoverageForSelection(project: Project) {
        val selectedModule = modules.getOrNull(window.selectedModule.selectedIndex)
            ?: return
        computeForSelected(selectedModule, project)
    }

    private fun computeForSelected(selectedModule: Module, project: Project) {
        try {
            ApplicationManager.getApplication().invokeAndWait({
                val backgroundableWrapper = BackgroundableWrapper(
                    project,
                    selectedModule,
                    "Computing coverage for module ${selectedModule.name}"
                ) {
                    SwingUtilities.invokeLater {
                        window.update(it)
                    }
                }
                ProgressManager.getInstance().run(backgroundableWrapper)
            }, ModalityState.NON_MODAL)
        } catch (e: Exception) {
            L.debug("coverageToolWindow", "Got error while computing coverage", e)
        }
    }

}

data class CoverageListData(val fqName: String, val psiElement: PsiElement) {
    override fun toString(): String {
        return fqName
    }
}

fun CoverageToolWindow.update(result: BackgroundAnalyzeResult) {
    val df = DecimalFormat.getInstance().apply { maximumFractionDigits = 2 }
    val percentagesClasses = (result.testedClasses.toDouble() / result.seenClasses.toDouble()) * 100.0
    val percentagesMethods = (result.testedMethods.toDouble() / result.seenMethods.toDouble()) * 100.0
    val percentagesProperties = (result.testedProperties.toDouble() / result.seenProperties.toDouble()) * 100.0
    classesTestedLabel.text = "${result.testedClasses}/${result.seenClasses} (${df.format(percentagesClasses)}% tested)"
    methodsTestedLabel.text = "${result.testedMethods}/${result.seenMethods} (${df.format(percentagesMethods)}% tested)"
    propertiesTestedLabel.text =
        "${result.testedProperties}/${result.seenProperties} (${df.format(percentagesProperties)}% tested)"

    missingClassesList.setListData(result.missingClassFq.map { it.toCoverageListData() }.toTypedArray())
    missingFunctionsList.setListData(result.missingFunctionFq.map { it.toCoverageListData() }.toTypedArray())
    missingPropertiesList.setListData(result.missingPropertyFq.map { it.toCoverageListData() }.toTypedArray())

    skippedClassesList.setListData(result.skippedClassFq.map { it.toCoverageListData() }.toTypedArray())
    skippedFunctionsList.setListData(result.skippedFunctionFq.map { it.toCoverageListData() }.toTypedArray())
    skippedPropertiesList.setListData(result.skippedPropertyFq.map { it.toCoverageListData() }.toTypedArray())


}

fun PsiElement.toCoverageListData(): CoverageListData {
    return CoverageListData(this.getKotlinFqNameString() ?: "", this)
}

data class BackgroundAnalyzeResult(
    val seenClasses: Int,
    val testedClasses: Int,

    val seenMethods: Int,
    val testedMethods: Int,

    val seenProperties: Int,
    val testedProperties: Int,

    val missingClassFq: List<PsiElement>,
    val missingFunctionFq: List<PsiElement>,
    val missingPropertyFq: List<PsiElement>,

    val skippedClassFq: List<PsiElement>,
    val skippedFunctionFq: List<PsiElement>,
    val skippedPropertyFq: List<PsiElement>
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

    private var skippedClassesFqPsi = mutableListOf<PsiElement>()
    private var skippedFunctionFqPsi = mutableListOf<PsiElement>()
    private var skippedPropertiesFqPsi = mutableListOf<PsiElement>()

    override fun run(indicator: ProgressIndicator) {
        val fileIndex = ProjectFileIndex.SERVICE.getInstance(project)
        if (module.sourceRoots.isEmpty()) {
            //assume its the root project, thus we are to iterate over all "regular" modules
            val otherModules = project.allModules().filterNot {
                it.isTestModule() && it.name != module.name
            }
            otherModules.forEach {
                runOnModule(it, fileIndex)
            }

        } else {
            runOnModule(module, fileIndex)
        }

    }

    private fun runOnModule(module: Module, fileIndex: ProjectFileIndex) {
        ApplicationManager.getApplication().runReadAction {

            module.sourceRoots.forEach {
                it.visit(fileIndex)
                updateCallback(
                    BackgroundAnalyzeResult(
                        seenClasses, testedClasses,
                        seenMethods, testedMethods,
                        seenProperties, testedProperties,
                        missingClassesFqPsi,
                        missingFunctionFqPsi,
                        missingPropertiesFqPsi,
                        skippedClassesFqPsi,
                        skippedFunctionFqPsi,
                        skippedPropertiesFqPsi
                    )
                )
            }
        }
    }

    override fun shouldStartInBackground(): Boolean {
        return true
    }

    private fun VirtualFile.visit(fileIndex: ProjectFileIndex) {
        val isSourceFile = extension == "kt" && fileIndex.isInSource(this) && !fileIndex.isInTestSourceContent(this)
        if (isSourceFile) {
            val ktFile = this.toPsiFile(project) as? KtFile ?: return
            //open file and analyze it.
            if (ktFile.annotationEntries.containsSuppressionForMissingTest()) {
                return
            }

            ktFile.collectDescendantsOfType<KtClassOrObject>().forEach {
                if (it.isAnonymous()) {
                    return@forEach
                }

                if (it.annotationEntries.containsSuppressionForMissingTest()) {
                    seenClasses += 1
                    testedClasses += 1
                    skippedClassesFqPsi.add(it)
                    return@forEach
                }

                val analyzeResult = MissingTestsForClassAnalyzer.analyze(it)
                seenClasses += 1
                if (analyzeResult.errors.isEmpty()) {
                    testedClasses += 1
                } else {
                    missingClassesFqPsi.add(it)
                }
            }
            ktFile.collectDescendantsOfType<KtNamedFunction>().forEach {
                if (it.annotationEntries.containsSuppressionForMissingTest()) {
                    seenMethods += 1
                    testedMethods += 1
                    skippedFunctionFqPsi.add(it)
                    return@forEach
                }
                val analyzeResult = MissingtestsForFunctionAnalyzers.analyze(it, true)
                seenMethods += 1
                if (analyzeResult.errors.isEmpty()) {
                    testedMethods += 1
                } else {
                    missingFunctionFqPsi.add(it)
                }
            }
            ktFile.collectDescendantsOfType<KtProperty>().forEach {
                if (it.annotationEntries.containsSuppressionForMissingTest()) {
                    seenProperties += 1
                    testedProperties += 1
                    skippedPropertiesFqPsi.add(it)
                    return@forEach
                }
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
                it.visit(fileIndex)
            }
        }
    }

}

private fun List<KtAnnotationEntry>.containsSuppressionForMissingTest(): Boolean {
    val suppression = firstOrNull {
        it.shortName?.identifier.equals("Suppress", false)
    } ?: return false
    return suppression.valueArguments.filterIsInstance<KtValueArgument>().any {
        it.text.equals("\"MissingTestFunction\"")
    }
}

