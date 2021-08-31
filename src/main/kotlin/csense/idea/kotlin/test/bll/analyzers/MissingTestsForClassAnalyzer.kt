package csense.idea.kotlin.test.bll.analyzers

import com.intellij.openapi.module.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import csense.idea.base.bll.*
import csense.idea.base.bll.kotlin.*
import csense.idea.base.bll.psi.*
import csense.idea.base.module.*
import csense.idea.kotlin.test.bll.*
import csense.idea.kotlin.test.quickfixes.*
import org.jetbrains.kotlin.asJava.*
import org.jetbrains.kotlin.idea.caches.project.*
import org.jetbrains.kotlin.idea.util.*
import org.jetbrains.kotlin.idea.util.projectStructure.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*

object MissingTestsForClassAnalyzer {

    fun analyze(outerClass: KtClassOrObject): AnalyzerResult {
        val ourClass = outerClass.namedClassOrObject()
        val containingKtFile = ourClass.containingKtFile
        val project = containingKtFile.project
        val errors = mutableListOf<AnalyzerError>()
        val psiElementToHighlight = ourClass.nameIdentifier ?: ourClass
        if (TestInformationCache.isFileInTestModuleOrSourceRoot(containingKtFile, project) ||
            ourClass.isCompanion() ||
            ourClass.isAbstract() ||
            ourClass.isSealed() ||
            containingKtFile.shouldIgnore()
        ) {
            //skip companion objects / non kt files.
            return AnalyzerResult(errors)
        }

        //if it is Anonymous
        if (ourClass.isAnonymous()) {
            //we want to avoid marking the whole ann class..
            //TODO this can provoke exceptionsif the superTypeList is empty.
            errors.add(
                AnalyzerError(
                    ourClass.findDescendantOfType<KtSuperTypeList>() ?: ourClass,
                    "Anonymous classes are hard to test, consider making this a class of itself",
                    arrayOf()
                )
            )
            return AnalyzerResult(errors)
        }

        //skip classes /things with no functions
        val functions = ourClass.getAllFunctions()
        if (functions.isEmpty()) {
            //if no functions are there, we have "nothing" to test. bail.
            return AnalyzerResult(errors)
        }

        //if we are an interface, and we have default impl's, then we should inspect the interface
        // otherwise NO
        if (ourClass.isInterfaceClass() && !ourClass.hasInterfaceDefaultImpls) {
            return AnalyzerResult(errors)
        }

        //step 2 is to find the test file in the test root
        val testSourceRoot = TestInformationCache.lookupModuleTestSourceRoot(containingKtFile)
        if (testSourceRoot == null) {
            errors.add(
                AnalyzerError(
                    psiElementToHighlight,
                    "There are no test source root",
                    arrayOf()
                )
            )
            return AnalyzerResult(errors)
        }

        val resultingDirectory = testSourceRoot.findPackageDir(containingKtFile)
        val testFile = resultingDirectory?.findTestFile(containingKtFile)

        if (testFile != null) {
            val haveTestClass = testFile.findMostSuitableTestClass(
                ourClass,
                containingKtFile.virtualFile.nameWithoutExtension
            )
            if (haveTestClass != null) {
                return AnalyzerResult(errors)
            } else {
                errors.add(
                    AnalyzerError(
                        psiElementToHighlight,
                        "You have properly not tested this class",
                        arrayOf(
                            CreateTestClassQuickFix(
                                ourClass.name + "Test",
                                testFile
                            )
                        )
                    )
                )
            }
        } else {
            errors.add(
                AnalyzerError(
                    psiElementToHighlight,
                    "You have properly not tested this class",
                    arrayOf(
                        CreateTestFileQuickFix(
                            testSourceRoot,
                            resultingDirectory,
                            containingKtFile
                        )
                    )
                )
            )
        }

        return AnalyzerResult(errors)
    }
}

fun PsiElement.findMostPropableTestSourceRoot(): PsiDirectory? {
    val module = ModuleUtil.findModuleForPsiElement(this) ?: return null
    //step 2 is to find the test file in the test root
    if (module.isTestModule()) {
        return null
    }
    return module.findMostPropableTestSourceRootDir()
}

fun Module.findMostPropableTestSourceRootDir(): PsiDirectory? {
    return (findMostPropableTestSourceRoot()
        ?: findMostPropableTestModule()?.findMostPropableTestSourceRoot())?.toPsiDirectory(project)
}

fun Module.findMostPropableTestSourceRoot(): VirtualFile? {
    //strategy for sourceRoot searching
    val testSourceRoots = sourceRoots.filterTestSourceRoots(project)
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
    val inst = ProjectFileIndex.SERVICE.getInstance(project)
    return filter {
        inst.isInTestSourceContent(it)
    }
}

fun Module.findMostPropableTestModule(): Module? {
    val searchingFor = this.name
    val allMods = project.allModules()
    if (isMPPModule || isNewMPPModule) {
        return when {
            searchingFor.contains("common", true) -> allMods.firstOrNull {
                it.isTestModule() && it.name.contains("commonTest", true)
            }
            searchingFor.contains("jvm", true) -> allMods.firstOrNull {
                it.isTestModule() && it.name.contains("jvmTest", true)
            }
            searchingFor.contains("js", true) -> allMods.firstOrNull {
                it.isTestModule() && it.name.contains("jsTest", true)
            }
            else -> allMods.firstOrNull { it.isTestModule() }
        }
    }
    return allMods.find { mod: Module ->
        //test will be fixed in next "shared base" (0.1.40)
        val isTestModule = mod.isTestModule() || run {
            ModuleRootManager.getInstance(mod).getSourceRoots(false).isEmpty() &&
                    ModuleRootManager.getInstance(mod).getSourceRoots(true).isNotEmpty()
        }
        if (!isTestModule) {
            return@find false
        }

        if (!ModuleRootManager.getInstance(mod).isDependsOn(this)) {
            return@find false
        }

        val modName = mod.name
        val withoutTestIndex = modName.length - 4
        val withoutTest = modName.substring(0, withoutTestIndex)
        searchingFor.startsWith(withoutTest)
    }
}


fun PsiDirectory.findPackageDir(file: KtFile): PsiDirectory? {
    val packageName = file.packageFqName.asString()
    return if (packageName == "") {
        this
    } else {
        this.findPackage(packageName)
    }
}
