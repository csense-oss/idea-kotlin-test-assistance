package csense.idea.kotlin.test.bll.analyzers

import com.intellij.openapi.module.*
import com.intellij.psi.*
import csense.idea.base.module.*
import csense.kotlin.ds.cache.*
import org.jetbrains.kotlin.psi.*

object TestInformationCache {
    
    fun isFileInTestModule(file: PsiFile): Boolean {
        isFileInTestModuleCache[file]?.let {
            return@isFileInTestModule it
        }
        return file.isInTestModule().apply {
            isFileInTestModuleCache[file] = this
        }
    }
    
    fun lookupModuleTestSourceRoot(element: PsiElement, file: KtFile): PsiDirectory? {
        val module = ModuleUtil.findModuleForFile(file) ?: return null
        moduleToTestSourceRoot[module]?.let {
            return@lookupModuleTestSourceRoot it
        }
        
        return element.findMostPropableTestSourceRoot().apply {
            moduleToTestSourceRoot[module] = this
        }
    }
    
    private val isFileInTestModuleCache = SimpleLRUCache<PsiFile, Boolean>(500)
    private val moduleToTestSourceRoot = SimpleLRUCache<Module, PsiDirectory?>(50)
}