package csense.idea.kotlin.test.bll

import com.intellij.openapi.module.*
import com.intellij.psi.*
import csense.kotlin.extensions.*
import csense.kotlin.extensions.primitives.*
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

fun PsiDirectory.findTestFile(vararg fileNames: String): KtFile? {
    val fileNameList = fileNames.toList()
    return files.find {
        it.name.startsWithAny(fileNameList)
    } as? KtFile
}


fun KtFile.haveTestOfMethod(ourFunction: KtNamedFunction): Boolean {
    val functionNamesToFind = setOf(
            ourFunction.name ?: "",
            "test" + ourFunction.name?.capitalize(),
            ourFunction.name + "test")
    return findDescendantOfType<KtNamedFunction> {
        it.name?.startsWithAny(functionNamesToFind) ?: false
    }.isNotNull
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