package csense.idea.kotlin.test.bll.testGeneration.generationSteps

import com.intellij.openapi.project.*
import csense.idea.kotlin.test.bll.frameworks.*

interface TestCodeGenerationStep {
    fun generate(project: Project, forFramework: TestFramework, withAssertions: AssertionsFramework)

}
