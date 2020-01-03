package csense.idea.kotlin.test.inspections

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import com.intellij.openapi.module.Module
import com.intellij.psi.PsiDirectory
import csense.idea.base.bll.kotlin.hasConstantCustomGetterOnly
import csense.idea.base.bll.kotlin.hasCustomSetterGetter
import csense.idea.base.bll.registerProblemSafe
import csense.idea.base.module.findPackageDir
import csense.idea.base.module.findTestModule
import csense.idea.base.module.isInTestModule
import csense.idea.kotlin.test.bll.*
import csense.idea.kotlin.test.quickfixes.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.idea.refactoring.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*
import kotlin.system.*

class MissingTestsForPropertyInspector : AbstractKotlinInspection() {
    override fun getDisplayName(): String {
        return "Missing test for property (getter/setter)"
    }

    override fun getStaticDescription(): String? {
        return "Highlights properties with custom getter / setter that are missing test(s)"
    }

    override fun getDefaultLevel(): HighlightDisplayLevel {
        return HighlightDisplayLevel.WEAK_WARNING
    }

    override fun getDescriptionFileName(): String? {
        return "Highlights properties with custom getter / setter that are missing test(s) "
    }

    override fun getShortName(): String {
        return "MissingTestProperty"
    }

    override fun getGroupDisplayName(): String {
        return Constants.groupName
    }

    override fun isEnabledByDefault(): Boolean {
        return true
    }

    override fun buildVisitor(holder: ProblemsHolder,
                              isOnTheFly: Boolean): KtVisitorVoid {
        return propertyVisitor { prop ->
            if (prop.isPrivate() ||
                    prop.isProtected() ||
                    prop.isAbstract() ||
                    !prop.hasCustomSetterGetter() ||
                    prop.containingKtFile.shouldIgnore() ||
                    prop.isInTestModule()) {
                return@propertyVisitor//ignore private & protected  methods / non kt files.
            }
            val safeContainingClasss = prop.containingClassOrObject?.namedClassOrObject()

            if (prop.hasConstantCustomGetterOnly()) {
                return@propertyVisitor
            }
            val timeInMs = measureTimeMillis {

                //step 2 is to find the test file in the test root
                val testModule = prop.findTestModule() ?: return@propertyVisitor
                val resultingDirectory = testModule.findPackageDir(prop.containingKtFile)

                val testFile = resultingDirectory?.findTestFile(prop.containingKtFile)

                if (testFile == null && !prop.isTopLevel) {
                    return@propertyVisitor //skip class / obj functions if no test file is found
                }
                val namesToLookAt = prop.computeViableNames()
                val haveTestOfMethod = testFile?.haveTestOfMethod(
                        namesToLookAt,
                        prop.containingKtFile,
                        safeContainingClasss
                ) == true
                if (!haveTestOfMethod) {
                    val testClass = testFile?.findMostSuitableTestClass(
                            safeContainingClasss,
                            prop.containingKtFile.virtualFile.nameWithoutExtension)
                    val fixes = createQuickFixesForFunction(testClass, prop, resultingDirectory, testModule, testFile)
                    holder.registerProblemSafe(prop.nameIdentifier ?: prop,
                            "You have properly not tested this property (getter/setter)",
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
            ourProp: KtProperty,
            resultingDir: PsiDirectory?,
            testModule: Module,
            testFile: KtFile?
    ): Array<LocalQuickFix> {
        if (testFile == null) {
            return arrayOf(CreateTestFileQuickFix(testModule, resultingDir, ourProp.containingKtFile))
        }
        if (testClass == null) {
            return arrayOf(
                    CreateTestClassQuickFix(
                            ourProp.containingClassOrObject?.namedClassOrObject()?.name
                                    ?: ourProp.containingKtFile.virtualFile.nameWithoutExtension,
                            testFile))
        }

        val testName = ourProp.computeMostPreciseName()
        return arrayOf(AddTestPropertyQuickFix(
                ourProp,
                testName,
                testClass
        ))
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

