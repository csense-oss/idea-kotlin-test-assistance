package csense.idea.kotlin.test.inspections

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import com.intellij.openapi.module.*
import com.intellij.psi.*
import csense.idea.kotlin.test.bll.*
import csense.kotlin.extensions.primitives.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.idea.refactoring.*
import org.jetbrains.kotlin.idea.util.*
import org.jetbrains.kotlin.idea.util.projectStructure.*
import org.jetbrains.kotlin.psi.*

class MissingTestsForClassInspector : AbstractKotlinInspection() {

    override fun getDisplayName(): String {
        return "Missing tests for class"
    }

    override fun getStaticDescription(): String? {
        return "Highlights classes that are missing test(s)"
    }

    override fun getDefaultLevel(): HighlightDisplayLevel {
        return HighlightDisplayLevel.WEAK_WARNING
    }

    override fun getDescriptionFileName(): String? {
        return "Highlights for classes that are missing test(s) "
    }

    override fun getShortName(): String {
        return "MissingTestClass"
    }

    override fun getGroupDisplayName(): String {
        return Constants.groupName
    }

    override fun isEnabledByDefault(): Boolean {
        return true
    }

    override fun buildVisitor(holder: ProblemsHolder,
                              isOnTheFly: Boolean): KtVisitorVoid {
        return classOrObjectVisitor { ourClass ->
            if (isInTestModule(ourClass)) {
                return@classOrObjectVisitor
            }
            //step 2 is to find the test file in the test root
            val testModule = findTestModule(ourClass) ?: return@classOrObjectVisitor
            val resultingDirectory = findPackageDir(testModule, ourClass.containingKtFile)
            val testFile = resultingDirectory?.let {
                findTestFile(it, ourClass.containingKtFile)
            }
            if (testFile != null) {
                return@classOrObjectVisitor //there are tests for this class so just skip this.
            }
            holder.registerProblem(ourClass.nameIdentifier ?: ourClass,
                    "You have properly not tested this class")

        }
    }


    fun isInTestModule(startingPoint: KtClassOrObject): Boolean {
        val module = ModuleUtil.findModuleForPsiElement(startingPoint) ?: return false
        return module.isTestModule()
    }

    fun Module.isTestModule(): Boolean {
        return name.endsWith("_test") || name.endsWith(".test")
    }

    fun findTestModule(startingPoint: KtClassOrObject): Module? {
        val module = ModuleUtil.findModuleForPsiElement(startingPoint) ?: return null
        //step 2 is to find the test file in the test root
        if (module.isTestModule()) {
            return null
        }

        val searchingFor = module.name
                .replace("_main", "_test")
                .replace(".main", ".test")
        return startingPoint.project.allModules().find { mod: Module ->
            searchingFor == mod.name
        }
    }

    fun findTestFile(resultingDirectory: PsiDirectory, containingFile: KtFile): KtFile? {
        val fileName = containingFile.virtualFile.nameWithoutExtension
        val searchingForNames = listOf(fileName)
        return resultingDirectory.files.find {
            it.name.startsWithAny(searchingForNames)
        } as? KtFile
    }


    fun findPackageDir(testModule: Module, containingFile: KtFile): PsiDirectory? {
        val packageName = containingFile.packageFqName.asString()
        val sourceRoot = testModule.sourceRoots.find {
            it.name == "kotlin"
        } ?: return null
        val psiDirectory = sourceRoot.toPsiDirectory(testModule.project) ?: return null
        return if (packageName == "") {
            psiDirectory
        } else {
            psiDirectory.findPackage(packageName)
        }
    }


}