package csense.idea.kotlin.test.inspections

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import csense.idea.base.bll.*
import csense.idea.base.bll.kotlin.*
import csense.idea.kotlin.test.bll.*
import csense.idea.kotlin.test.bll.analyzers.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.psi.*

/*
- mark empty tests (functions marked with @Test but with no real code in them)
    - quickfix is add @Ignore
 */
class EmptyTestInspection : AbstractKotlinInspection() {

    override fun getDisplayName(): String {
        return "Empty tests"
    }

    override fun getStaticDescription(): String {
        return "Highlights tests that are not ignore but are empty"
    }

    override fun getDefaultLevel(): HighlightDisplayLevel {
        return HighlightDisplayLevel.ERROR
    }

    override fun getDescriptionFileName(): String {
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

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean
    ): KtVisitorVoid {
        return namedFunctionVisitor { ourFnc ->
            val file = ourFnc.containingKtFile
            val project = file.project
            if (!TestInformationCache.isFileInTestModuleOrSourceRoot(file, project) ||//not in test module ?
                ourFnc.isNotAnnotatedTest() || //if its not a test ?
                ourFnc.isAnnotatedIgnore() ///if it is ignored then skip it as well
            ) {
                return@namedFunctionVisitor
            }
            //we are a test with no ignore.
            //are we empty ?
            if (ourFnc.isBodyEmpty()) {
                holder.registerProblemSafe(
                    ourFnc.nameIdentifier ?: ourFnc,
                    "Empty test, mark it as ignore, or make it :)"
                )
            }

        }
    }
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