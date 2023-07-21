package csense.idea.kotlin.test.bll.psi

import csense.idea.base.bll.psiWrapper.annotation.*
import csense.idea.base.bll.psiWrapper.function.operations.*
import org.jetbrains.kotlin.psi.*


fun KtNamedFunction.isAnnotatedTest(): Boolean {
    return toKtPsiFunction().containsAnnotationBy { it: KtPsiAnnotation ->
        it.fqName in TestFramework.allTestFqNames
    }
}

fun KtNamedFunction.isNotAnnotatedTest(): Boolean = !isAnnotatedTest()

fun KtNamedFunction.isAnnotatedIgnore(): Boolean {
    return toKtPsiFunction().containsAnnotationBy { it: KtPsiAnnotation ->
        it.fqName in TestFramework.allIgnoreFqNames
    }
}

//fun List<KtAnnotationEntry>.anyTextContains(
//    name: String,
//    ignoreCase: Boolean = false
//) = any {
//    it.text?.contains(name, ignoreCase) ?: false
//}
//
