package csense.idea.kotlin.test.inspections

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import com.intellij.openapi.module.*
import com.intellij.psi.*
import csense.idea.kotlin.test.bll.*
import csense.idea.kotlin.test.quickfixes.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.idea.refactoring.*
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
            if (ourFunction.isPrivate() ||
                    ourFunction.isProtected() ||
                    ourFunction.isAbstract() ||
                    ourFunction.containingKtFile.shouldIgnore() ||
                    ourFunction.isInTestModule()) {
                return@namedFunctionVisitor//ignore private & protected  methods / non kt files.
            }


            val timeInMs = measureTimeMillis {
                val parent = ourFunction.containingClassOrObject?.namedClassOrObject()

                //skip anonymous classes' function(s)
                if (parent != null && parent.isAnonymous()) {
                    return@namedFunctionVisitor
                }

                val containingKtFile = ourFunction.containingKtFile

                val safeContainingClass = parent
                //step 2 is to find the test file in the test root
                val testModule = ourFunction.findTestModule() ?: return@namedFunctionVisitor
                val resultingDirectory = testModule.findPackageDir(containingKtFile)

                val testFile = resultingDirectory?.findTestFile(containingKtFile)

                if (testFile == null) {
                    //offer to create the file in the dir.
                    holder.registerProblem(ourFunction.nameIdentifier ?: ourFunction,
                            "There are no test file",
                            CreateTestFileQuickFix(testModule, resultingDirectory,
                                    ourFunction.containingKtFile))
                }

                if (testFile == null && !ourFunction.isTopLevel) {
                    return@namedFunctionVisitor //skip class / obj functions if no test file is found
                }
                val namesToLookAt = ourFunction.computeViableNames()
                val haveTestOfMethod = testFile?.haveTestOfMethod(
                        namesToLookAt,
                        ourFunction.containingKtFile,
                        safeContainingClass
                ) == true
                if (!haveTestOfMethod) {
                    //TODO use file name if containg is null / empty.
                    val testClass = testFile?.findMostSuitableTestClass(
                            safeContainingClass,
                            ourFunction.containingKtFile.virtualFile.nameWithoutExtension)
                    val fixes = createQuickFixesForFunction(
                            testClass,
                            ourFunction,
                            resultingDirectory,
                            testModule,
                            testFile
                    )
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


    fun createQuickFixesForFunction(
            testClass: KtClassOrObject?,
            ourFunction: KtNamedFunction,
            resultingDir: PsiDirectory?,
            testModule: Module,
            testFile: KtFile?
    ): Array<LocalQuickFix> {
        if (testFile == null) {
            return arrayOf(CreateTestFileQuickFix(testModule, resultingDir, ourFunction.containingKtFile))
        }
        if (testClass == null) {
            return arrayOf(
                    CreateTestClassQuickFix(
                            ourFunction.containingClassOrObject?.namedClassOrObject()?.name
                                    ?: ourFunction.containingKtFile.virtualFile.nameWithoutExtension,
                            testFile))
        }

        val testName = ourFunction.computeMostPreciseName()
        return arrayOf(AddTestMethodQuickFix(
                ourFunction,
                testName,
                testClass
        ))
    }


}

fun KtNamedFunction.computeMostPreciseName(): String {
    val safeName = name ?: ""
    if (isExtensionDeclaration()) {
        val extensionName = receiverTypeReference?.text
        return if (haveOverloads()) {
            val firstParamName = firstParameterNameOrEmpty()
            extensionName?.plus(safeName.capitalize())?.plus(firstParamName) ?: safeName
        } else {
            extensionName?.plus(safeName.capitalize()) ?: safeName
        }.safeDecapitizedFunctionName()
    }
    if (haveOverloads()) {
        val firstParamName = firstParameterNameOrEmpty()
        return (safeName + firstParamName).safeFunctionName()
    }
    return safeName.safeDecapitizedFunctionName()
}


fun KtNamedFunction.firstParameterNameOrEmpty(): String {
    return valueParameters.firstOrNull()?.name?.capitalize() ?: ""
}

fun KtNamedFunction.firstParameterTypeCapOrEmpty(): String {
    //TODO consider that this could open op say "optString" or alike ways of expression the optionality.
    return valueParameters.firstOrNull()?.typeReference?.text?.replace("?", "")
            ?: ""
}

fun KtNamedFunction.computeViableNames(): List<String> {
    val overLoads = haveOverloads()
    val safeName = name ?: ""

    val regularNames = if (overLoads) {
        //overloads opens up 2 things
        // firstParameterName
        // firstParameterType
        //if an extension then also
        // typeSafeName
        listOfNotNull(
                safeName + firstParameterNameOrEmpty(),
                safeName + firstParameterTypeCapOrEmpty()

        )
    } else {
        //no overloads; so just the name should be sufficient.
        listOf(safeName)
    }
    val extensions = if (isExtensionDeclaration()) {
        val extensionName = receiverTypeReference?.text?.safeFunctionName()?.decapitalize()
        listOfNotNull(
                extensionName?.let { it.decapitalize() + safeName.capitalize() },
                extensionName?.let { it + safeName.capitalize() },
                extensionName?.let { it + safeName.capitalize() + firstParameterNameOrEmpty() },
                extensionName?.let { it + safeName.capitalize() + firstParameterTypeCapOrEmpty() })
    } else {
        listOf()
    }
    return regularNames + extensions
}
