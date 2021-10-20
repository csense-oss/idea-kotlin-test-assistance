package csense.idea.kotlin.test.testNavigation

import com.intellij.codeInsight.daemon.*
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import csense.idea.base.bll.kotlin.*
import csense.idea.base.bll.psi.*
import csense.idea.base.module.*
import csense.idea.base.module.findPackageDir
import csense.idea.kotlin.test.bll.*
import csense.idea.kotlin.test.bll.analyzers.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*
import java.awt.event.*


class TestNavigationVariableProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val method = element.getKtPropertyFromLineMarkerIdentifierLeaf() ?: return null
        if (method.isInTestModule()) {
            return null
        }
        val testMethod: List<PsiElement> = method.getTestedMethod()
        if (testMethod.isNotEmpty()) {
            val navHandler = GutterIconNavigationHandler { _: MouseEvent,
                                                           _: PsiElement ->
                testMethod.firstOrNull()?.tryNavigate(true)
            }

            return LineMarkerInfo(
                element,
                element.textRange,
                IconLoader.getIcon("/icons/test_icon.svg", javaClass),
                {
                    "Navigate to corresponding test case"
                },
                navHandler,
                GutterIconRenderer.Alignment.LEFT,
                { //for screen readers
                    "Navigate to corresponding test case(s)"
                }
            )

        }

        return null
    }


    fun KtProperty.getTestedMethod(): List<PsiElement> {
        val parent = containingClassOrObject?.namedClassOrObject()
        val containingFile = containingKtFile
        if (parent != null && parent.isAnonymous()) {
            return emptyList()
        }

        val safeContainingClass = parent
        //step 2 is to find the test file in the test root

        val testModule = TestInformationCache.lookupModuleTestSourceRoot(containingFile)
            ?: return emptyList()
        val resultingDirectory = testModule.findPackageDir(containingFile)
        val testFile = resultingDirectory?.findTestFile(containingFile) ?: return emptyList()
        val namesToLookAt = computeViableNames()
        return testFile.findTestOfMethodOrNull(
            namesToLookAt,
            containingFile,
            safeContainingClass
        )
    }

}

fun PsiElement.getKtPropertyFromLineMarkerIdentifierLeaf(): KtProperty? {
    return getKtElementFromLineMarkerIdentifierLeaf()
}

