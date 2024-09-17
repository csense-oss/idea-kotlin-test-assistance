package csense.idea.kotlin.test.bll.search

import com.intellij.openapi.module.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import csense.idea.base.bll.platform.*
import csense.idea.base.module.*
import csense.idea.kotlin.test.bll.*
import org.jetbrains.kotlin.idea.base.util.module
import org.jetbrains.kotlin.idea.util.*
import org.jetbrains.kotlin.psi.*

object TestSearch {
    fun findTestFileBy(containingKtFile: KtFile): KtFile? {
        val testModule: VirtualFile = TestInformationCache.lookupModuleTestSourceRoot(containingKtFile) ?: return null
        val resultingDirectory: PsiDirectory? =
            testModule.toPsiDirectory(containingKtFile.project)?.findPackageDir(containingKtFile)
        return resultingDirectory?.findTestFile(containingKtFile)
    }

    //TODO cleanup..
    fun findCodeFromTestFile(containingKtFile: KtFile): List<KtFile>? {
        val packageName: String = containingKtFile.packageFqName.asString()
        val cleanedFileName: String = containingKtFile.name.removeTestFileNames()
        val project: Project = containingKtFile.project
        val containingModule: Module = containingKtFile.module ?: return null

        val allPossibleSourceRoots: List<VirtualFile> = project.modules.filter { it: Module ->
            ModuleRootManager.getInstance(containingModule).isDependsOn(it)
        }.map { it: Module ->
            it.sourceRoots.filterSourceContent(project)
        }.flatten()
        val validPackages: List<VirtualFile> = allPossibleSourceRoots.mapNotNull { it: VirtualFile ->
            it.findDirectoryForPackage(packageName)
        }

        val validFiles: List<VirtualFile> = validPackages.mapNotNull { it: VirtualFile ->
            it.children?.filter { it: VirtualFile? ->
                it != null && it.isFile && it.nameWithoutExtension.startsWith(cleanedFileName)
            }
        }.flatten()

        val validPsiFiles: List<KtFile> = validFiles.mapNotNull { it: VirtualFile ->
            it.findPsiFile(project) as? KtFile
        }

        return validPsiFiles
    }
}

fun String.removeTestFileNames(): String {
    return this.removeSuffix("Test.kt") //TODO add more...
}

fun String.removeTestCodeNames():String{
    return this.removeSuffix("Test") //TODO add more
}

fun VirtualFile.findDirectoryForPackage(packageName: String): VirtualFile? {
    val packageParts: List<String> = packageName.split(".")
    var currentPackage: VirtualFile = this
    packageParts.forEach { partPackage: String ->
        val subDir: VirtualFile? = currentPackage.findDirectory(partPackage)
        if (subDir == null || subDir.doesNotExists()) {
            return@findDirectoryForPackage null
        }
        currentPackage = subDir
    }
    return currentPackage
}

fun VirtualFile.doesNotExists(): Boolean {
    return !exists()
}