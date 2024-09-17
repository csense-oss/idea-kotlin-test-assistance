package csense.idea.kotlin.test.testNavigation

import com.intellij.codeInsight.daemon.*
import com.intellij.psi.*
import csense.idea.base.bll.kotlin.*
import csense.idea.base.module.*
import csense.idea.kotlin.test.bll.psi.*
import csense.idea.kotlin.test.testNavigation.bll.*
import org.jetbrains.kotlin.psi.*

class TestNavigationClassProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val `class`: KtClassOrObject? = element.getKtElementFromLineMarkerIdentifierLeaf<KtClassOrObject>()
        if (`class` != null) {
            return onClassOrOrbject(`class`)
        }
        return null
    }

    private fun onClassOrOrbject(`class`: KtClassOrObject): NavigateToTestCaseLineMarkerInfo? {
        if (`class`.isInTestModule()) {
            return null
        }
        val tests: List<PsiElement> = `class`.getTests()
        val firstTest: PsiElement = tests.firstOrNull() ?: return null
        return NavigateToTestCaseLineMarkerInfo(
            element = `class`,
            testMethod = firstTest
        )
    }
}