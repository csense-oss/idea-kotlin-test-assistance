package csense.idea.kotlin.test.quickfixes

import com.intellij.codeInspection.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.util.*
import org.jetbrains.kotlin.idea.util.application.*
import org.jetbrains.kotlin.psi.*

class AddTestMethodQuickFix(
        element: PsiElement,
        val testName: String,
        val whereToWrite: KtClassOrObject
) : LocalQuickFixOnPsiElement(element) {

    override fun getText(): String {
        return "Add test for this method"
    }

    override fun getFamilyName(): String {
        return "Csense - kotlin - test"
    }


    override fun invoke(
            project: Project,
            file: PsiFile,
            startElement: PsiElement,
            endElement: PsiElement
    ) {
        val ktPsiFactory = KtPsiFactory(project)
        val testMethod = ktPsiFactory.createFunction(
                """ @Test
                    fun $testName(){
                    TODO("Test me :)")
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