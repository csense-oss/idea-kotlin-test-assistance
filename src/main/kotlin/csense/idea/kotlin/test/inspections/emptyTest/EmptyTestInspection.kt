package csense.idea.kotlin.test.inspections.emptyTest

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import csense.idea.base.bll.*
import csense.idea.base.bll.kotlin.*
import csense.idea.kotlin.test.bll.*
import csense.idea.kotlin.test.bll.analyzers.*
import csense.idea.kotlin.test.bll.psi.*
import csense.idea.kotlin.test.inspections.emptyTest.fixes.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*

/*
- mark empty tests (functions marked with @Test but with no real code in them)
    - quickfix is add @Ignore
 */
class EmptyTestInspection : AbstractKotlinInspection() {

    override fun getDisplayName(): String {
        return "Tests either missing assertions or is empty"
    }

    override fun getStaticDescription(): String {
        return "Highlights tests that are not ignore but are empty or missing assertion(s)"
    }

    override fun getDefaultLevel(): HighlightDisplayLevel {
        return HighlightDisplayLevel.ERROR
    }

    override fun getDescriptionFileName(): String {
        return "Highlights tests that are not ignore but are empty or missing assertion(s)"
    }

    override fun getShortName(): String {
        return "MissingTestCode"
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

        val shouldIgnore: Boolean = file.shouldIgnore() || ourFnc.shouldIgnore()
        if (shouldIgnore) {
            return@namedFunctionVisitor
        }

        val isValidTest: Boolean = ourFnc.containsAssertInCalls()
        if (isValidTest) {
            return@namedFunctionVisitor
        }

        val testFramework: TestFramework = file.guessBestTestFrameworkOrKotlinTest()

        reportEmptyTest(function = ourFnc, forFramework = testFramework, holder = holder)
    }

    private fun reportEmptyTest(
        function: KtNamedFunction,
        forFramework: TestFramework,
        holder: ProblemsHolder
    ) {


        holder.registerProblemSafe(
            psiElement = function.nameIdentifier ?: function,
            descriptionTemplate = "Test is either empty or missing assert statements",
            fixes = arrayOf(
                AddIgnoreQuickFix(onFunction = function, framework = forFramework)
            )
        )
    }

    private fun KtNamedFunction.containsAssertInCalls(): Boolean {
        return !isBodyEmpty() && anyDescendantOfType<KtCallExpression> { it: KtCallExpression ->
            it.text.contains("assert", ignoreCase = true)
        }
    }

    private fun KtFile.shouldIgnore(): Boolean {
        return isNotInTestModule()
    }

    private fun KtNamedFunction.shouldIgnore(): Boolean {
        return isNotAnnotatedTest() || isAnnotatedIgnore()
    }

    private fun KtFile.guessBestTestFrameworkOrKotlinTest(): TestFramework {
        return guessTestFrameworkInUse() ?: TestFramework.KotlinTest
    }
}



