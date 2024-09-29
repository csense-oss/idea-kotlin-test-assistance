package csense.idea.kotlin.test.inspections.missingTestAnnotation

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import csense.idea.base.bll.*
import csense.idea.kotlin.test.bll.*
import csense.idea.kotlin.test.bll.frameworks.*
import csense.idea.kotlin.test.bll.psi.*
import csense.idea.kotlin.test.inspections.missingTestAnnotation.fixes.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*

class MissingTestAnnotationInspection : AbstractKotlinInspection() {

    override fun getDisplayName(): String {
        return "Highlights functions that looks like test but are not marked as tests"
    }

    override fun getStaticDescription(): String {
        return "Highlights functions that looks like test but are not marked as tests"
    }

    override fun getDefaultLevel(): HighlightDisplayLevel {
        return HighlightDisplayLevel.ERROR
    }


    override fun getShortName(): String {
        return "MissingTestAnnotation"
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
    ): KtVisitorVoid = namedFunctionVisitor { ourFnc: KtNamedFunction ->
        val file: KtFile = ourFnc.containingKtFile
        if (file.isNotInTestModule()) {
            return@namedFunctionVisitor
        }

        if (ourFnc.isValidTestFunction()) {
            return@namedFunctionVisitor
        }

        val testFramework: TestFramework = file.guessBestTestFrameworkOrKotlinTest()
        reportMissingTestAnnotation(
            function = ourFnc,
            forFramework = testFramework,
            holder = holder
        )
    }

    private fun reportMissingTestAnnotation(
        function: KtNamedFunction,
        forFramework: TestFramework,
        holder: ProblemsHolder
    ) {
        holder.registerProblemSafe(
            psiElement = function.nameIdentifier ?: function,
            descriptionTemplate = "This appears as a test but is not marked as such",
            fixes = arrayOf(
                AddTestAnnotationQuickFix(onFunction = function, framework = forFramework)
            )
        )
    }

    private fun KtNamedFunction.isValidTestFunction(): Boolean {
        if (containsAssertInName()) {
            return true
        }
        if (isPrivate()) {
            return true
        }
        if (isAnnotatedTest() || isAnnotatedIgnore()) {
            return true
        }
        return !containsAssertInCalls()

    }
}