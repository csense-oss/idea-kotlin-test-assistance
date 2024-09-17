package csense.idea.kotlin.test.bll

import com.intellij.openapi.module.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import csense.idea.base.bll.psi.*
import csense.kotlin.datastructures.collections.*
import org.jetbrains.kotlin.idea.util.*
import org.jetbrains.kotlin.psi.*

//TODO look into TestFrameworks (idea code)
object TestInformationCache {

    fun isFileInTestModuleOrSourceRoot(file: PsiFile, project: Project): Boolean {
        return isFileInTestModuleCache.getOrPut(
            key = file,
            value = {
                project.fileIndexService.isInTestSourceContent(file.virtualFile)
            }
        )
    }

    fun lookupModuleTestSourceRoot(file: KtFile): VirtualFile? {
        val module: Module = file.findModule() ?: return null
        val testSource: VirtualFile? = module.findMostPropableTestSourceRoot()
        return moduleToTestSourceRoot.getOrPut(
            key = module,
            value = { testSource }
        )
    }

    private val isFileInTestModuleCache = SimpleLRUCache<PsiFile, Boolean>(cacheSize = 500)
    private val moduleToTestSourceRoot = SimpleLRUCache<Module, VirtualFile?>(cacheSize = 50)
}


fun PsiFile.isInTestModule2(): Boolean {
    return TestInformationCache.isFileInTestModuleOrSourceRoot(file = this, project = project)
}

fun PsiFile.isNotInTestModule(): Boolean {
    return !isInTestModule2()
}


//TEMP

fun PsiElement.isInTestModule2(): Boolean {
    //works for android.. & idea 203 + 213
    return TestSourcesFilter.isTestSources(
        /* file = */ containingFile.virtualFile,
        /* project = */ project
    )
}


fun Module.isTestModule(): Boolean {
    //TODO use testSourcesFilter or study isInTestSourceContent's docs..
//    if (sourceType == SourceType.TEST) {
//        return true
//    }
    val rootMgr: ModuleRootManager = ModuleRootManager.getInstance(this)
    return rootMgr.getSourceRoots(false).isEmpty() &&
            rootMgr.getSourceRoots(true).isNotEmpty()
}


fun Module.findMostPropableTestModule(): Module? {
    val allModules: List<Module> = this.project.modules.asList()
    val allTestModules: List<Module> = allModules.filter { it: Module -> it.isTestModule() }
    val validTestModules: List<Module> = allTestModules.filter { it: Module ->
        ModuleRootManager.getInstance(it).isDependsOn(this)
    }
    return validTestModules.selectByName(this)
        ?: validTestModules.selectBestCandidate(this)
}

fun Module.findMostProbableSourceModuleFromTest(): Module? {
    val allModules: List<Module> = this.project.modules.asList()
    val allTestModules: List<Module> = allModules.filter { it: Module -> it.isTestModule() }
    val validTestModules: List<Module> = allTestModules.filter { it: Module ->
        ModuleRootManager.getInstance(it).isDependsOn(this)
    }
    return validTestModules.selectByName(this)
        ?: validTestModules.selectBestCandidate(this)
}

private fun List<Module>.selectByName(fromModule: Module): Module? {
    return firstOrNull { it: Module ->
        it.name.removeSuffix("Test").equals(
            fromModule.name.removeSuffix("Main")
        )
    }
}

private fun List<Module>.selectBestCandidate(fromModule: Module): Module? {
    return firstOrNull { it: Module ->
        it.name.endsWith("unitTest", ignoreCase = true)
    } ?: firstOrNull()
}

fun Module.findMostPropableTestSourceRoot(): VirtualFile? {
    val thisTestRoot: VirtualFile? = findMostPropableTestSourceRootLocal()
    if (thisTestRoot != null) {
        return thisTestRoot
    }
    val bestTestModule: Module = findMostPropableTestModule() ?: return null
    return bestTestModule.findMostPropableTestSourceRootLocal()
}

fun Module.findMostPropableTestSourceRootLocal(): VirtualFile? {
    val testSourceRoots: List<VirtualFile> = sourceRoots.filterTestSourceRoots(project)
    return testSourceRoots.findMostPreferedTestSourceRootForKotlin()
}

/**
 * Will first find the kotlin folder, then the java then if non matches, the first if any
 * @receiver List<VirtualFile>
 * @return VirtualFile?
 */
fun List<VirtualFile>.findMostPreferedTestSourceRootForKotlin(): VirtualFile? {
    return firstOrNull {
        it.name.equals("kotlin", true)
    } ?: firstOrNull {
        it.name.equals("java", true)
    } ?: firstOrNull()
}

fun Array<VirtualFile>.filterTestSourceRoots(project: Project): List<VirtualFile> {
    val inst: ProjectFileIndex = project.fileIndexService
    return filter { it: VirtualFile ->
        inst.isInTestSourceContent(it)
    }
}