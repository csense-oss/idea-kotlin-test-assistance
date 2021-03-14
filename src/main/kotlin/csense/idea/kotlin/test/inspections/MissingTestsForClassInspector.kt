package csense.idea.kotlin.test.inspections

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import csense.idea.kotlin.test.bll.*
import csense.idea.kotlin.test.bll.analyzers.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.psi.*

class MissingTestsForClassInspector : AbstractKotlinInspection() {

    override fun getDisplayName(): String {
        return "Missing tests for class"
    }

    override fun getStaticDescription(): String {
        return "Highlights classes that are missing test(s)"
    }

    override fun getDefaultLevel(): HighlightDisplayLevel {
        return HighlightDisplayLevel.WEAK_WARNING
    }

    override fun getDescriptionFileName(): String {
        return "Highlights for classes that are missing test(s) "
    }

    override fun getShortName(): String {
        return "MissingTestClass"
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
        return classOrObjectVisitor { outerClass ->
            val result = MissingTestsForClassAnalyzer.analyze(outerClass)
            result.errors.forEach {
                holder.registerProblem(it)
            }
        }
    }
}
