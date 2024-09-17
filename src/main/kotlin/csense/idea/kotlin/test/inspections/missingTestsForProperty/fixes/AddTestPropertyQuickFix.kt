package csense.idea.kotlin.test.inspections.missingTestsForProperty.fixes

import com.intellij.codeInsight.daemon.*
import com.intellij.codeInspection.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import csense.idea.kotlin.test.bll.*
import csense.idea.kotlin.test.bll.testGeneration.computeMostViableSimpleTestData
import org.jetbrains.kotlin.idea.util.application.*
import org.jetbrains.kotlin.psi.*

class AddTestPropertyQuickFix(
    element: PsiElement,
    val testName: String,
    val whereToWrite: KtClassOrObject
) : LocalQuickFixOnPsiElement(element) {

    override fun getText(): String {
        return "Add test for this property"
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
        val prop: KtProperty = startElement as KtProperty
        val safeName: String = testName.safeFunctionName()
        val ktPsiFactory = KtPsiFactory(project)
        val code: PsiElement = prop.computeMostViableSimpleTestData(safeName, ktPsiFactory)

        project.executeWriteCommand("Update Test Class") {
            try {
                val body: KtClassBody = whereToWrite.getOrCreateBody()
                body.addBefore(code, body.lastChild)
            } catch (e: Throwable) {
                TODO("Add error handling here")
            }
        }
        DaemonCodeAnalyzer.getInstance(project).restart(file)
    }
}