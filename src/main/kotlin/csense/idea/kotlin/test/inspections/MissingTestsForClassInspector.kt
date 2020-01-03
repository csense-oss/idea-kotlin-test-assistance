package csense.idea.kotlin.test.inspections

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import com.intellij.psi.*
import csense.idea.base.bll.kotlin.*
import csense.idea.base.bll.psi.isInterfaceClass
import csense.idea.base.bll.registerProblemSafe
import csense.idea.base.module.findPackageDir
import csense.idea.base.module.findTestModule
import csense.idea.base.module.isInTestModule
import csense.idea.kotlin.test.bll.*
import csense.idea.kotlin.test.quickfixes.*
import csense.kotlin.extensions.toUnit
import csense.kotlin.extensions.tryAndLog
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
            val ktFile = ourClass.containingKtFile
            if (ourClass.isInTestModule() ||
                    ourClass.isCompanion() ||
                    ourClass.isAbstract() ||
                    ourClass.isSealed() ||
                    ktFile.shouldIgnore()) {
                return@classOrObjectVisitor//skip companion objects / non kt files.
            }

            //if it is Anonymous
            if (ourClass.isAnonymous()) {

                holder.registerProblemSafe(
                        ourClass.children.firstOrNull() ?: ourClass,
                        "Anonymous classes are hard to test, consider making this a class of itself")
                return@classOrObjectVisitor
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
            val resultingDirectory = testModule.findPackageDir(ktFile)
            val testFile = resultingDirectory?.findTestFile(ktFile)

            if (testFile != null) {
                val haveTestClass = testFile.findMostSuitableTestClass(
                        ourClass,
                        ktFile.virtualFile.nameWithoutExtension)
                if (haveTestClass != null) {
                    return@classOrObjectVisitor
                } else {
                    holder.registerProblemSafe(ourClass.nameIdentifier ?: ourClass,
                            "You have properly not tested this class",
                            CreateTestClassQuickFix(
                                    ourClass.name + "Test",
                                    testFile))
                }
            } else {
                holder.registerProblemSafe(ourClass.nameIdentifier ?: ourClass,
                        "You have properly not tested this class",
                        CreateTestFileQuickFix(
                                testModule,
                                resultingDirectory,
                                ktFile
                        ))
            }
        }
    }
}