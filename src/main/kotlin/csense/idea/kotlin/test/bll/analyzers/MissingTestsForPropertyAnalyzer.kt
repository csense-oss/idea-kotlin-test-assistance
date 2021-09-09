package csense.idea.kotlin.test.bll.analyzers

import com.intellij.codeInspection.*
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import csense.idea.base.bll.kotlin.*
import csense.idea.kotlin.test.bll.*
import csense.idea.kotlin.test.quickfixes.*
import org.jetbrains.kotlin.idea.refactoring.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*
import kotlin.system.*

object MissingTestsForPropertyAnalyzer {
    fun analyze(item: KtProperty, itemProject: Project? = null): AnalyzerResult {
        val containingKtFile = item.containingKtFile
        val project = itemProject ?: containingKtFile.project
        val psiElementToHighlight = item.nameIdentifier ?: item
        val errors = mutableListOf<AnalyzerError>()
        if (item.isPrivate() ||
            item.isProtected() ||
            item.isAbstract() ||
            !item.hasCustomSetterGetter() ||
            containingKtFile.shouldIgnore() ||
            TestInformationCache.isFileInTestModuleOrSourceRoot(containingKtFile, project)
        ) {
            return AnalyzerResult.empty//ignore private & protected  methods / non kt files.
        }
        val safeContainingClasss = item.containingClassOrObject?.namedClassOrObject()

        if (item.hasConstantCustomGetterOnly()) {
            return AnalyzerResult.empty
        }
        val timeInMs = measureTimeMillis {

            //step 2 is to find the test file in the test root
            val testModule = TestInformationCache.lookupModuleTestSourceRoot(containingKtFile)
            if (testModule == null) {
                errors.add(
                    AnalyzerError(
                        psiElementToHighlight,
                        "There are no test source root",
                        arrayOf()
                    )
                )
                return@analyze AnalyzerResult(errors)
            }

            val resultingDirectory = testModule.findPackageDir(containingKtFile)

            val testFile = resultingDirectory?.findTestFile(containingKtFile)

            if (testFile == null && !item.isTopLevel) {
                return@analyze AnalyzerResult.empty //skip class / obj functions if no test file is found
            }
            val namesToLookAt = item.computeViableNames()
            val haveTestOfMethod = testFile?.haveTestOfMethod(
                namesToLookAt,
                item.containingKtFile,
                safeContainingClasss
            ) == true
            if (!haveTestOfMethod) {
                val testClass = testFile?.findMostSuitableTestClass(
                    safeContainingClasss,
                    item.containingKtFile.virtualFile.nameWithoutExtension
                )
                val fixes = createQuickFixesForFunction(testClass, item, resultingDirectory, testModule, testFile)
                errors.add(
                    AnalyzerError(
                        psiElementToHighlight,
                        "You have properly not tested this property (getter/setter)",
                        fixes
                    )
                )
            }
        }

        if (timeInMs > 10) {
            println("Took $timeInMs ms")
        }
        return AnalyzerResult(errors)
    }

    fun createQuickFixesForFunction(
        testClass: KtClassOrObject?,
        ourProp: KtProperty,
        resultingDir: PsiDirectory?,
        testSourceRoot: PsiDirectory,
        testFile: KtFile?
    ): Array<LocalQuickFix> {
        if (testFile == null) {
            return arrayOf(CreateTestFileQuickFix(testSourceRoot, resultingDir, ourProp.containingKtFile))
        }
        if (testClass == null) {
            return arrayOf(
                CreateTestClassQuickFix(
                    ourProp.containingClassOrObject?.namedClassOrObject()?.name
                        ?: ourProp.containingKtFile.virtualFile.nameWithoutExtension,
                    testFile
                )
            )
        }

        val testName = ourProp.computeMostPreciseName()
        return arrayOf(
            AddTestPropertyQuickFix(
                ourProp,
                testName,
                testClass
            )
        )
    }
}


fun KtProperty.computeViableNames(): List<String> {
    val safeName = name ?: ""
    val extensionNames: List<String> = if (isExtensionDeclaration()) {
        val extName = receiverTypeReference?.text?.safeDecapitizedFunctionName()
        return listOfNotNull(extName + safeName.capitalize())
    } else {
        listOf()
    }
    return listOf(safeName) + extensionNames
}

fun KtProperty.computeMostPreciseName(): String {
    return if (isExtensionDeclaration()) {
        val extensionName = receiverTypeReference?.text?.safeDecapitizedFunctionName()
        extensionName?.plus(name?.capitalize() ?: "") ?: name ?: ""
    } else {
        name ?: ""
    }
}

