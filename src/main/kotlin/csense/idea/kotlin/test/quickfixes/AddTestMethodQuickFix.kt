package csense.idea.kotlin.test.quickfixes

import com.intellij.codeInsight.daemon.*
import com.intellij.codeInspection.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import csense.idea.base.bll.psi.*
import csense.idea.kotlin.test.bll.*
import csense.idea.kotlin.test.bll.testGeneration.*
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

        val assertionType = SettingsContainer.getTestAssertionType()
        val code = KtNamedFunctionTestData.computeMostViableSimpleTestData(
            function = fnc,
            testName = safeTestName,
            assertion = assertionType
        )
        val codePsi: PsiElement = if (code.startsWith("class")) {
            ktPsiFactory.createClass(code)
        } else {
            ktPsiFactory.createFunction(code)
        }

        project.executeWriteCommand("update test class") {
            try {
                val body = whereToWrite.getOrCreateBody()
                body.addBefore(codePsi, body.lastChild)
            } catch (e: Throwable) {
                throw e
            }
        }
        DaemonCodeAnalyzer.getInstance(project).restart(file)
    }
}
