@file:Suppress("NOTHING_TO_INLINE")

package csense.idea.kotlin.test.bll

import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import csense.idea.base.bll.kotlin.*
import csense.idea.base.bll.psi.countDescendantOfType
import csense.idea.base.bll.psi.haveDescendantOfType
import csense.kotlin.extensions.collections.*
import csense.kotlin.extensions.collections.typed.contains
import csense.kotlin.extensions.primitives.endsWithAny
import csense.kotlin.extensions.primitives.startsWithAny
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.findDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.isPublic
import sun.awt.image.*


fun PsiDirectory.findTestFile(containingFile: KtFile): KtFile? {
    val fileName = containingFile.virtualFile.nameWithoutExtension
    val possibleFileNames = fileName.computeTestFileNames()
    return files.find {
        it.name.startsWithAny(possibleFileNames, ignoreCase = true)
    } as? KtFile
}

/**
 * Regular tests names
 * @receiver String
 * @return List<String>
 */
fun String.computeTestFileNames(): List<String> {
    return listOf(
        this,
        this + "Test",
        this + "KtTest",
        this + "KtTests",
        this + "Tests",
        this.safeClassName() + "Test",
        this.safeClassName() + "KtTest",
        this.safeClassName() + "KtTests",
        this.safeClassName() + "Tests",
    )
}

fun KtFile.haveTestOfMethod(fnNames: List<String>, orgFile: KtFile, orgClass: KtClassOrObject?): Boolean {
    return findTestOfMethodOrNull(fnNames, orgFile, orgClass).isNotEmpty()
}

fun KtFile.findTestOfMethodOrNull(
    fnNames: List<String>,
    orgFile: KtFile,
    orgClass: KtClassOrObject?
): List<PsiElement> {
    val publicClasses = orgFile.countDescendantOfType<KtClassOrObject> {
        it.isPublic
    }
    val classes = when {
        publicClasses == 1 || publicClasses == 0 -> { //eg if there are only extensions there will be no public classes.
            getTestSingleClassOfMethodName(fnNames).nullOnEmpty() ?: getTestSingleClassObjectOfMethodName(fnNames)
        }
        publicClasses > 1 -> {
            getTestMultipleClassOfMethodName(fnNames, orgClass, orgFile.virtualFile.nameWithoutExtension)
        }
        else -> {
            emptyList()
        }
    }
    return classes
}

fun KtFile.findMostSuitableTestClass(forClass: KtClassOrObject?, fileName: String): KtClassOrObject? {
    return findMostSuitableTestClassPrivate(forClass, fileName)
}

fun KtClass.findMostSuitableTestClass(forClass: KtClassOrObject?, fileName: String): KtClassOrObject? {
    return findMostSuitableTestClassPrivate(forClass, fileName)
}

private fun KtElement.findMostSuitableTestClassPrivate(forClass: KtClassOrObject?, fileName: String): KtClassOrObject? {
    return findDescendantOfType { classOrObject: KtClassOrObject ->
        if (forClass != null && forClass.isCompanion()) {
            classOrObject.name //todo what the...
        }
        forClass?.name?.let { ourClass ->
            classOrObject.name?.startsWith(ourClass, true)
        } ?: classOrObject.name?.startsWith(fileName, true) ?: false
    }
}


fun KtFile.haveTestMultipleClassOfMethodName(
    fnNames: List<String>,
    orgClass: KtClassOrObject?,
    fileName: String
): Boolean {
    return getTestMultipleClassOfMethodName(fnNames, orgClass, fileName).isNotEmpty()
}

fun KtFile.getTestMultipleClassOfMethodName(
    fnNames: List<String>,
    orgClass: KtClassOrObject?,
    fileName: String
): List<PsiElement> {
    val validClass = findMostSuitableTestClass(orgClass, fileName) ?: return listOf()

    return fnNames.mapNotNull {
        val functionNamesToFind = it.computeTestNames()
        val didFindFunction = validClass.getTestByMethodNames(functionNamesToFind)
        didFindFunction ?: validClass.getTestOfClassObjectOfMethodName(functionNamesToFind)
    }
}

fun KtFile.haveTestSingleClassOfMethodName(fnNames: List<String>): Boolean = fnNames.any { ourFunction ->
    val functionNamesToFind = ourFunction.computeTestNames()
    getTestByMethodNames(functionNamesToFind) != null
}

fun KtFile.getTestSingleClassOfMethodName(fnNames: List<String>): List<PsiElement> = fnNames.mapNotNull { ourFunction ->
    val functionNamesToFind = ourFunction.computeTestNames()
    getTestByMethodNames(functionNamesToFind)
}

fun PsiElement.getTestByMethodNames(functionNamesToFind: Set<String>): KtNamedFunction? {
    return findDescendantOfType<KtNamedFunction> {
        val name = it.name ?: return@findDescendantOfType false
        it.containingClassOrObject?.isTopLevel() == true &&
                functionNamesToFind.contains(name, true)
    }
}

fun KtFile.getTestSingleClassObjectOfMethodName(fnNames: List<String>): List<PsiElement> =
    fnNames.mapNotNull { ourFunction ->
        val functionNamesToFind = ourFunction.computeTestNames()
        getTestOfClassObjectOfMethodName(functionNamesToFind)
    }

fun PsiElement.haveTestOfClassObjectOfMethodName(functionNamesToFind: Set<String>): Boolean {
    return haveDescendantOfType<KtClassOrObject> {
        val name = it.name?.decapitalize() ?: return@haveDescendantOfType false
        functionNamesToFind.contains(name, true)
    }
}

fun PsiElement.getTestOfClassObjectOfMethodName(functionNamesToFind: Set<String>): KtClassOrObject? {
    return findDescendantOfType {
        val name = it.name?.decapitalize() ?: return@findDescendantOfType false
        functionNamesToFind.contains(name, true)
    }
}
fun String.computeTestNames(): Set<String> {
    return setOf(
        this,
        "test" + this.capitalize(),
        this + "test"
    )
}

