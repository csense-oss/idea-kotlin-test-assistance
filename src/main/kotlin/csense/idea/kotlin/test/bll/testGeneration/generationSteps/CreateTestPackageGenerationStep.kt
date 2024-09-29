package csense.idea.kotlin.test.bll.testGeneration.generationSteps

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import csense.idea.kotlin.test.bll.*
import csense.idea.kotlin.test.bll.frameworks.*

class CreateTestPackageGenerationStep(
    val packageFqName: String,
    val forModule: Module
) : TestCodeGenerationStep {
    override fun generate(
        project: Project,
        forFramework: TestFramework,
        withAssertions: AssertionsFramework
    ) {
        val sourceRoot: VirtualFile = forModule.findMostPropableTestSourceRoot() ?: return
        //traverse whole package space.
        TODO("Not yet implemented")
    }
}