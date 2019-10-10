package csense.idea.kotlin.test.quickfixes

import com.intellij.codeInspection.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.util.*
import csense.idea.kotlin.test.bll.*
import org.jetbrains.kotlin.idea.util.application.*
import org.jetbrains.kotlin.psi.*

class AddTestMethodQuickFix(
        element: KtNamedFunction,
        val testName: String,
        val whereToWrite: KtClassOrObject
) : LocalQuickFixOnPsiElement(element) {

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
        val fnc = startElement as? KtNamedFunction ?: return
        val code = fnc.computeMostViableSimpleTestData()

        val safeTestName = testName.safeFunctionName()
        val ktPsiFactory = KtPsiFactory(project)
        val testMethod = ktPsiFactory.createFunction(
                """@Test
                   fun $safeTestName(){
                       //TODO make me.
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