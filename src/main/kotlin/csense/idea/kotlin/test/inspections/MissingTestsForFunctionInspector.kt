package csense.idea.kotlin.test.inspections

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import com.intellij.openapi.module.*
import com.intellij.psi.*
import csense.idea.kotlin.test.bll.*
import csense.idea.kotlin.test.quickfixes.*
import csense.kotlin.extensions.*
import csense.kotlin.extensions.primitives.*
import org.jetbrains.kotlin.asJava.classes.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.idea.refactoring.*
import org.jetbrains.kotlin.idea.util.*
import org.jetbrains.kotlin.idea.util.projectStructure.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*
import kotlin.system.*

class MissingTestsForFunctionInspector : AbstractKotlinInspection() {

    override fun getDisplayName(): String {
        return "Missing test for function"
    }

    override fun getStaticDescription(): String? {
        return "Highlights functions that are missing test(s)"
    }

    override fun getDefaultLevel(): HighlightDisplayLevel {
        return HighlightDisplayLevel.WEAK_WARNING
    }

    override fun getDescriptionFileName(): String? {
        return "Highlights functions that are missing test(s) "
    }

    override fun getShortName(): String {
        return "MissingTestFunction"
    }

    override fun getGroupDisplayName(): String {
        return Constants.groupName
    }

    override fun isEnabledByDefault(): Boolean {
        return true
    }


    override fun buildVisitor(holder: ProblemsHolder,
                              isOnTheFly: Boolean): KtVisitorVoid {
        return namedFunctionVisitor { ourFunction: KtNamedFunction ->
            if (ourFunction.isPrivate() || ourFunction.isProtected()) {
                return@namedFunctionVisitor//ignore private & protected  methods
            }
            val timeInMs = measureTimeMillis {
                if (isInTestModule(ourFunction)) {
                    return@namedFunctionVisitor
                }
                //step 2 is to find the test file in the test root
                val testModule = findTestModule(ourFunction) ?: return@namedFunctionVisitor
                val resultingDirectory = findPackageDir(testModule, ourFunction.containingKtFile)
                val testFile = resultingDirectory?.let {
                    findTestFile(it, ourFunction.containingKtFile)
                } ?: return@namedFunctionVisitor
                val haveTestFunction = haveTestOfMethod(ourFunction, testFile)
                if (!haveTestFunction) {
                    val fixes = createQuickFixesForFunction(testFile, ourFunction)
                    holder.registerProblem(ourFunction.nameIdentifier ?: ourFunction,
                            "You have properly not tested this method",
                            *fixes)
                }
            }
            if (timeInMs > 10) {
                println("Took $timeInMs ms")
            }
        }
    }

    fun findTestFile(resultingDirectory: PsiDirectory, containingFile: KtFile): KtFile? {
        val fileName = containingFile.virtualFile.nameWithoutExtension
        val searchingForNames = listOf(fileName)
        return resultingDirectory.files.find {
            it.name.startsWithAny(searchingForNames)
        } as? KtFile
    }

    fun haveTestOfMethod(ourFunction: KtNamedFunction, testFile: KtFile): Boolean {
        val functionNamesToFind = setOf(
                ourFunction.name ?: "",
                "test" + ourFunction.name?.capitalize(),
                ourFunction.name + "test")
        return testFile.findDescendantOfType<KtNamedFunction> {
            it.name?.startsWithAny(functionNamesToFind) ?: false
        }.isNotNull
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

    fun isInTestModule(startingPoint: KtNamedFunction): Boolean {
        val module = ModuleUtil.findModuleForPsiElement(startingPoint) ?: return false
        return module.isTestModule()
    }

    fun Module.isTestModule(): Boolean {
        return name.endsWith("_test") || name.endsWith(".test")
    }

    fun findTestModule(startingPoint: KtNamedFunction): Module? {
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


    fun createQuickFixesForFunction(file: KtFile, ourFunction: KtNamedFunction): Array<LocalQuickFix> {
        val firstClass = file.classes.firstOrNull()
        return if (firstClass != null) {

            val ktClassOrObject: KtClassOrObject = when (firstClass) {
                is KtClassOrObject -> firstClass
                is KtLightClass ->
                    firstClass.kotlinOrigin ?: return arrayOf()
                else -> return arrayOf()
            }

            arrayOf(AddTestMethodQuickFix(
                    ourFunction,
                    "test" + ourFunction.name?.capitalize(),
                    ktClassOrObject
            ))
        } else {
            arrayOf()
        }
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