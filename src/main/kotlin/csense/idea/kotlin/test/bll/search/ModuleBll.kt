@file:Suppress("NOTHING_TO_INLINE")

package csense.idea.kotlin.test.bll.search

import com.intellij.psi.*
import csense.idea.base.bll.kotlin.*
import csense.idea.base.bll.psi.*
import csense.idea.kotlin.test.bll.*
import csense.kotlin.extensions.collections.*
import csense.kotlin.extensions.collections.typed.*
import csense.kotlin.extensions.primitives.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*


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
fun String.computeTestFileNames(): Set<String> {
    return setOf(
        this,
        this + "Test",
        this + "KtTest",
        this + "KtTests",
        this + "Tests",
        this.safeClassName() + "Test",
        this.safeClassName() + "KtTest",
        this.safeClassName() + "KtTests",
        this.safeClassName() + "Tests",
        "Test" + this.safeClassName().titleCaseFirstWord(),
    )
}

fun KtFile.haveTestOfMethod(fnNames: Set<String>, orgFile: KtFile, orgClass: KtClassOrObject?): Boolean {
    return findTestByNameOrNull(fnNames, orgFile, orgClass).isNotEmpty()
}

fun KtFile.findTestByNameOrNull(
    fnNames: Set<String>,
    orgFile: KtFile,
    orgClass: KtClassOrObject?
): List<PsiElement> {
    val publicClasses: Int = orgFile.countDescendantOfType<KtClassOrObject> { it: KtClassOrObject ->
        it.isPublic
    }
    val classes: List<PsiElement> = when {
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

fun KtFile.getTestMultipleClassOfMethodName(
    fnNames: Set<String>,
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
fun KtFile.getTestSingleClassOfMethodName(fnNames: Set<String>): List<PsiElement> = fnNames.mapNotNull { ourFunction ->
    val functionNamesToFind: Set<String> = ourFunction.computeTestNames()
    getTestByMethodNames(functionNamesToFind)
}

fun PsiElement.getTestByMethodNames(functionNamesToFind: Set<String>): KtNamedFunction? {
    return findDescendantOfType<KtNamedFunction> { it: KtNamedFunction ->
        val name: String = it.name ?: return@findDescendantOfType false
        it.containingClassOrObject?.isTopLevel() == true &&
                functionNamesToFind.contains(other = name, ignoreCase = true)
    }
}

fun KtFile.getTestSingleClassObjectOfMethodName(fnNames: Set<String>): List<PsiElement> =
    fnNames.mapNotNull { ourFunction: String ->
        val functionNamesToFind: Set<String> = ourFunction.computeTestNames()
        getTestOfClassObjectOfMethodName(functionNamesToFind)
    }

fun PsiElement.getTestOfClassObjectOfMethodName(functionNamesToFind: Set<String>): KtClassOrObject? {
    return findDescendantOfType { it: KtClassOrObject ->
        val name: String = it.name?.titleCaseFirstWord() ?: return@findDescendantOfType false
        functionNamesToFind.contains(other = name, ignoreCase = true)
    }
}
fun String.computeTestNames(): Set<String> {
    return setOf(
        this,
        "test" + this.titleCaseFirstWord(),
        this + "test"
    )
}