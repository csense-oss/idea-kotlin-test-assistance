@file:Suppress("ActionIsNotPreviewFriendly")

package csense.idea.kotlin.test.inspections.missingTestsForFunction.fixes

import com.intellij.codeInspection.*
import com.intellij.openapi.project.*
import csense.idea.kotlin.test.bll.*
import csense.idea.kotlin.test.bll.frameworks.*
import csense.idea.kotlin.test.bll.testGeneration.generationSteps.*
import org.jetbrains.kotlin.idea.util.application.*

class AddTestMethodByStepsQuickFix(
    private val steps: List<TestCodeGenerationStep>,
    private val forFramework: TestFramework,
    private val withAssertions: AssertionsFramework
) : LocalQuickFix {
    override fun getFamilyName(): String {
        return Constants.groupName
    }

    override fun getName(): String {
        return "AddTestMethodQuickFix"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        project.executeWriteCommand(name = name) {
            steps.forEach { it: TestCodeGenerationStep ->
                it.generate(project, forFramework, withAssertions)
            }
        }
    }

}