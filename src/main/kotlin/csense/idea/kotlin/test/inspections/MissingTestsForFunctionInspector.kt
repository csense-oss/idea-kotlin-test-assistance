package csense.idea.kotlin.test.inspections

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import csense.idea.kotlin.test.bll.*
import csense.idea.kotlin.test.bll.analyzers.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.psi.*

class MissingTestsForFunctionInspector : AbstractKotlinInspection() {
    
    override fun getDisplayName(): String {
        return "Missing test for function"
    }
    
    override fun getStaticDescription(): String? {
        return "Highlights functions that are missing test(s)"
    }
    
    override fun getDefaultLevel(): HighlightDisplayLevel {
        return HighlightDisplayLevel.WEAK_WARNING
    }
    
    override fun getDescriptionFileName(): String? {
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
    
    
    override fun buildVisitor(holder: ProblemsHolder,
                              isOnTheFly: Boolean): KtVisitorVoid {
        return namedFunctionVisitor { ourFunction: KtNamedFunction ->
            val result = MissingtestsForFunctionAnalyzers.analyze(ourFunction)
            result.errors.forEach {
                holder.registerProblem(it)
            }
        }
    }
}
