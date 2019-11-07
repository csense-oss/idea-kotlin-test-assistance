package csense.idea.kotlin.test.inspections

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import com.intellij.psi.*
import csense.idea.kotlin.test.bll.*
import csense.idea.kotlin.test.quickfixes.*
import csense.kotlin.extensions.map
import org.jetbrains.kotlin.asJava.*
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.lexer.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*

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
        return classOrObjectVisitor { outerClass ->
            val ourClass = outerClass.namedClassOrObject()
            if (ourClass.isInTestModule() ||
                    ourClass.hasModifier(KtTokens.COMPANION_KEYWORD) ||
                    ourClass.containingKtFile.shouldIgnore()) {
                return@classOrObjectVisitor//skip companion objects / non kt files.
            }


            //skip classes /things with no functions
            val functions = ourClass.getAllFunctions()
            if (functions.isEmpty()) {
                //if no functions are there, we have "nothing" to test. bail.
                return@classOrObjectVisitor
            }

            //if we are an interface, and we have default impl's, then we should inspect the interface
            // otherwise NO
            if (ourClass.isInterfaceClass() && !ourClass.hasInterfaceDefaultImpls) {
                return@classOrObjectVisitor
            }

            //step 2 is to find the test file in the test root
            val testModule = ourClass.findTestModule() ?: return@classOrObjectVisitor
            val resultingDirectory = testModule.findPackageDir(ourClass.containingKtFile)
            val testFile = resultingDirectory?.findTestFile(ourClass.containingKtFile)

            if (testFile != null) {
                val haveTestClass = testFile.findMostSuitableTestClass(
                        ourClass,
                        ourClass.containingKtFile.virtualFile.nameWithoutExtension)
                if (haveTestClass != null) {
                    return@classOrObjectVisitor
                } else {
                    holder.registerProblem(ourClass.nameIdentifier ?: ourClass,
                            "You have properly not tested this class",
                            CreateTestClassQuickFix(
                                    ourClass.name + "Test",
                                    testFile))
                }
            } else {
                holder.registerProblem(ourClass.nameIdentifier ?: ourClass,
                        "You have properly not tested this class",
                        CreateTestFileQuickFix(
                                testModule,
                                resultingDirectory,
                                ourClass.containingKtFile
                        ))
            }
        }
    }
}

fun KtClassOrObject.getAllFunctions(): List<KtNamedFunction> =
        collectDescendantsOfType()

fun PsiNamedElement.isInterfaceClass(): Boolean = when (this) {
    is KtClass -> isInterface()
    is PsiClass -> isInterface
    else -> false
}