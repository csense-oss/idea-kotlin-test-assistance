package csense.idea.kotlin.test.testNavigation

import com.intellij.codeInsight.daemon.*
import com.intellij.psi.*
import csense.idea.base.bll.kotlin.*
import csense.idea.base.module.*
import csense.idea.kotlin.test.bll.psi.*
import csense.idea.kotlin.test.testNavigation.bll.*
import org.jetbrains.kotlin.psi.*

class TestedCodeNavigationFunctionProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val method: KtNamedFunction = element.getKtNamedFunctionFromLineMarkerIdentifierLeaf() ?: return null
        if (!method.isInTestModule()) {
            return null
        }

        val testMethod: List<PsiElement> = method.getTestedMethod()
        val firstTestMethod: PsiElement = testMethod.firstOrNull() ?: return null
        return NavigateToTestedCodeLineMarkerInfo(
            element = method,
            testMethod = firstTestMethod
        )

    }
}