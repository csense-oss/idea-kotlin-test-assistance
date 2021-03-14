package csense.idea.kotlin.test.quickfixes

import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory

class CreateCompanionTestClassQuickFix(
    className: String,
    testClass: KtClass
) : LocalQuickFixOnPsiElement(testClass) {
    private val correctedClassName: String = className.capitalize() + "Companion"
    private val testClassName: String = "${correctedClassName}Test"

    override fun getFamilyName(): String {
        return this::class.java.simpleName
    }

    override fun getText(): String {
        return "Create companion test class $testClassName"
    }

    override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
        val testFile = startElement as? KtFile ?: return
        val factory = KtPsiFactory(project)
        val clz = factory.createClass("class $testClassName{\n}")
        testFile.add(clz)
    }

}