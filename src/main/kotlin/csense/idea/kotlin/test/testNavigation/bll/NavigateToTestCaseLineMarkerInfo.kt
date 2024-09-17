package csense.idea.kotlin.test.testNavigation.bll

import com.intellij.codeInsight.daemon.*
import com.intellij.openapi.editor.markup.*
import com.intellij.psi.*
import csense.idea.base.bll.psi.*
import csense.idea.kotlin.test.res.*
import java.awt.event.*

class NavigateToTestCaseLineMarkerInfo(
    element: PsiElement,
    testMethod: PsiElement
) : LineMarkerInfo<PsiElement>(
    /* element = */ element,
    /* range = */ element.textRange,
    /* icon = */ Icons.testIcon,
    /* tooltipProvider = */ { _: PsiElement ->
        "Navigate to corresponding test case(s)"
    },
    /* navHandler = */ GutterIconNavigationHandler { _: MouseEvent,
                                                     _: PsiElement ->
        testMethod.tryNavigate(true)
    },
    /* alignment = */ GutterIconRenderer.Alignment.LEFT,
    /* accessibleNameProvider = */ { //for screen readers
        "Navigate to corresponding test case(s)"
    }
)