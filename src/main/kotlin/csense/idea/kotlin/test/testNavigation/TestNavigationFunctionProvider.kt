package csense.idea.kotlin.test.testNavigation

import com.intellij.codeInsight.daemon.*
import com.intellij.psi.*
import csense.idea.base.bll.kotlin.*
import csense.idea.base.module.*
import csense.idea.kotlin.test.bll.*
import csense.idea.kotlin.test.bll.psi.*
import csense.idea.kotlin.test.testNavigation.bll.*
import org.jetbrains.kotlin.psi.*

class TestNavigationFunctionProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val method: KtNamedFunction? = element.getKtNamedFunctionFromLineMarkerIdentifierLeaf()
        if (method != null) {
            return onKtNamedFunction(method)
        }
        val `class`: KtClassOrObject? = element.getKtElementFromLineMarkerIdentifierLeaf<KtClassOrObject>()
        if (`class` != null) {
            return onClassOrOrbject(`class`)
        }

        return null

    }

    private fun onClassOrOrbject(`class`: KtClassOrObject): NavigateToTestCaseLineMarkerInfo? {
        if(`class`.isInTestModule()){
            return null
        }
        val tests: List<PsiElement> =  `class`.getTests()
        val firstTest: PsiElement = tests.firstOrNull() ?: return null
        return NavigateToTestCaseLineMarkerInfo(
            element = `class`,
            testMethod = firstTest
        )
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