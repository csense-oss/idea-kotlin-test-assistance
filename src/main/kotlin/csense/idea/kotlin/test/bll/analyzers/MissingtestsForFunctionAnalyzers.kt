//package csense.idea.kotlin.test.bll.analyzers
//
//import com.intellij.codeInspection.*
//import com.intellij.psi.*
//import csense.idea.base.bll.kotlin.*
//import csense.idea.kotlin.test.bll.*
//import csense.idea.kotlin.test.bll.psi.*
//import csense.idea.kotlin.test.bll.search.*
//import csense.idea.kotlin.test.inspections.missingTestsForFunction.fixes.*
//import csense.kotlin.extensions.primitives.*
//import org.jetbrains.kotlin.idea.refactoring.*
//import org.jetbrains.kotlin.psi.*
//import org.jetbrains.kotlin.psi.psiUtil.*
//import kotlin.system.*
//
//object MissingtestsForFunctionAnalyzers {
//    fun analyze(ourFunction: KtNamedFunction, includeAll: Boolean = false): AnalyzerResult {
//        val containingKtFile = ourFunction.containingKtFile
//        val project = containingKtFile.project
//        val psiElementToHighlight = ourFunction.nameIdentifier ?: ourFunction
//        val errors = mutableListOf<AnalyzerError>()
//        if (ourFunction.isPrivate() ||
//            ourFunction.isProtected() ||
//            ourFunction.isAbstract() ||
//            containingKtFile.isNotKotlinFile() ||
//            TestInformationCache.isFileInTestModuleOrSourceRoot(containingKtFile, project)
//        ) {
//            return AnalyzerResult.empty//ignore private & protected  methods / non kt files.
//        }
//
//
//        val timeInMs = measureTimeMillis {
//            val parent = ourFunction.containingClassOrObject?.namedClassOrObject()
//            //skip anonymous classes' function(s)
//            if (parent != null && parent.isAnonymous()) {
//                return@analyze AnalyzerResult(errors)
//            }
//
//            val safeContainingClass = parent
//            //step 2 is to find the test file in the test root
//
//            val testModule = TestInformationCache.lookupModuleTestSourceRoot(containingKtFile)
//            if (testModule == null) {
//                errors.add(
//                    AnalyzerError(
//                        psiElementToHighlight,
//                        "There are no test source root",
//                        arrayOf()
//                    )
//                )
//                return@analyze AnalyzerResult(errors)
//            }
//
//            val resultingDirectory = testModule.findPackageDir(containingKtFile)
//
//            val testFile = resultingDirectory?.findTestFile(containingKtFile)
//
//            if (testFile == null) {
//                errors.add(
//                    AnalyzerError(
//                        psiElementToHighlight,
//                        "There are no test file",
//                        arrayOf(
//                            CreateTestFileQuickFix(
//                                testModule,
//                                resultingDirectory,
//                                containingKtFile
//                            )
//                        )
//                    )
//                )
//                //offer to create the file in the dir.
//            }
//
//            if (testFile == null && !ourFunction.isTopLevel) {
//                return@analyze AnalyzerResult(errors) //skip class / obj functions if no test file is found
//            }
//            val namesToLookAt = ourFunction.computeViableNames()
//            val haveTestOfMethod = testFile?.haveTestOfMethod(
//                namesToLookAt,
//                ourFunction.containingKtFile,
//                safeContainingClass
//            ) == true
//
//            if (!haveTestOfMethod) {
//                val fileName = containingKtFile.virtualFile.nameWithoutExtension.safeClassName()
//                val fixes: Array<LocalQuickFix>
//                if (safeContainingClass?.isCompanion() == true) {
//                    val parentTestClass = testFile?.findMostSuitableTestClass(
//                        safeContainingClass.containingClass(),
//                        fileName
//                    )
//                    fixes = createQuickFixesForCompanionFunction(
//                        parentTestClass,
//                        ourFunction,
//                        resultingDirectory,
//                        testModule,
//                        testFile
//                    )
//
//                } else {
//                    //TODO use file name if containing is null / empty.
//                    val testClass = testFile?.findMostSuitableTestClass(
//                        safeContainingClass,
//                        fileName
//                    )
//
//
//                    fixes = createQuickFixesForFunction(
//                        testClass,
//                        ourFunction,
//                        resultingDirectory,
//                        testModule,
//                        testFile
//                    )
//                }
//                errors.add(
//                    AnalyzerError(
//                        psiElementToHighlight,
//                        "You have properly not tested this method",
//                        fixes
//                    )
//                )
//            }
//        }
//        if (timeInMs > 10) {
//            println("Took $timeInMs ms")
//        }
//        return AnalyzerResult(errors)
//    }
//
//    fun createQuickFixesForFunction(
//        testClass: KtClassOrObject?,
//        ourFunction: KtNamedFunction,
//        resultingDir: PsiDirectory?,
//        testSourceRoot: PsiDirectory,
//        testFile: KtFile?
//    ): Array<LocalQuickFix> {
//        if (testFile == null) {
//            return arrayOf(CreateTestFileQuickFix(testSourceRoot, resultingDir, ourFunction.containingKtFile))
//        }
//        if (testClass == null) {
//            return arrayOf(
//                CreateTestClassQuickFix(
//                    ourFunction.containingClassOrObject?.namedClassOrObject()?.name
//                        ?: ourFunction.containingKtFile.virtualFile.nameWithoutExtension,
//                    testFile
//                )
//            )
//        }
//
//        val testName = ourFunction.computeMostPreciseTestName()
//        return arrayOf(
//            AddTestMethodQuickFix(
//                ourFunction,
//                testName,
//                testClass
//            )
//        )
//    }
//
//    fun createQuickFixesForCompanionFunction(
//        parentTestClass: KtClassOrObject?,
//        ourFunction: KtNamedFunction,
//        resultingDirectory: PsiDirectory?,
//        testModule: PsiDirectory,
//        testFile: KtFile?
//    ): Array<LocalQuickFix> {
//        if (testFile == null) {
//            return arrayOf(CreateTestFileQuickFix(testModule, resultingDirectory, ourFunction.containingKtFile))
//        }
//
////        if (parentTestClass == null) {
////            return arrayOf(
////                CreateCompanionTestClassQuickFix(
////                    ourFunction.containingClassOrObject?.namedClassOrObject()?.name
////                        ?: ourFunction.containingKtFile.virtualFile.nameWithoutExtension,
////                    testFile
////                )
////            )
////        }
//
//        return arrayOf()
//    }
//
////    fun createQuickFixesForCompanionFunction(
////        parentClass: KtClassOrObject?,
////        ourFunction: KtNamedFunction,
////        resultingDir: PsiDirectory?,
////        testSourceRoot: PsiDirectory,
////        testFile: KtFile?
////    ): Array<LocalQuickFix> {
////        if (testFile == null) {
////            return arrayOf(CreateTestFileQuickFix(testSourceRoot, resultingDir, ourFunction.containingKtFile))
////        }
////        if (parentClass == null) {
////            return arrayOf(
////                CreateCompanionTestClassQuickFix(
////                    ourFunction.containingClassOrObject?.namedClassOrObject()?.name
////                        ?: ourFunction.containingKtFile.virtualFile.nameWithoutExtension,
////                    testFile
////                )
////            )
////        }
////
////        val testName = ourFunction.computeMostPreciseName()
////        return arrayOf(
////            AddTestMethodQuickFix(
////                ourFunction,
////                testName,
////                testClass
////            )
////        )
////    }
//}
//
