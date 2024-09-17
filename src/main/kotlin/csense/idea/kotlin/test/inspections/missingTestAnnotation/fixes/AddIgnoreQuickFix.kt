package csense.idea.kotlin.test.inspections.missingTestAnnotation.fixes

import com.intellij.openapi.project.*
import com.intellij.psi.*
import csense.idea.base.bll.quickfixes.*
import csense.idea.kotlin.test.bll.*
import csense.idea.kotlin.test.bll.frameworks.*
import org.jetbrains.kotlin.idea.util.*
import org.jetbrains.kotlin.idea.util.application.*
import org.jetbrains.kotlin.name.*
import org.jetbrains.kotlin.psi.*

class AddTestAnnotationQuickFix(
    onFunction: KtNamedFunction,
    @Suppress("ActionIsNotPreviewFriendly")
    private val framework: TestFramework
) : LocalQuickFixOnSingleKtElement<KtNamedFunction>(onFunction) {

    override fun invoke(project: Project, file: PsiFile, element: KtNamedFunction) {
        project.executeWriteCommand(AddTestAnnotationQuickFix::class.java.simpleName) {
            element.addAnnotation(FqName(framework.testFqName))
        }
    }

    override fun getFamilyName(): String {
        return Constants.groupName
    }

    override fun getText(): String {
        return "Mark method as a test"
    }
}