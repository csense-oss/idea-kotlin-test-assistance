package csense.idea.kotlin.test.quickfixes

import com.intellij.codeInspection.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.util.*
import csense.idea.kotlin.test.bll.*
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
        val prop = startElement as KtProperty
        val safeName = testName.safeFunctionName()
        val code = prop.computeMostViableSimpleTestData()
        val ktPsiFactory = KtPsiFactory(project)
        val testMethod = ktPsiFactory.createFunction(
                """@Test
                   fun $safeName(){
                       //TODO make me
                       ${code.joinToString("\n")}
                   }
                """.trimMargin())
        project.executeWriteCommand("update test class") {
            try {
                val body = whereToWrite.getOrCreateBody()
                body.addBefore(testMethod, body.lastChild)
            } catch (e: Throwable) {
                TODO("Add error handling here")
            }
        }
    }
}