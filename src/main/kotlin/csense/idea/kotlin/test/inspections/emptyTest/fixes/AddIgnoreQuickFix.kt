package csense.idea.kotlin.test.inspections.emptyTest.fixes

import com.intellij.openapi.project.*
import com.intellij.psi.*
import csense.idea.base.bll.quickfixes.*
import csense.idea.kotlin.test.bll.*
import csense.idea.kotlin.test.bll.psi.*
import org.jetbrains.kotlin.idea.util.*
import org.jetbrains.kotlin.idea.util.application.*
import org.jetbrains.kotlin.name.*
import org.jetbrains.kotlin.psi.*

class AddIgnoreQuickFix(
    onFunction: KtNamedFunction,
    @Suppress("ActionIsNotPreviewFriendly")
    private val framework: TestFramework
) : LocalQuickFixOnSingleKtElement<KtNamedFunction>(onFunction) {

    override fun invoke(project: Project, file: PsiFile, element: KtNamedFunction) {
        project.executeWriteCommand(AddIgnoreQuickFix::class.java.simpleName) {
            element.addAnnotation(FqName(framework.ignoreFqName))
        }
    }

    override fun getFamilyName(): String {
        return Constants.groupName
    }

    override fun getText(): String {
        return "Add ignore to test method"
    }
}