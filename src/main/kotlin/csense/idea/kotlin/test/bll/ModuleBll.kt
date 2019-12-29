@file:Suppress("NOTHING_TO_INLINE")

package csense.idea.kotlin.test.bll

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import csense.kotlin.extensions.collections.typed.contains
import csense.kotlin.extensions.isNotNull
import csense.kotlin.extensions.primitives.doesNotEndsWith
import csense.kotlin.extensions.primitives.endsWithAny
import csense.kotlin.extensions.primitives.startsWithAny
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.util.projectStructure.allModules
import org.jetbrains.kotlin.idea.util.sourceRoots
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.findDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.forEachDescendantOfType
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
            this + "Tests"
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
    return findDescendantOfType { it: KtClassOrObject ->
        forClass?.name?.let { ourClass ->
            it.name?.startsWith(ourClass, true)
        }
                ?: it.name?.startsWith(fileName, true)
                ?: false
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


fun KtFile.shouldIgnore(): Boolean {
    return this.fileType != KotlinLanguage.INSTANCE.associatedFileType ||
            !this.virtualFilePath.endsWithAny(".kt", ".kts")
}

fun Module.findPackageDir(containingFile: KtFile): PsiDirectory? {
    val packageName = containingFile.packageFqName.asString()
    val psiDirectory = findKotlinRootDir() ?: return null
    return if (packageName == "") {
        psiDirectory
    } else {
        psiDirectory.findPackage(packageName)
    }
}

fun Module.findKotlinRootDir(): PsiDirectory? {
    val sourceRoot = sourceRoots.find {
        it.name == "kotlin"
    } ?: return null
    return sourceRoot.toPsiDirectory(project) ?: return null
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
    return this.project.allModules().find { mod: Module ->
        val modName = mod.name
        //if the name starts with teh same and
        //  &&mod.testSourceInfo() != null
        if (modName.doesNotEndsWith("test", true) || modName.length < 4) {
            return@find false
        }
        if (!mod.rootManager.isDependsOn(module)) {
            return@find false
        }

        val withoutTestIndex = modName.length - 4
        val withoutTest = modName.substring(0, withoutTestIndex)
        searchingFor.startsWith(withoutTest)
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
        crossinline predicate: (T) -> Boolean
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
    val found = findDescendantOfType<T> {
        it != this && predicate(it)
    }
    return found != this && found.isNotNull
}

fun String.computeTestNames(): Set<String> {
    return setOf(
            this,
            "test" + this.capitalize(),
            this + "test")
}

fun VirtualFile.toPsiDirectory(project: Project): PsiDirectory? = PsiManager.getInstance(project).findDirectory(this)
