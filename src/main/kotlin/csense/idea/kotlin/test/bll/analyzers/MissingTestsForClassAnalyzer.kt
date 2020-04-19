package csense.idea.kotlin.test.bll.analyzers

import csense.idea.base.bll.kotlin.*
import csense.idea.base.bll.psi.*
import csense.idea.base.module.*
import csense.idea.kotlin.test.bll.*
import csense.idea.kotlin.test.quickfixes.*
import org.jetbrains.kotlin.asJava.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*

object MissingTestsForClassAnalyzer : Analyzer<KtClassOrObject> {
    
    override fun analyze(outerClass: KtClassOrObject): AnalyzerResult {
        val errors = mutableListOf<AnalyzerError>()
        val ourClass = outerClass.namedClassOrObject()
        val ktFile = ourClass.containingKtFile
        if (ourClass.isInTestModule() ||
                ourClass.isCompanion() ||
                ourClass.isAbstract() ||
                ourClass.isSealed() ||
                ktFile.shouldIgnore()) {
            //skip companion objects / non kt files.
            return AnalyzerResult(errors)
        }
        
        //if it is Anonymous
        if (ourClass.isAnonymous()) {
            //we want to avoid marking the whole ann class..
            errors.add(AnalyzerError(
                    ourClass.findDescendantOfType<KtSuperTypeList>() ?: ourClass,
                    "Anonymous classes are hard to test, consider making this a class of itself",
                    arrayOf()))
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
        val testModule = ourClass.findTestModule() ?: return AnalyzerResult.empty
        val resultingDirectory = testModule.findPackageDir(ktFile)
        val testFile = resultingDirectory?.findTestFile(ktFile)
        
        if (testFile != null) {
            val haveTestClass = testFile.findMostSuitableTestClass(
                    ourClass,
                    ktFile.virtualFile.nameWithoutExtension)
            if (haveTestClass != null) {
                return AnalyzerResult(errors)
            } else {
                errors.add(AnalyzerError(ourClass.nameIdentifier
                        ?: ourClass,
                        "You have properly not tested this class",
                        arrayOf(CreateTestClassQuickFix(
                                ourClass.name + "Test",
                                testFile
                        ))))
            }
        } else {
            errors.add(AnalyzerError(ourClass.nameIdentifier
                    ?: ourClass,
                    "You have properly not tested this class",
                    arrayOf(CreateTestFileQuickFix(
                            testModule,
                            resultingDirectory,
                            ktFile
                    ))))
        }
        
        return AnalyzerResult(errors)
    }
}