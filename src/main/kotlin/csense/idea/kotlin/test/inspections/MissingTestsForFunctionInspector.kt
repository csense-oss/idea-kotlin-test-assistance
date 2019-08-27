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
                if (ourFunction.isInTestModule()) {
                    return@namedFunctionVisitor
                }

                //step 2 is to find the test file in the test root
                val testModule = ourFunction.findTestModule() ?: return@namedFunctionVisitor
                val resultingDirectory = testModule.findPackageDir(ourFunction.containingKtFile)

                val testFile = resultingDirectory?.findTestFile(ourFunction.containingKtFile)

                if (testFile == null && !ourFunction.isTopLevel) {
                    return@namedFunctionVisitor //skip class / obj functions if no test file is found
                }

                val haveTestFunction = testFile?.haveTestOfMethod(ourFunction) == true
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

    fun createQuickFixesForFunction(file: KtFile?, ourFunction: KtNamedFunction): Array<LocalQuickFix> {
        val firstClass = file?.classes?.firstOrNull()
        val ktClassOrObject: KtClassOrObject = when (firstClass) {
            is KtClassOrObject -> firstClass
            is KtLightClass -> firstClass.kotlinOrigin ?: return arrayOf()
            else -> return arrayOf()
        }

        return arrayOf(AddTestMethodQuickFix(
                ourFunction,
                "test" + ourFunction.name?.capitalize(),
                ktClassOrObject
        ))
    }
}
