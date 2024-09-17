package csense.idea.kotlin.test.bll.testGeneration.generationSteps

import com.intellij.openapi.project.*
import csense.idea.kotlin.test.bll.frameworks.*

class CreateTestMethodGenerationStep(
    val methodFqName: String
) : TestCodeGenerationStep {
    override fun generate(project: Project, forFramework: TestFramework, withAssertions: AssertionsFramework) {
        TODO("Not yet implemented")
    }

}