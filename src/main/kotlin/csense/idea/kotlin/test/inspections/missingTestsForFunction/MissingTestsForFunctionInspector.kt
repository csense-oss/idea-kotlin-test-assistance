package csense.idea.kotlin.test.inspections.missingTestsForFunction

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import com.intellij.openapi.module.*
import csense.idea.base.bll.*
import csense.idea.kotlin.test.bll.*
import csense.idea.kotlin.test.bll.frameworks.*
import csense.idea.kotlin.test.bll.psi.*
import csense.idea.kotlin.test.bll.testGeneration.generationSteps.*
import csense.idea.kotlin.test.inspections.missingTestsForFunction.fixes.*
import org.jetbrains.kotlin.idea.codeinsight.api.classic.inspections.*
import org.jetbrains.kotlin.psi.*

class MissingTestsForFunctionInspector : AbstractKotlinInspection() {

    override fun getDisplayName(): String {
        return "Missing test for function"
    }

    override fun getStaticDescription(): String {
        return "Highlights functions that are missing test(s)"
    }

    override fun getDefaultLevel(): HighlightDisplayLevel {
        return HighlightDisplayLevel.WEAK_WARNING
    }

    override fun getDescriptionFileName(): String {
        return "Highlights functions that are missing test(s) "
    }

    override fun getShortName(): String {
        return "MissingTestFunction"
    }

    override fun getGroupDisplayName(): String {
        return Constants.groupName
    }

    override fun isEnabledByDefault(): Boolean {
        return true
    }


    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean
    ): KtVisitorVoid {
        return namedFunctionVisitor { ourFunction: KtNamedFunction ->
            if (ourFunction.isInTestModule2()) {
                return@namedFunctionVisitor
            }
            if (ourFunction.hasValidTest()) {
                return@namedFunctionVisitor
            }
            val quickFix: AddTestMethodByStepsQuickFix = createQuickfix(ourFunction)
            registerProblem(holder, ourFunction, quickFix)

        }
    }

    private fun registerProblem(
        holder: ProblemsHolder,
        ourFunction: KtNamedFunction,
        quickFix: AddTestMethodByStepsQuickFix
    ) {
        holder.registerProblemSafe(
            psiElement = ourFunction.nameIdentifier ?: ourFunction,
            descriptionTemplate = "Missing test(s) for function",
            fixes = arrayOf(
                quickFix
            )
        )
    }

    private fun createQuickfix(
        function: KtNamedFunction
    ): AddTestMethodByStepsQuickFix {
        val fqNameForPackage: String = function.fqNameForTestClass() //TODO
        val neededStepsBeforeAddingFunction: List<TestCodeGenerationStep> = getMissingStepsToGetTestClass(
            fqNameForPackage,
            fqNameForPackage,
            ModuleUtilCore.findModuleForPsiElement(function)
        )
        val createFunctionStep = CreateTestMethodGenerationStep(function.fqNameForTestClass())

        return AddTestMethodByStepsQuickFix(
            steps = neededStepsBeforeAddingFunction + createFunctionStep,
            forFramework = getTestFrameworkFromOtherTestsOrKotlinTest(),
            withAssertions = getAssertionFramework()
        )
    }


    private fun getTestFrameworkFromOtherTestsOrKotlinTest(): TestFramework {
        //TODO
        return TestFramework.KotlinTest
    }

    private fun getAssertionFramework(): AssertionsFramework {
        //TODO from settings
        return AssertionsFramework.Csense
    }

    private fun getMissingStepsToGetTestClass(
        packageFqName: String,
        classFqName: String,
        codeModule: Module?
    ): List<TestCodeGenerationStep> {
        if (codeModule == null) {
            return emptyList()
        }
        val result: MutableList<TestCodeGenerationStep> = mutableListOf()
        if (!hasTestPackage()) {
            result += CreateTestPackageGenerationStep(
                packageFqName = packageFqName,
                forModule = codeModule
            )
        }
        if (!hasTestClass()) {
            result += CreateTestClassGenerationStep(
                classFqName = classFqName
            )
        }
        if (!hasTestMethod()) {
            result += CreateTestMethodGenerationStep(
                methodFqName = "TODO"
            )
        }
        return result
    }

    private fun hasTestPackage(): Boolean {
        return false
    }

    private fun hasTestClass(): Boolean {
        return false
    }

    private fun hasTestMethod(): Boolean {
        return false
    }
}