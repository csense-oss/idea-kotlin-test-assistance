package csense.idea.kotlin.test.bll.testGeneration.generationSteps

import com.intellij.openapi.project.*
import csense.idea.kotlin.test.bll.frameworks.*

class CreateTestPackageGenerationStep(
    val packageFqName: String
) : TestCodeGenerationStep {
    override fun generate(project: Project, forFramework: TestFramework, withAssertions: AssertionsFramework) {
        TODO("Not yet implemented")
    }
}