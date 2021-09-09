@file:Suppress("NOTHING_TO_INLINE")

package csense.idea.kotlin.test.bll

import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import csense.idea.base.bll.kotlin.isCompanion
import csense.idea.base.bll.psi.countDescendantOfType
import csense.idea.base.bll.psi.haveDescendantOfType
import csense.kotlin.extensions.collections.typed.contains
import csense.kotlin.extensions.primitives.endsWithAny
import csense.kotlin.extensions.primitives.startsWithAny
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.findDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.isPublic


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
    val publicClasses = orgFile.countDescendantOfType<KtClassOrObject> {
        it.isPublic
    }
    return when {
        publicClasses == 1 || publicClasses == 0 -> { //eg if there are only extensions there will be no public classes.
            haveTestSingleClassOfMethodName(fnNames) || haveTestSingleClassObjectOfMethodName(fnNames)
        }
        publicClasses > 1 -> {
            haveTestMultipleClassOfMethodName(fnNames, orgClass, orgFile.virtualFile.nameWithoutExtension)
        }
        else -> {
            false
        }
    }
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
    val validClass = findMostSuitableTestClass(orgClass, fileName) ?: return false

    return fnNames.any {
        val functionNamesToFind = it.computeTestNames()
        val didFindFunction = validClass.haveTestOfMethodNames(functionNamesToFind)
        return@any didFindFunction || validClass.haveTestOfClassObjectOfMethodName(functionNamesToFind)
    }
}

fun KtFile.haveTestSingleClassOfMethodName(fnNames: List<String>): Boolean = fnNames.any { ourFunction ->
    val functionNamesToFind = ourFunction.computeTestNames()
    haveTestOfMethodNames(functionNamesToFind)
}

fun PsiElement.haveTestOfMethodNames(functionNamesToFind: Set<String>): Boolean =
    haveDescendantOfType<KtNamedFunction> {
        val name = it.name ?: return@haveDescendantOfType false
        it.containingClassOrObject?.isTopLevel() == true &&
                functionNamesToFind.contains(name, true)
    }


fun KtFile.haveTestSingleClassObjectOfMethodName(fnNames: List<String>): Boolean = fnNames.any { ourFunction ->
    val functionNamesToFind = ourFunction.computeTestNames()
    haveTestOfClassObjectOfMethodName(functionNamesToFind)
}

fun PsiElement.haveTestOfClassObjectOfMethodName(functionNamesToFind: Set<String>): Boolean {
    return haveDescendantOfType<KtClassOrObject> {
        val name = it.name?.decapitalize() ?: return@haveDescendantOfType false
        functionNamesToFind.contains(name, true)
    }
}

//TODO "is kotlin file" ?
fun KtFile.shouldIgnore(): Boolean {
    return this.fileType != KotlinLanguage.INSTANCE.associatedFileType ||
            !this.virtualFilePath.endsWithAny(".kt", ".kts")
}

fun String.computeTestNames(): Set<String> {
    return setOf(
        this,
        "test" + this.capitalize(),
        this + "test"
    )
}

