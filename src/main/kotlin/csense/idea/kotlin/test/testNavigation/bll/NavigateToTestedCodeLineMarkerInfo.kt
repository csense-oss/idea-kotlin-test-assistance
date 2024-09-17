package csense.idea.kotlin.test.testNavigation.bll

import com.intellij.codeInsight.daemon.*
import com.intellij.openapi.editor.markup.*
import com.intellij.psi.*
import csense.idea.base.bll.psi.*
import csense.idea.kotlin.test.res.*
import java.awt.event.*

class NavigateToTestedCodeLineMarkerInfo(
    element: PsiElement,
    testMethod: PsiElement
) : LineMarkerInfo<PsiElement>(
    /* element = */ element,
    /* range = */ element.textRange,
    /* icon = */ Icons.testIcon,
    /* updatePass = */ { _: PsiElement ->
        "Navigate to corresponding code for test"
    },
    /* tooltipProvider = */ GutterIconNavigationHandler { _: MouseEvent,
                                                           _: PsiElement ->
        testMethod.tryNavigate(true)
    },
    /* navHandler = */ GutterIconRenderer.Alignment.LEFT,
    /* alignment = */ { //for screen readers
        "Navigate to corresponding code for test"
    }
)