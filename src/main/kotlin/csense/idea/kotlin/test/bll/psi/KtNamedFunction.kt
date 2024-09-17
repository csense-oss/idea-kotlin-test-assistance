package csense.idea.kotlin.test.bll.psi

import com.intellij.psi.*
import csense.idea.base.bll.kotlin.*
import csense.idea.base.bll.psi.*
import csense.idea.base.bll.psiWrapper.annotation.*
import csense.idea.base.bll.psiWrapper.function.operations.*
import csense.idea.kotlin.test.bll.*
import csense.idea.kotlin.test.bll.frameworks.*
import csense.idea.kotlin.test.bll.search.*
import csense.kotlin.extensions.collections.*
import csense.kotlin.extensions.primitives.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*
import kotlin.collections.isNotEmpty


fun KtNamedFunction.isAnnotatedTest(): Boolean {
    return toKtPsiFunction().containsAnnotationBy { it: KtPsiAnnotation ->
        it.fqName in TestFramework.allTestFqNames
    }
}

fun KtNamedFunction.isNotAnnotatedTest(): Boolean = !isAnnotatedTest()

fun KtNamedFunction.isAnnotatedIgnore(): Boolean {
    return toKtPsiFunction().containsAnyAnnotation(TestFramework.allIgnoreFqNames)
}


fun KtNamedFunction.fqNameForTestClass(): String {
    return containingClassOrObject?.getKotlinFqNameString()
        ?: containingKtFile.packageFqName.asString()
}



fun KtNamedFunction.firstParameterNameOrEmpty(): String {
    return valueParameters.firstOrNull()?.name?.titleCaseFirstWord() ?: ""
}

fun KtNamedFunction.firstParameterTypeCapOrEmpty(): String {
    //TODO consider that this could open op say "optString" or alike ways of expression the optionality.
    return valueParameters.firstOrNull()?.typeReference?.text?.replace(oldValue = "?", newValue = "")
        ?: ""
}

fun KtNamedFunction.computeViableTestNames(): Set<String> {
    val overLoads: Boolean = haveOverloads()
    val safeName: String = name?.safeFunctionName() ?: ""

    val regularNames: Set<String> = if (overLoads) {
        //overloads opens up 2 things
        // firstParameterName
        // firstParameterType
        //if an extension then also
        // typeSafeName
        setOfNotNull(
            safeName + firstParameterNameOrEmpty(),
            safeName + firstParameterTypeCapOrEmpty()
        )
    } else {
        //no overloads; so just the name should be sufficient.
        setOf(safeName)
    }
    val extensions: Set<String> = if (isExtensionDeclaration()) {
        val extensionName: String? = receiverTypeReference?.text?.safeFunctionName()?.decapitalize()
        setOfNotNull(
            extensionName?.let { it.decapitalize() + safeName.titleCaseFirstWord() },
            extensionName?.let { it + safeName.titleCaseFirstWord() },
            extensionName?.let { it + safeName.titleCaseFirstWord() + firstParameterNameOrEmpty() },
            extensionName?.let { it + safeName.titleCaseFirstWord() + firstParameterTypeCapOrEmpty() })
    } else {
        emptySet()
    }
    return extensions + regularNames
}


//fun KtNamedFunction.computeMostPreciseTestName(): String {
//    val safeName: String = name ?: ""
//    if (isExtensionDeclaration()) {
//        val extensionName: String? = receiverTypeReference?.text
//        return if (haveOverloads()) {
//            val firstParamName: String = firstParameterNameOrEmpty()
//            extensionName?.plus(safeName.titleCaseFirstWord())?.plus(firstParamName) ?: safeName
//        } else {
//            extensionName?.plus(safeName.titleCaseFirstWord()) ?: safeName
//        }.safeDecapitizedFunctionName()
//    }
//    if (haveOverloads()) {
//        val firstParamName: String = firstParameterNameOrEmpty()
//        return (safeName + firstParamName).safeFunctionName()
//    }
//    return safeName.safeDecapitizedFunctionName()
//}


fun KtNamedFunction.hasValidTest(): Boolean {
    return getTests().isNotEmpty()
}

fun KtNamedFunction.containsAssertInCalls(): Boolean {
    return !isBodyEmpty() && anyDescendantOfType<KtCallExpression> { it: KtCallExpression ->
        it.text.contains("assert", ignoreCase = true)
    }
}

fun KtNamedFunction.containsAssertInName(): Boolean {
    return name?.contains("assert", ignoreCase = true) == true
}

fun KtNamedFunction.getTests(): List<PsiElement> {

    val containingKtFile: KtFile = containingKtFile
    val testFile: KtFile = TestSearch.findTestFileBy(containingKtFile) ?: return emptyList()

    val containingClass: KtClassOrObject? = containingClassOrObject?.namedClassOrObject()
    return testFile.findTestByNameOrNull(
        fnNames = computeViableTestNames(),
        orgFile = containingKtFile,
        orgClass = containingClass
    )
}


fun KtNamedFunction.getTestedMethod(): List<PsiElement> {
    val containingKtFile: KtFile = containingKtFile
    val testFiles: List<KtFile> = TestSearch.findCodeFromTestFile(containingKtFile) ?: return emptyList()

    val containingClass: KtClassOrObject? = containingClassOrObject?.namedClassOrObject()

    return testFiles.selectFirstOrNull { it: KtFile ->
        it.findCodeFromTestNameOrNull(
            fnNames = computeViableCodeNames(),
            orgFile = containingKtFile,
            orgClass = containingClass
        )
    } ?: emptyList()

}


fun KtNamedFunction.computeViableCodeNames(): Set<String> {
    return setOfNotNull(name, name?.plus("Test"))
//    TODO()
//    val overLoads: Boolean = haveOverloads()
//    val safeName: String = name?.safeFunctionName() ?: ""
//
//    val regularNames: Set<String> = if (overLoads) {
//        //overloads opens up 2 things
//        // firstParameterName
//        // firstParameterType
//        //if an extension then also
//        // typeSafeName
//        setOfNotNull(
//            safeName + firstParameterNameOrEmpty(),
//            safeName + firstParameterTypeCapOrEmpty()
//        )
//    } else {
//        //no overloads; so just the name should be sufficient.
//        setOf(safeName)
//    }
//    val extensions: Set<String> = if (isExtensionDeclaration()) {
//        val extensionName: String? = receiverTypeReference?.text?.safeFunctionName()?.decapitalize()
//        setOfNotNull(
//            extensionName?.let { it.decapitalize() + safeName.titleCaseFirstWord() },
//            extensionName?.let { it + safeName.titleCaseFirstWord() },
//            extensionName?.let { it + safeName.titleCaseFirstWord() + firstParameterNameOrEmpty() },
//            extensionName?.let { it + safeName.titleCaseFirstWord() + firstParameterTypeCapOrEmpty() })
//    } else {
//        emptySet()
//    }
//    return extensions + regularNames
}


fun KtFile.findCodeFromTestNameOrNull(
    fnNames: Set<String>,
    orgFile: KtFile,
    orgClass: KtClassOrObject?
): List<PsiElement> {
    val matchingFunctions= collectDescendantsOfType<KtElement> { it: KtElement ->
        it.name in fnNames
    }
    if(matchingFunctions.isNotEmpty()){
        return matchingFunctions
    }
    val matchingClasses: List<KtClassOrObject> = collectDescendantsOfType<KtClassOrObject> { it.name == orgClass?.name?.removeTestCodeNames() }
    if(matchingClasses.isNotEmpty()){
        return matchingClasses
    }
    return emptyList()
//    TODO()
//    val classes: List<PsiElement> = when {
//        publicClasses == 1 || publicClasses == 0 -> { //eg if there are only extensions there will be no public classes.
//            getTestSingleClassOfMethodName(fnNames).nullOnEmpty() ?: getTestSingleClassObjectOfMethodName(fnNames)
//        }
//        publicClasses > 1 -> {
//            getTestMultipleClassOfMethodName(fnNames, orgClass, orgFile.virtualFile.nameWithoutExtension)
//        }
//        else -> {
//            emptyList()
//        }
//    }
//    return classes
}