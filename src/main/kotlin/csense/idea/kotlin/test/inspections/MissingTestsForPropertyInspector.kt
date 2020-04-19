package csense.idea.kotlin.test.inspections

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import com.intellij.openapi.module.Module
import com.intellij.psi.PsiDirectory
import csense.idea.base.bll.kotlin.hasConstantCustomGetterOnly
import csense.idea.base.bll.kotlin.hasCustomSetterGetter
import csense.idea.base.bll.registerProblemSafe
import csense.idea.base.module.findPackageDir
import csense.idea.base.module.findTestModule
import csense.idea.base.module.isInTestModule
import csense.idea.kotlin.test.bll.*
import csense.idea.kotlin.test.bll.analyzers.*
import csense.idea.kotlin.test.quickfixes.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.idea.refactoring.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*
import kotlin.system.*

class MissingTestsForPropertyInspector : AbstractKotlinInspection() {
    override fun getDisplayName(): String {
        return "Missing test for property (getter/setter)"
    }
    
    override fun getStaticDescription(): String? {
        return "Highlights properties with custom getter / setter that are missing test(s)"
    }
    
    override fun getDefaultLevel(): HighlightDisplayLevel {
        return HighlightDisplayLevel.WEAK_WARNING
    }
    
    override fun getDescriptionFileName(): String? {
        return "Highlights properties with custom getter / setter that are missing test(s) "
    }
    
    override fun getShortName(): String {
        return "MissingTestProperty"
    }
    
    override fun getGroupDisplayName(): String {
        return Constants.groupName
    }
    
    override fun isEnabledByDefault(): Boolean {
        return true
    }
    
    override fun buildVisitor(holder: ProblemsHolder,
                              isOnTheFly: Boolean): KtVisitorVoid {
        return propertyVisitor { prop ->
            val result = MissingTestsForPropertyAnalyzer.analyze(prop)
            result.errors.forEach {
                holder.registerProblem(it)
            }
            
        }
    }
}
