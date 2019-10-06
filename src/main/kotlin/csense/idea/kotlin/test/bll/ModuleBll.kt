@file:Suppress("NOTHING_TO_INLINE")

package csense.idea.kotlin.test.bll

import com.intellij.openapi.module.*
import com.intellij.psi.*
import csense.kotlin.extensions.*
import csense.kotlin.extensions.primitives.*
import org.jetbrains.kotlin.idea.*
import org.jetbrains.kotlin.idea.refactoring.*
import org.jetbrains.kotlin.idea.util.*
import org.jetbrains.kotlin.idea.util.projectStructure.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*


fun PsiDirectory.findTestFile(containingFile: KtFile): KtFile? {
    val fileName = containingFile.virtualFile.nameWithoutExtension
    val searchingForNames = listOf(fileName)
    return files.find {
        it.name.startsWithAny(searchingForNames)
    } as? KtFile
}

//fun PsiDirectory.findTestFile(vararg fileNames: String): KtFile? {
//    val fileNameList = fileNames.toList()
//    return files.find {
//        it.name.startsWithAny(fileNameList)
//    } as? KtFile
//}


fun KtFile.haveTestOfMethodName(fnNames: List<String>): Boolean = fnNames.any { ourFunction ->
    val functionNamesToFind = setOf(
            ourFunction,
            "test" + ourFunction.capitalize(),
            ourFunction + "test")
    haveDescendantOfType<KtNamedFunction> {
        it.containingClassOrObject?.isTopLevel() == true &&
                functionNamesToFind.contains(it.name)
    }
}

fun KtFile.haveTestObjectOfMethodName(fnNames: List<String>): Boolean = fnNames.any { ourFunction ->
    val functionNamesToFind = setOf(
            ourFunction,
            "test" + ourFunction.capitalize(),
            ourFunction + "test")
    haveDescendantOfType<KtClassOrObject> {
        it.name?.decapitalize()?.startsWithAny(
                functionNamesToFind) ?: false
    }
}


fun KtFile.shouldIgnore(): Boolean {
    return this.fileType != KotlinLanguage.INSTANCE.associatedFileType ||
            !this.virtualFilePath.endsWithAny(".kt", ".kts")
}

fun Module.findPackageDir(containingFile: KtFile): PsiDirectory? {
    val packageName = containingFile.packageFqName.asString()
    val sourceRoot = sourceRoots.find {
        it.name == "kotlin"
    } ?: return null
    val psiDirectory = sourceRoot.toPsiDirectory(project) ?: return null
    return if (packageName == "") {
        psiDirectory
    } else {
        psiDirectory.findPackage(packageName)
    }
}

fun PsiElement.isInTestModule(): Boolean {
    val module = ModuleUtil.findModuleForPsiElement(this) ?: return false
    return module.isTestModule()
}

fun Module.isTestModule(): Boolean {
    return name.endsWith("_test") || name.endsWith(".test")
}

fun PsiElement.findTestModule(): Module? {
    val module = ModuleUtil.findModuleForPsiElement(this) ?: return null
    //step 2 is to find the test file in the test root
    if (module.isTestModule()) {
        return null
    }

    val searchingFor = module.name
            .replace("_main", "_test")
            .replace(".main", ".test")
    return this.project.allModules().find { mod: Module ->
        searchingFor == mod.name
    }
}

fun PsiDirectory.findPackage(packageName: String): PsiDirectory? {
    if (packageName.isEmpty()) {
        return null
    }
    val folders = packageName.split(".")
    var resultingDirectory = this
    folders.forEach {
        resultingDirectory = resultingDirectory.findSubdirectory(it) ?: return null
    }
    return resultingDirectory
}


inline fun KtNamedFunction.haveOverloads(): Boolean {
    //use class or object if there, or if we are toplevel use the file to search.
    containingClassOrObject?.let {
        return haveOverloads(it)
    }
    return haveOverloads(containingKtFile)
}

inline fun KtNamedFunction.haveOverloads(containingFile: KtFile): Boolean {
    return containingFile.countDescendantOfType<KtNamedFunction> {
        it.isTopLevel && it.name == this.name
    } > 1
}

inline fun KtNamedFunction.haveOverloads(containerClassOrObject: KtClassOrObject): Boolean {
    return containerClassOrObject.countDescendantOfType<KtNamedFunction> {
        it.name == this.name
    } > 1
}

inline fun <reified T : PsiElement> PsiElement.countDescendantOfType(
        noinline predicate: (T) -> Boolean
): Int {
    var counter = 0
    forEachDescendantOfType<T> {
        if (predicate(it)) {
            counter += 1
        }
    }
    return counter
}

inline fun <reified T : PsiElement> PsiElement.haveDescendantOfType(
        noinline predicate: (T) -> Boolean): Boolean {
    return findDescendantOfType(predicate).isNotNull
}