package csense.idea.kotlin.test.inspections

import com.intellij.codeHighlighting.*
import com.intellij.codeInspection.*
import csense.idea.kotlin.test.bll.*
import org.jetbrains.kotlin.idea.inspections.*
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
            if (ourClass.isInTestModule()) {
                return@classOrObjectVisitor
            }
            //step 2 is to find the test file in the test root
            val testModule = ourClass.findTestModule() ?: return@classOrObjectVisitor
            val resultingDirectory = testModule.findPackageDir(ourClass.containingKtFile)
            val testFile = resultingDirectory?.findTestFile(ourClass.containingKtFile)
            if (testFile != null) {
                return@classOrObjectVisitor //there are tests for this class so just skip this.
            }
            holder.registerProblem(ourClass.nameIdentifier ?: ourClass,
                    "You have properly not tested this class")

        }
    }
}