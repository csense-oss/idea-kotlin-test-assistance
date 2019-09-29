package csense.idea.kotlin.test.inspections

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import com.intellij.psi.*
import csense.idea.kotlin.test.bll.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*

/*
- mark empty tests (functions marked with @Test but with no real code in them)
    - quickfix is add @Ignore
 */
class EmptyTestInspection : AbstractKotlinInspection() {

    override fun getDisplayName(): String {
        return "Empty tests"
    }

    override fun getStaticDescription(): String? {
        return "Highlights tests that are not ignore but are empty"
    }

    override fun getDefaultLevel(): HighlightDisplayLevel {
        return HighlightDisplayLevel.ERROR
    }

    override fun getDescriptionFileName(): String? {
        return "Highlights tests that are not ignore but are empty"
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

    override fun buildVisitor(holder: ProblemsHolder,
                              isOnTheFly: Boolean): KtVisitorVoid {
        return namedFunctionVisitor { ourFnc ->
            if (!ourFnc.isInTestModule() ||//not in test module ?
                    ourFnc.isNotAnnotatedTest() || //if its not a test ?
                    ourFnc.isAnnotatedIgnore() ///if it is ignored then skip it as well
            ) {
                return@namedFunctionVisitor
            }
            //we are a test with no ignore.
            //are we empty ?
            if (ourFnc.isBodyEmpty()) {
                holder.registerProblem(
                        ourFnc.nameIdentifier ?: ourFnc,
                        "Empty test, mark it as ignore."
                )
            }

        }
    }
}

fun KtNamedFunction.isBodyEmpty(): Boolean {
    return bodyExpression?.children?.isEmpty() ?: true
}

fun KtNamedFunction.isAnnotatedTest(): Boolean =
        annotationEntries.anyTextContains("test", true)

fun KtNamedFunction.isNotAnnotatedTest(): Boolean = !isAnnotatedTest()

fun KtNamedFunction.isAnnotatedIgnore(): Boolean =
        annotationEntries.anyTextContains("ignore", true)

fun List<KtAnnotationEntry>.anyTextContains(
        name: String,
        ignoreCase: Boolean = false
) = any {
    it.text?.contains(name, ignoreCase) ?: false
}