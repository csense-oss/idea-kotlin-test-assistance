package csense.idea.kotlin.test.bll.analyzers

import com.intellij.openapi.module.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.psi.*
import csense.kotlin.ds.cache.*
import org.jetbrains.kotlin.psi.*

object TestInformationCache {
    
    fun isFileInTestModuleOrSourceRoot(file: PsiFile, project: Project): Boolean {
        isFileInTestModuleCache[file]?.let {
            return@isFileInTestModuleOrSourceRoot it
        }
        
        return ProjectFileIndex.SERVICE.getInstance(project).isInTestSourceContent(file.virtualFile).apply {
            isFileInTestModuleCache[file] = this
        }
    }
    
    fun lookupModuleTestSourceRoot(file: KtFile): PsiDirectory? {
        val module = ModuleUtil.findModuleForFile(file) ?: return null
        moduleToTestSourceRoot[module]?.let {
            return@lookupModuleTestSourceRoot it
        }
        
        return file.findMostPropableTestSourceRoot().apply {
            moduleToTestSourceRoot[module] = this
        }
    }
    
    private val isFileInTestModuleCache = SimpleLRUCache<PsiFile, Boolean>(500)
    private val moduleToTestSourceRoot = SimpleLRUCache<Module, PsiDirectory?>(50)
}