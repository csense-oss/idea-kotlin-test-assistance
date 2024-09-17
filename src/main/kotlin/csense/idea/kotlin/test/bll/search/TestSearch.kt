package csense.idea.kotlin.test.bll.search

import com.intellij.openapi.module.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import csense.idea.base.bll.platform.*
import csense.idea.base.bll.psi.*
import csense.idea.base.module.*
import csense.idea.kotlin.test.bll.*
import org.jetbrains.kotlin.psi.*

object TestSearch {
    fun findTestFileBy(containingKtFile: KtFile): KtFile? {
        val testModule: VirtualFile = TestInformationCache.lookupModuleTestSourceRoot(containingKtFile) ?: return null
        val resultingDirectory: PsiDirectory? =
            testModule.toPsiDirectory(containingKtFile.project)?.findPackageDir(containingKtFile)
        return resultingDirectory?.findTestFile(containingKtFile)
    }

    fun findCodeFromTestFile(containingKtFile: KtFile): KtFile? {
        return null
//        val codeModule: Module = containingKtFile.findModule() ?: return null
//        val probable: Module = codeModule.findMostProbableSourceModuleFromTest() ?: return null
//        probable.
//        TODO("Not yet implemented")
    }
}