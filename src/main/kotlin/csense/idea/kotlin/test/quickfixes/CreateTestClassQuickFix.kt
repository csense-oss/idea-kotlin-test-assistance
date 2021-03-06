package csense.idea.kotlin.test.quickfixes

import com.intellij.codeInspection.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import org.jetbrains.kotlin.psi.*

class CreateTestClassQuickFix(
        className: String,
        testFile: KtFile
) : LocalQuickFixOnPsiElement(testFile) {
    private val correctedClassName: String = className.capitalize()
    override fun getFamilyName(): String {
        return this::class.java.simpleName
    }
    
    override fun getText(): String {
        return "Create test class ${correctedClassName}Test"
    }
    
    override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
        val testFile = startElement as? KtFile ?: return
        val factory = KtPsiFactory(project)
        val clz = factory.createClass("class ${correctedClassName}Test{\n}")
        testFile.add(clz)
    }
    
    
}