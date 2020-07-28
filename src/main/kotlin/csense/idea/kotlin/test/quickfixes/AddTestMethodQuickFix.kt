package csense.idea.kotlin.test.quickfixes

import com.intellij.codeInspection.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import csense.idea.base.bll.psi.*
import csense.idea.kotlin.test.bll.*
import csense.idea.kotlin.test.settings.*
import org.jetbrains.kotlin.idea.util.application.*
import org.jetbrains.kotlin.psi.*

class AddTestMethodQuickFix(
        element: KtNamedFunction,
        private val testName: String,
        whereToWrite: KtClassOrObject
) : LocalQuickFixOnPsiElement(element) {
    
    private val classToWrite = whereToWrite.smartPsiElementPointer()
    
    override fun getText(): String {
        return "Add test for this method"
    }
    
    override fun getFamilyName(): String {
        return Constants.groupName
    }
    
    
    override fun invoke(
            project: Project,
            file: PsiFile,
            startElement: PsiElement,
            endElement: PsiElement
    ) {
        val whereToWrite = classToWrite.element ?: return
        val fnc = startElement as? KtNamedFunction ?: return
        val safeTestName = testName.safeFunctionName()
        
        val ktPsiFactory = KtPsiFactory(project)
        val code = fnc.computeMostViableSimpleTestData(safeTestName, ktPsiFactory, SettingsContainer.shouldGenerateAssertStatements)
        project.executeWriteCommand("update test class") {
            try {
                val body = whereToWrite.getOrCreateBody()
                body.addBefore(code, body.lastChild)
            } catch (e: Throwable) {
                TODO("Add error handling here")
            }
        }
    }
}