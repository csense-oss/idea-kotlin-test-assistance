package csense.idea.kotlin.test.testNavigation

import com.intellij.codeInsight.daemon.*
import com.intellij.psi.*
import csense.idea.base.bll.kotlin.*
import csense.idea.base.module.*
import csense.idea.kotlin.test.bll.psi.*
import csense.idea.kotlin.test.testNavigation.bll.*
import org.jetbrains.kotlin.psi.*

class TestNavigationFunctionProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val method: KtNamedFunction? = element.getKtNamedFunctionFromLineMarkerIdentifierLeaf()
        if (method != null) {
            return onKtNamedFunction(method)
        }
        return null
    }

    private fun onKtNamedFunction(method: KtNamedFunction): NavigateToTestCaseLineMarkerInfo? {
        if (method.isInTestModule()) {
            return null
        }

        val testMethod: List<PsiElement> = method.getTests()
        val firstTestMethod: PsiElement = testMethod.firstOrNull() ?: return null
        return NavigateToTestCaseLineMarkerInfo(
            element = method,
            testMethod = firstTestMethod
        )
    }
}